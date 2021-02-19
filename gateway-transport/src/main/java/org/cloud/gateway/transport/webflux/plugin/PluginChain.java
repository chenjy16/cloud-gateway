package org.cloud.gateway.transport.webflux.plugin;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public interface PluginChain {


    Mono<Void> execute(ServerWebExchange exchange);

}
