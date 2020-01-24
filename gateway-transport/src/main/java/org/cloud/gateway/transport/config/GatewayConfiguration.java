package org.cloud.gateway.transport.config;

import org.cloud.gateway.cache.UpstreamCacheManager;
import org.cloud.gateway.cache.ZookeeperCacheManager;
import org.cloud.gateway.web.disruptor.publisher.SoulEventPublisher;
import org.cloud.gateway.transport.webflux.filter.StreamWebFilter;
import org.cloud.gateway.transport.webflux.handler.GatewayHandlerMapping;
import org.cloud.gateway.transport.webflux.handler.GatewayWebHandler;

import org.cloud.gateway.transport.webflux.plugin.Plugin;

import org.cloud.gateway.transport.webflux.plugin.after.ResponsePlugin;

import org.cloud.gateway.transport.webflux.plugin.before.CachedBodyPlugin;
import org.cloud.gateway.transport.webflux.plugin.function.RoutePlugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;

import java.util.List;
import java.util.stream.Collectors;


@Configuration
@ComponentScan("org.cloud.gateway")
public class GatewayConfiguration {

    private final ZookeeperCacheManager zookeeperCacheManager;



    private final UpstreamCacheManager upstreamCacheManager;


    @Autowired(required = false)
    public GatewayConfiguration(final ZookeeperCacheManager zookeeperCacheManager,
                                final SoulEventPublisher soulEventPublisher,
                                final UpstreamCacheManager upstreamCacheManager) {
        this.zookeeperCacheManager = zookeeperCacheManager;
        this.upstreamCacheManager = upstreamCacheManager;
    }







    @Bean
    public Plugin routePlugin() {
        return new RoutePlugin(zookeeperCacheManager, upstreamCacheManager);
    }


    @Bean
    public Plugin responsePlugin() {
        return new ResponsePlugin();
    }


    @Bean
    public WebHandler gatewayWebHandler(final List<Plugin> list) {
        final List<Plugin> plugins = list.stream()
                .sorted((m, n) -> {
                    if (m.pluginType().equals(n.pluginType())) {
                        return m.getOrder() - n.getOrder();
                    } else {
                        return m.pluginType().getName().compareTo(n.pluginType().getName());
                    }
                }).collect(Collectors.toList());
        return new GatewayWebHandler(plugins);
    }



    @Bean
    public GatewayHandlerMapping gatewayHandlerMapping(final GatewayWebHandler gatewayWebHandler) {
        return new GatewayHandlerMapping(gatewayWebHandler);
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter streamWebFilter() {
        return new StreamWebFilter();
    }

    @Bean
    public Plugin cachedBodyPlugin() {
        return new CachedBodyPlugin();
    }


}
