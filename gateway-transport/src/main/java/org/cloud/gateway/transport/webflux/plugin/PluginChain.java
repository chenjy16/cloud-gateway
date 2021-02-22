package org.cloud.gateway.transport.webflux.plugin;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public interface PluginChain {


    /**
     * @Desc:
     * @param       exchange
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:17
     *
     */
    Mono<Void> execute(ServerWebExchange exchange);

}
