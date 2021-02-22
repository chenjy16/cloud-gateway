package org.cloud.gateway.transport.webflux.plugin;


import org.cloud.gateway.core.enums.PluginTypeEnum;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public interface Plugin {


    /**
     * @Desc:       插件执行逻辑
     * @param       exchange
     * @param       chain
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:16
     *
     */
    Mono<Void> execute(ServerWebExchange exchange, PluginChain chain);


    PluginTypeEnum pluginType();


    int getOrder();


    String named();


    default Boolean skip(ServerWebExchange exchange) {
        return false;
    }

}

