package org.cloud.gateway.transport.webflux.handler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public final class GatewayHandlerMapping extends AbstractHandlerMapping {

    private final GatewayWebHandler gatewayWebHandler;


    public GatewayHandlerMapping(final GatewayWebHandler gatewayWebHandler) {
        this.gatewayWebHandler = gatewayWebHandler;
        setOrder(1);
    }

    @Override
    protected Mono<?> getHandlerInternal(final ServerWebExchange exchange) {
        return Mono.just(gatewayWebHandler);
    }

    @Override
    protected CorsConfiguration getCorsConfiguration(final Object handler, final ServerWebExchange exchange) {
        return super.getCorsConfiguration(handler, exchange);
    }

}
