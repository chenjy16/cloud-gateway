package org.cloud.gateway.transport.webflux.filter;
import org.cloud.gateway.transport.webflux.plugin.before.GwServerWebExchangeDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


public class StreamWebFilter implements WebFilter {


    /**
     * @Desc:
     * @param       exchange
     * @param       chain
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:15
     *
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new GwServerWebExchangeDecorator(exchange));
    }
}
