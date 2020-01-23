package org.cloud.gateway.webflux.filter;
import org.cloud.gateway.webflux.plugin.before.GwServerWebExchangeDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


public class StreamWebFilter implements WebFilter {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new GwServerWebExchangeDecorator(exchange));
    }
}
