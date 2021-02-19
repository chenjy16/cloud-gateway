package org.cloud.gateway.transport.webflux.plugin;

import org.cloud.gateway.common.enums.PluginTypeEnum;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public interface Plugin {


    Mono<Void> execute(ServerWebExchange exchange, PluginChain chain);


    PluginTypeEnum pluginType();


    int getOrder();


    String named();


    default Boolean skip(ServerWebExchange exchange) {
        return false;
    }

}

