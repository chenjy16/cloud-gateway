package org.cloud.gateway.transport.webflux.plugin.before;
import org.cloud.gateway.core.enums.PluginEnum;
import org.cloud.gateway.core.enums.PluginTypeEnum;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.cloud.gateway.transport.webflux.utils.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.awt.image.DataBuffer;
import java.util.Objects;


public class CachedBodyPlugin implements Plugin {


    /**
     * @Desc:       缓存请求体
     * @param       exchange
     * @param       chain
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/20 15:24
     *
     */
    @Override
    public Mono<Void> execute(ServerWebExchange exchange, PluginChain chain) {
       DataBuffer body= exchange.getAttributeOrDefault("Cached_req_body_attr", null);
       if(Objects.nonNull(body)){
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
        return PluginTypeEnum.BEFORE;
    }

    @Override
    public int getOrder() {
        return PluginEnum.CACHEDBODY.getCode();
    }

    @Override
    public String named() {return PluginEnum.CACHEDBODY.getName();}
}
