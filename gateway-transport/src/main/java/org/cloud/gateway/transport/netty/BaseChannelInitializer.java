package org.cloud.gateway.transport.netty;
import com.netflix.config.CachedDynamicIntProperty;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.spectator.api.Registry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseChannelInitializer extends ChannelInitializer<Channel> {

    public static final String HTTP_CODEC_HANDLER_NAME = "codec";
    public static final AttributeKey<ChannelConfig> ATTR_CHANNEL_CONFIG = AttributeKey.newInstance("channel_config");


    public static final CachedDynamicIntProperty MAX_INITIAL_LINE_LENGTH = new CachedDynamicIntProperty("server.http.decoder.maxInitialLineLength", 16384);
    public static final CachedDynamicIntProperty MAX_HEADER_SIZE = new CachedDynamicIntProperty("server.http.decoder.maxHeaderSize", 32768);
    public static final CachedDynamicIntProperty MAX_CHUNK_SIZE = new CachedDynamicIntProperty("server.http.decoder.maxChunkSize", 32768);


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


    protected final Registry registry;


    protected final MaxInboundConnectionsHandler maxConnectionsHandler;
    protected final boolean withProxyProtocol;
    protected final StripUntrustedProxyHeadersHandler stripInboundProxyHeadersHandler;
    // TODO
    protected final SessionContextDecorator sessionContextDecorator;
    protected final RequestCompleteHandler requestCompleteHandler;
    protected final BasicCounter httpRequestReadTimeoutCounter;
    protected final FilterLoader filterLoader;
    protected final FilterUsageNotifier filterUsageNotifier;
    protected final SourceAddressChannelHandler sourceAddressChannelHandler;



    /** A collection of all the active channels that we can use to things like graceful shutdown */
    protected final ChannelGroup channels;


    protected BaseChannelInitializer(
            String metricId,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this(-1, metricId, channelConfig, channelDependencies, channels);
    }

    @Deprecated
    protected BaseChannelInitializer(
            int port,
            ChannelConfig channelConfig,
            ChannelConfig channelDependencies,
            ChannelGroup channels) {
        this(port, String.valueOf(port), channelConfig, channelDependencies, channels);
    }

    private BaseChannelInitializer(
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
        this.registry = channelDependencies.get(ZuulDependencyKeys.registry);
        this.maxConnections = channelConfig.get(CommonChannelConfigKeys.maxConnections);
        this.maxConnectionsHandler = new MaxInboundConnectionsHandler(maxConnections);
        this.maxRequestsPerConnection = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnection);
        this.maxRequestsPerConnectionInBrownout = channelConfig.get(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout);
        this.connectionExpiry = channelConfig.get(CommonChannelConfigKeys.connectionExpiry);
        this.connCloseDelay = channelConfig.get(CommonChannelConfigKeys.connCloseDelay);
        StripUntrustedProxyHeadersHandler.AllowWhen allowProxyHeadersWhen = channelConfig.get(CommonChannelConfigKeys.allowProxyHeadersWhen);
        this.stripInboundProxyHeadersHandler = new StripUntrustedProxyHeadersHandler(allowProxyHeadersWhen);
        this.sessionContextDecorator = channelDependencies.get(ZuulDependencyKeys.sessionCtxDecorator);
        this.requestCompleteHandler = channelDependencies.get(ZuulDependencyKeys.requestCompleteHandler);
        this.httpRequestReadTimeoutCounter = channelDependencies.get(ZuulDependencyKeys.httpRequestReadTimeoutCounter);
        this.filterLoader = channelDependencies.get(ZuulDependencyKeys.filterLoader);
        this.filterUsageNotifier = channelDependencies.get(ZuulDependencyKeys.filterUsageNotifier);
        this.sourceAddressChannelHandler = new SourceAddressChannelHandler();
    }

}
