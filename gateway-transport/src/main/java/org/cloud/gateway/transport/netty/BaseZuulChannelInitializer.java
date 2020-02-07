package org.cloud.gateway.transport.netty;

import static com.google.common.base.Preconditions.checkNotNull;


import java.util.List;
import java.util.concurrent.TimeUnit;

import com.netflix.config.CachedDynamicIntProperty;
import com.netflix.servo.monitor.BasicCounter;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import static org.cloud.gateway.transport.netty.PassportState.*;

public abstract class BaseZuulChannelInitializer extends ChannelInitializer<Channel>
{
    public static final String HTTP_CODEC_HANDLER_NAME = "codec";
    public static final AttributeKey<ChannelConfig> ATTR_CHANNEL_CONFIG = AttributeKey.newInstance("channel_config");

    protected static final LoggingHandler nettyLogger = new LoggingHandler("zuul.server.nettylog", LogLevel.INFO);

    public static final CachedDynamicIntProperty MAX_INITIAL_LINE_LENGTH = new CachedDynamicIntProperty("server.http.decoder.maxInitialLineLength", 16384);
    public static final CachedDynamicIntProperty MAX_HEADER_SIZE = new CachedDynamicIntProperty("server.http.decoder.maxHeaderSize", 32768);
    public static final CachedDynamicIntProperty MAX_CHUNK_SIZE = new CachedDynamicIntProperty("server.http.decoder.maxChunkSize", 32768);

    /**
     * The port that the server intends to listen on.  Subclasses should NOT use this field, as it may not be set, and
     * may differ from the actual listening port.  For example:
     *
     * <ul>
     *     <li>When binding the server to port `0`, the actual port will be different from the one provided here.
     *     <li>If there is no port (such as in a LocalSocket, or DomainSocket), the port number may be `-1`.
     * </ul>
     *
     * <p>Instead, subclasses should read the local address on channel initialization, and decide to take action then.
     */
    @Deprecated
    protected final int port;
    protected final ChannelConfig channelConfig;
    protected final ChannelConfig channelDependencies;
    protected final int idleTimeout;
    protected final int httpRequestReadTimeout;
    protected final int maxRequestsPerConnection;
    protected final int maxRequestsPerConnectionInBrownout;
    protected final int connectionExpiry;
    protected final int maxConnections;
    private final int connCloseDelay;
    
    protected final MaxInboundConnectionsHandler maxConnectionsHandler;
    protected final BasicCounter httpRequestReadTimeoutCounter;  
    protected final boolean withProxyProtocol;
    protected final StripUntrustedProxyHeadersHandler stripInboundProxyHeadersHandler;
    // TODO
    //protected final HttpRequestThrottleChannelHandler requestThrottleHandler;
    protected final ChannelHandler rateLimitingChannelHandler;
    protected final ChannelHandler sslClientCertCheckChannelHandler;
    //protected final RequestRejectedChannelHandler requestRejectedChannelHandler;
    protected final SessionContextDecorator sessionContextDecorator;
    protected final RequestCompleteHandler requestCompleteHandler;
    protected final FilterLoader filterLoader;
    protected final FilterUsageNotifier filterUsageNotifier;
    protected final SourceAddressChannelHandler sourceAddressChannelHandler;

    /** A collection of all the active channels that we can use to things like graceful shutdown */
    protected final ChannelGroup channels;

    /**
     * After calling this method, child classes should not reference {@link #port} any more.
     */
    protected BaseZuulChannelInitializer(
            String metricId,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this(-1, metricId, channelConfig, channelDependencies, channels);
    }

    /**
     * Call {@link #BaseZuulChannelInitializer(String, ChannelConfig, ChannelConfig, ChannelGroup)} instead.
     */
    @Deprecated
    protected BaseZuulChannelInitializer(
            int port,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this(port, String.valueOf(port), channelConfig, channelDependencies, channels);
    }

    private BaseZuulChannelInitializer(
            int port,
            String metricId,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this.port = port;
        checkNotNull(metricId, "metricId");
        this.channelConfig = channelConfig;
        this.channelDependencies = channelDependencies;
        this.channels = channels;


        this.withProxyProtocol = channelConfig.get(CommonChannelConfigKeys.withProxyProtocol);

        this.idleTimeout = channelConfig.get(CommonChannelConfigKeys.idleTimeout);
        this.httpRequestReadTimeout = channelConfig.get(CommonChannelConfigKeys.httpRequestReadTimeout);


        this.maxConnections = channelConfig.get(CommonChannelConfigKeys.maxConnections);
        this.maxConnectionsHandler = new MaxInboundConnectionsHandler(maxConnections);
        this.maxRequestsPerConnection = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnection);
        this.maxRequestsPerConnectionInBrownout = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout);
        this.connectionExpiry = channelConfig.get(CommonChannelConfigKeys.connectionExpiry);
        this.connCloseDelay = channelConfig.get(CommonChannelConfigKeys.connCloseDelay);

        StripUntrustedProxyHeadersHandler.AllowWhen allowProxyHeadersWhen = channelConfig.get(CommonChannelConfigKeys.allowProxyHeadersWhen);
        this.stripInboundProxyHeadersHandler = new StripUntrustedProxyHeadersHandler(allowProxyHeadersWhen);

        this.rateLimitingChannelHandler = channelDependencies.get(ZuulDependencyKeys.rateLimitingChannelHandlerProvider).get();

        this.sslClientCertCheckChannelHandler = channelDependencies.get(ZuulDependencyKeys.sslClientCertCheckChannelHandlerProvider).get();


        this.sessionContextDecorator = channelDependencies.get(ZuulDependencyKeys.sessionCtxDecorator);
        this.requestCompleteHandler = channelDependencies.get(ZuulDependencyKeys.requestCompleteHandler);

        this.filterLoader = channelDependencies.get(ZuulDependencyKeys.filterLoader);
        this.filterUsageNotifier = channelDependencies.get(ZuulDependencyKeys.filterUsageNotifier);

        this.sourceAddressChannelHandler = new SourceAddressChannelHandler();
    }

    protected void storeChannel(Channel ch)
    {
        this.channels.add(ch);
        // Also add the ChannelConfig as an attribute on each channel. So interested filters/channel-handlers can introspect
        // and potentially act differently based on the config.
        ch.attr(ATTR_CHANNEL_CONFIG).set(channelConfig);
    }

  
   
    protected void addHttp1Handlers(ChannelPipeline pipeline)
    {
        pipeline.addLast(HTTP_CODEC_HANDLER_NAME, createHttpServerCodec());

        pipeline.addLast(new Http1ConnectionCloseHandler());
        pipeline.addLast("conn_expiry_handler",
                new Http1ConnectionExpiryHandler(maxRequestsPerConnection, maxRequestsPerConnectionInBrownout, connectionExpiry));
    }

    protected HttpServerCodec createHttpServerCodec()
    {
        return new HttpServerCodec(
                MAX_INITIAL_LINE_LENGTH.get(),
                MAX_HEADER_SIZE.get(),
                MAX_CHUNK_SIZE.get(),
                false
        );
    }
    
    protected void addHttpRelatedHandlers(ChannelPipeline pipeline)
    {
        //pipeline.addLast(new PassportStateHttpServerHandler.InboundHandler());
        //pipeline.addLast(new PassportStateHttpServerHandler.OutboundHandler());
        if (httpRequestReadTimeout > -1) {
            HttpRequestReadTimeoutHandler.addLast(pipeline, httpRequestReadTimeout, TimeUnit.MILLISECONDS, httpRequestReadTimeoutCounter);
        }
        pipeline.addLast(new HttpServerLifecycleChannelHandler.HttpServerLifecycleInboundChannelHandler());
        pipeline.addLast(new HttpServerLifecycleChannelHandler.HttpServerLifecycleOutboundChannelHandler());
        pipeline.addLast(new HttpBodySizeRecordingChannelHandler.InboundChannelHandler());
        pipeline.addLast(new HttpBodySizeRecordingChannelHandler.OutboundChannelHandler());
        //pipeline.addLast(httpMetricsHandler);
        //pipeline.addLast(perEventLoopRequestsMetricsHandler);

        //if (accessLogPublisher != null) {
        //    pipeline.addLast(new AccessLogChannelHandler.AccessLogInboundChannelHandler(accessLogPublisher));
        //    pipeline.addLast(new AccessLogChannelHandler.AccessLogOutboundChannelHandler());
        //}

        pipeline.addLast(stripInboundProxyHeadersHandler);

        if (rateLimitingChannelHandler != null) {
            pipeline.addLast(rateLimitingChannelHandler);
        }

        //pipeline.addLast(requestRejectedChannelHandler);
    }

    protected void addTimeoutHandlers(ChannelPipeline pipeline) {
        pipeline.addLast(new IdleStateHandler(0, 0, idleTimeout, TimeUnit.MILLISECONDS));
        pipeline.addLast(new CloseOnIdleStateHandler());
    }
    
    
    

    protected void addZuulHandlers(final ChannelPipeline pipeline)
    {
        pipeline.addLast("logger", nettyLogger);
        pipeline.addLast(new ClientRequestReceiver(sessionContextDecorator));
        //pipeline.addLast(passportLoggingHandler);
        addZuulFilterChainHandler(pipeline);
        pipeline.addLast(new ClientResponseWriter(requestCompleteHandler, registry));
    }

    
    
    protected void addZuulFilterChainHandler(final ChannelPipeline pipeline) {
    	
        final ZuulFilter<HttpResponseMessage, HttpResponseMessage>[] responseFilters = getFilters(
                new OutboundPassportStampingFilter(FILTERS_OUTBOUND_START),
                new OutboundPassportStampingFilter(FILTERS_OUTBOUND_END));

        // response filter chain
        final ZuulFilterChainRunner<HttpResponseMessage> responseFilterChain = getFilterChainRunner(responseFilters,
                filterUsageNotifier);

        // endpoint | response filter chain
        final FilterRunner<HttpRequestMessage, HttpResponseMessage> endPoint = getEndpointRunner(responseFilterChain,
                filterUsageNotifier, filterLoader);

        final ZuulFilter<HttpRequestMessage, HttpRequestMessage>[] requestFilters = getFilters(
                new InboundPassportStampingFilter(FILTERS_INBOUND_START),
                new InboundPassportStampingFilter(FILTERS_INBOUND_END));

        // request filter chain | end point | response filter chain
        final ZuulFilterChainRunner<HttpRequestMessage> requestFilterChain = getFilterChainRunner(requestFilters,
                filterUsageNotifier, endPoint);

        pipeline.addLast(new ZuulFilterChainHandler(requestFilterChain, responseFilterChain));
    }

    
    
    
    
    protected ZuulEndPointRunner getEndpointRunner(ZuulFilterChainRunner<HttpResponseMessage> responseFilterChain,
                                                   FilterUsageNotifier filterUsageNotifier, FilterLoader filterLoader) {
        return new ZuulEndPointRunner(filterUsageNotifier, filterLoader, responseFilterChain);
    }

    protected <T extends ZuulMessage> ZuulFilterChainRunner<T> getFilterChainRunner(ZuulFilter<T, T>[] filters,
                                                                                    FilterUsageNotifier filterUsageNotifier) {
        return new ZuulFilterChainRunner<>(filters, filterUsageNotifier);
    }

    protected <T extends ZuulMessage, R extends ZuulMessage> ZuulFilterChainRunner<T> getFilterChainRunner(ZuulFilter<T, T>[] filters,
                                                                                    FilterUsageNotifier filterUsageNotifier,
                                                                                    FilterRunner<T, R> filterRunner) {
        return new ZuulFilterChainRunner<>(filters, filterUsageNotifier, filterRunner);
    }

    public <T extends ZuulMessage> ZuulFilter<T, T> [] getFilters(final ZuulFilter start, final ZuulFilter stop) {
        final List<ZuulFilter> zuulFilters = filterLoader.getFiltersByType(start.filterType());
        final ZuulFilter[] filters = new ZuulFilter[zuulFilters.size() + 2];
        filters[0] = start;
        for (int i=1, j=0; i < filters.length && j < zuulFilters.size(); i++,j++) {
            filters[i] = zuulFilters.get(j);
        }
        filters[filters.length -1] = stop;
        return filters;
    }
}
