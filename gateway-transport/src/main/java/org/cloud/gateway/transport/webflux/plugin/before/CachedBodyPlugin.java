package org.cloud.gateway.transport.webflux.plugin.before;
import org.cloud.gateway.common.enums.PluginTypeEnum;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.awt.image.DataBuffer;


public class CachedBodyPlugin implements Plugin {


    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain chain) {
       DataBuffer body= exchange.getAttributeOrDefault("Cached_req_body_attr", null);
       if(body!=null){

           return chain.execute(exchange);
       }

       return ServerWebExchangeUtils.cacheRequestBody(exchange,(serverHttpRequest)->{
           if(serverHttpRequest==exchange.getRequest()){
               return chain.execute(exchange);
           }
           return chain.execute(exchange.mutate().request(serverHttpRequest).build());
       });

    }

    @Override
    public PluginTypeEnum pluginType() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String named() {
        return null;
    }
}
