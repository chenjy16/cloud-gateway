package org.cloud.gateway.transport.netty;
import com.google.errorprone.annotations.ForOverride;
import com.netflix.config.ChainedDynamicProperty;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.spectator.api.Registry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.util.Map;

public abstract class BaseServerStartup   {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseServerStartup.class);

    protected final ServerStatusManager serverStatusManager;
    protected final Registry registry;

    protected final SessionContextDecorator sessionCtxDecorator;
    protected final RequestCompleteHandler reqCompleteHandler;
    protected final FilterLoader filterLoader;
    protected final FilterUsageNotifier usageNotifier;

    private Map<? extends SocketAddress, ? extends ChannelInitializer<?>> addrsToChannelInitializers;
    private ClientConnectionsShutdown clientConnectionsShutdown;
    private Server server;



    public BaseServerStartup(ServerStatusManager serverStatusManager, FilterLoader filterLoader,
                             SessionContextDecorator sessionCtxDecorator, FilterUsageNotifier usageNotifier,
                             RequestCompleteHandler reqCompleteHandler, Registry registry)
    {
        this.serverStatusManager = serverStatusManager;
        this.registry = registry;
        this.sessionCtxDecorator = sessionCtxDecorator;
        this.reqCompleteHandler = reqCompleteHandler;
        this.filterLoader = filterLoader;
        this.usageNotifier = usageNotifier;
    }

    public Server server()
    {
        return server;
    }

    
    
    @PostConstruct
    public void init() throws Exception
    {
        ChannelGroup clientChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        clientConnectionsShutdown = new ClientConnectionsShutdown(clientChannels,
                GlobalEventExecutor.INSTANCE);

        addrsToChannelInitializers = chooseAddrsAndChannels(clientChannels);



        server = new Server(
                serverStatusManager,
                addrsToChannelInitializers,
                clientConnectionsShutdown,
                new DefaultEventLoopConfig());
    }


    @Deprecated
    protected Map<Integer, ChannelInitializer> choosePortsAndChannels(ChannelGroup clientChannels) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @ForOverride
    protected Map<SocketAddress, ChannelInitializer<?>> chooseAddrsAndChannels(ChannelGroup clientChannels) {

        @SuppressWarnings("unchecked") // Channel init map has the wrong generics and we can't fix without api breakage.
                Map<Integer, ChannelInitializer<?>> portMap =
                (Map<Integer, ChannelInitializer<?>>) (Map) choosePortsAndChannels(clientChannels);

        return Server.convertPortMap(portMap);
    }


    protected ChannelConfig defaultChannelDependencies(String listenAddressName) {
        ChannelConfig channelDependencies = new ChannelConfig();
        addChannelDependencies(channelDependencies, listenAddressName);
        return channelDependencies;
    }

    protected void addChannelDependencies(
            ChannelConfig channelDeps,
            @SuppressWarnings("unused") String listenAddressName) { // listenAddressName is used by subclasses
    	
        channelDeps.set(ZuulDependencyKeys.registry, registry);
        channelDeps.set(ZuulDependencyKeys.serverStatusManager, serverStatusManager);
        channelDeps.set(ZuulDependencyKeys.sessionCtxDecorator, sessionCtxDecorator);
        channelDeps.set(ZuulDependencyKeys.requestCompleteHandler, reqCompleteHandler);
        final BasicCounter httpRequestReadTimeoutCounter =  new BasicCounter(MonitorConfig.builder("server.http.request.read.timeout").build());
        DefaultMonitorRegistry.getInstance().register(httpRequestReadTimeoutCounter);
        channelDeps.set(ZuulDependencyKeys.httpRequestReadTimeoutCounter, httpRequestReadTimeoutCounter);
        channelDeps.set(ZuulDependencyKeys.filterLoader, filterLoader);
        channelDeps.set(ZuulDependencyKeys.filterUsageNotifier, usageNotifier);
    }

    /**
     * First looks for a property specific to the named listen address of the form -
     * "server.${addrName}.${propertySuffix}". If none found, then looks for a server-wide property of the form -
     * "server.${propertySuffix}".  If that is also not found, then returns the specified default value.
     *
     * @param listenAddressName
     * @param propertySuffix
     * @param defaultValue
     * @return
     */
    public static int chooseIntChannelProperty(String listenAddressName, String propertySuffix, int defaultValue) {
        String globalPropertyName = "server." + propertySuffix;
        String listenAddressPropertyName = "server." + listenAddressName + "." + propertySuffix;
        Integer value = new DynamicIntProperty(listenAddressPropertyName, -999).get();
        if (value == -999) {
            value = new DynamicIntProperty(globalPropertyName, -999).get();
            if (value == -999) {
                value = defaultValue;
            }
        }
        return value;
    }

    public static boolean chooseBooleanChannelProperty(
            String listenAddressName, String propertySuffix, boolean defaultValue) {
        String globalPropertyName = "server." + propertySuffix;
        String listenAddressPropertyName = "server." + listenAddressName + "." + propertySuffix;

        Boolean value = new ChainedDynamicProperty.DynamicBooleanPropertyThatSupportsNull(
                listenAddressPropertyName, null).get();
        if (value == null) {
            value = new DynamicBooleanProperty(globalPropertyName, defaultValue).getDynamicProperty().getBoolean();
            if (value == null) {
                value = defaultValue;
            }
        }
        return value;
    }

    
    
    public static ChannelConfig defaultChannelConfig(String listenAddressName) {
    	
        ChannelConfig config = new ChannelConfig();

        config.add(new ChannelConfigValue<>(
                CommonChannelConfigKeys.maxConnections,
                chooseIntChannelProperty(
                        listenAddressName, "connection.max", CommonChannelConfigKeys.maxConnections.defaultValue())));
        
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.maxRequestsPerConnection,
                chooseIntChannelProperty(listenAddressName, "connection.max.requests", 20000)));
        
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout,
                chooseIntChannelProperty(
                        listenAddressName,
                        "connection.max.requests.brownout",
                        CommonChannelConfigKeys.maxRequestsPerConnectionInBrownout.defaultValue())));
        
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.connectionExpiry,
                chooseIntChannelProperty(
                        listenAddressName,
                        "connection.expiry",
                        CommonChannelConfigKeys.connectionExpiry.defaultValue())));
        
        config.add(new ChannelConfigValue<>(
                CommonChannelConfigKeys.httpRequestReadTimeout,
                chooseIntChannelProperty(
                        listenAddressName,
                        "http.request.read.timeout",
                        CommonChannelConfigKeys.httpRequestReadTimeout.defaultValue())));
        

        int connectionIdleTimeout = chooseIntChannelProperty(
                listenAddressName, "connection.idle.timeout",
                CommonChannelConfigKeys.idleTimeout.defaultValue());
        
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.idleTimeout, connectionIdleTimeout));
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.serverTimeout, new ServerTimeout(connectionIdleTimeout)));

        // For security, default to NEVER allowing XFF/Proxy headers from client.
        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER));

        config.set(CommonChannelConfigKeys.withProxyProtocol, true);
        config.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);

        config.add(new ChannelConfigValue<>(CommonChannelConfigKeys.connCloseDelay,
                chooseIntChannelProperty(
                        listenAddressName,
                        "connection.close.delay",
                        CommonChannelConfigKeys.connCloseDelay.defaultValue())));

        return config;
    }

}

