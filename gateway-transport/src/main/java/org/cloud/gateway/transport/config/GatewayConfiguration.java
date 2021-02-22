package org.cloud.gateway.transport.config;
import com.google.common.collect.Lists;
import org.cloud.gateway.orchestration.config.OrchestrationConfiguration;
import org.cloud.gateway.orchestration.internal.registry.GatewayOrchestrationFacade;
import org.cloud.gateway.orchestration.reg.api.RegistryCenterConfiguration;
import org.cloud.gateway.transport.webflux.filter.StreamWebFilter;
import org.cloud.gateway.transport.webflux.handler.GatewayHandlerMapping;
import org.cloud.gateway.transport.webflux.handler.GatewayWebHandler;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.cloud.gateway.transport.webflux.plugin.after.ResponsePlugin;
import org.cloud.gateway.transport.webflux.plugin.before.CachedBodyPlugin;
import org.cloud.gateway.transport.webflux.plugin.function.RoutePlugin;
import org.springframework.beans.factory.annotation.Qualifier;
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


    @Bean
    public GatewayOrchestrationFacade gatewayOrchestrationFacade(@Qualifier OrchestrationConfiguration orchestrationConfiguration) {
        GatewayOrchestrationFacade gatewayOrchestrationFacade =new GatewayOrchestrationFacade(orchestrationConfiguration);
        gatewayOrchestrationFacade.init();
        return gatewayOrchestrationFacade;
    }

    @Bean
    public OrchestrationConfiguration orchestrationConfiguration(@Qualifier RegistryCenterConfiguration registryCenterConfiguration) {
        return new OrchestrationConfiguration("",registryCenterConfiguration,false);
    }

    @Bean
    public RegistryCenterConfiguration registryCenterConfiguration() {
        return new RegistryCenterConfiguration();
    }


    @Bean
    public Plugin routePlugin(@Qualifier GatewayOrchestrationFacade gatewayOrchestrationFacade) {
        return new RoutePlugin(gatewayOrchestrationFacade);
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
