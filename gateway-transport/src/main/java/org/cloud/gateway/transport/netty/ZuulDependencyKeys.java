package org.cloud.gateway.transport.netty;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.spectator.api.Registry;
import io.netty.channel.ChannelHandler;

import javax.xml.ws.Provider;


/**
 * Created by cjy on 2020/1/4.
 */
public class ZuulDependencyKeys {

    public static final ChannelConfigKey<AccessLogPublisher> accessLogPublisher = new ChannelConfigKey<>("accessLogPublisher");
    public static final ChannelConfigKey<EventLoopGroupMetrics> eventLoopGroupMetrics = new ChannelConfigKey<>("eventLoopGroupMetrics");
    public static final ChannelConfigKey<Registry> registry = new ChannelConfigKey<>("registry");
    public static final ChannelConfigKey<SessionContextDecorator> sessionCtxDecorator = new ChannelConfigKey<>("sessionCtxDecorator");
    public static final ChannelConfigKey<RequestCompleteHandler> requestCompleteHandler = new ChannelConfigKey<>("requestCompleteHandler");
    public static final ChannelConfigKey<BasicCounter> httpRequestReadTimeoutCounter = new ChannelConfigKey<>("httpRequestReadTimeoutCounter");
    public static final ChannelConfigKey<FilterLoader> filterLoader = new ChannelConfigKey<>("filterLoader");
    public static final ChannelConfigKey<FilterUsageNotifier> filterUsageNotifier = new ChannelConfigKey<>("filterUsageNotifier");
    public static final ChannelConfigKey<EurekaClient> discoveryClient = new ChannelConfigKey<>("discoveryClient");
    public static final ChannelConfigKey<ApplicationInfoManager> applicationInfoManager = new ChannelConfigKey<>("applicationInfoManager");
    public static final ChannelConfigKey<ServerStatusManager> serverStatusManager = new ChannelConfigKey<>("serverStatusManager");
    public static final ChannelConfigKey<Boolean> SSL_CLIENT_CERT_CHECK_REQUIRED = new ChannelConfigKey<>("requiresSslClientCertCheck", false);

    public static final ChannelConfigKey<Provider<ChannelHandler>> rateLimitingChannelHandlerProvider = new ChannelConfigKey<>("rateLimitingChannelHandlerProvider");
    public static final ChannelConfigKey<Provider<ChannelHandler>> sslClientCertCheckChannelHandlerProvider = new ChannelConfigKey<>("sslClientCertCheckChannelHandlerProvider");
    public static final ChannelConfigKey<PushConnectionRegistry> pushConnectionRegistry = new ChannelConfigKey<>("pushConnectionRegistry");
}
