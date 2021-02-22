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

    /**
     * @Desc:
     * @param       exchange
     * @return:     reactor.core.publisher.Mono<?>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:16
     *
     */
    @Override
    protected Mono<?> getHandlerInternal(final ServerWebExchange exchange) {
        return Mono.just(gatewayWebHandler);
    }


    /**
     * @Desc:
     * @param       handler
     * @param       exchange
     * @return:     org.springframework.web.cors.CorsConfiguration
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:16
     *
     */
    @Override
    protected CorsConfiguration getCorsConfiguration(final Object handler, final ServerWebExchange exchange) {
        return super.getCorsConfiguration(handler, exchange);
    }

}
