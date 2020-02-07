package org.cloud.gateway.transport.netty;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.ClientAuth;


public class SampleServerStartup extends BaseServerStartup {

    enum ServerType {
        HTTP,
        HTTP2,
        HTTP_MUTUAL_TLS,
        WEBSOCKET
    }

    private static final String[] WWW_PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};
    private static final ServerType SERVER_TYPE = ServerType.HTTP;
    private final PushConnectionRegistry pushConnectionRegistry;
    private final SamplePushMessageSenderInitializer pushSenderInitializer;

 
    public SampleServerStartup(ServerStatusManager serverStatusManager, FilterLoader filterLoader,
                               SessionContextDecorator sessionCtxDecorator, FilterUsageNotifier usageNotifier,
                               RequestCompleteHandler reqCompleteHandler, Registry registry,
                               DirectMemoryMonitor directMemoryMonitor, EventLoopGroupMetrics eventLoopGroupMetrics,
                               EurekaClient discoveryClient, ApplicationInfoManager applicationInfoManager,
                               AccessLogPublisher accessLogPublisher, PushConnectionRegistry pushConnectionRegistry,
                               SamplePushMessageSenderInitializer pushSenderInitializer) {
        super(serverStatusManager, filterLoader, sessionCtxDecorator, usageNotifier, reqCompleteHandler, registry,
                directMemoryMonitor, eventLoopGroupMetrics, discoveryClient, applicationInfoManager,
                accessLogPublisher);
        this.pushConnectionRegistry = pushConnectionRegistry;
        this.pushSenderInitializer = pushSenderInitializer;
    }

    @Override
    protected Map<SocketAddress, ChannelInitializer<?>> chooseAddrsAndChannels(ChannelGroup clientChannels) {
        Map<SocketAddress, ChannelInitializer<?>> addrsToChannels = new HashMap<>();
        SocketAddress sockAddr;
        String metricId;
        {
            @Deprecated
            int port = new DynamicIntProperty("zuul.server.port.main", 7001).get();
            sockAddr = new SocketAddressProperty("zuul.server.addr.main", "=" + port).getValue();
            if (sockAddr instanceof InetSocketAddress) {
                metricId = String.valueOf(((InetSocketAddress) sockAddr).getPort());
            } else {
                // Just pick something.   This would likely be a UDS addr or a LocalChannel addr.
                metricId = sockAddr.toString();
            }
        }

        SocketAddress pushSockAddr;
        {
            int pushPort = new DynamicIntProperty("zuul.server.port.http.push", 7008).get();
            pushSockAddr = new SocketAddressProperty(
                    "zuul.server.addr.http.push", "="  + pushPort).getValue();
        }

        String mainListenAddressName = "main";
        ServerSslConfig sslConfig;
        ChannelConfig channelConfig = defaultChannelConfig(mainListenAddressName);
        ChannelConfig channelDependencies = defaultChannelDependencies(mainListenAddressName);

        /* These settings may need to be tweaked depending if you're running behind an ELB HTTP listener, TCP listener,
         * or directly on the internet.
         */
        switch (SERVER_TYPE) {
            /* The below settings can be used when running behind an ELB HTTP listener that terminates SSL for you
             * and passes XFF headers.
             */
            case HTTP:
                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.ALWAYS);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, false);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, false);

                addrsToChannels.put(
                        sockAddr,
                        new ZuulServerChannelInitializer(
                                metricId, channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             */
            case HTTP2:
             
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             *
             * Can be tested using certs in resources directory:
             *  curl https://localhost:7001/test -vk --cert src/main/resources/ssl/client.cert:zuul123 --key src/main/resources/ssl/client.key
             */
            case HTTP_MUTUAL_TLS:
   
                break;

            /* Settings to be used when running behind an ELB TCP listener with proxy protocol as a Push notification
             * server using WebSockets */
            case WEBSOCKET:
    
                break;

    
        }

        return Collections.unmodifiableMap(addrsToChannels);
    }

    private File loadFromResources(String s) {
        return new File(ClassLoader.getSystemResource("ssl/" + s).getFile());
    }
}
