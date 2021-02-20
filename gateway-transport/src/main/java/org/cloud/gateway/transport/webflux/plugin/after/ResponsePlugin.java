package org.cloud.gateway.transport.webflux.plugin.after;
import org.cloud.gateway.core.enums.PluginTypeEnum;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ResponsePlugin implements Plugin {


    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final PluginChain chain) {

        return chain.execute(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();
            ClientResponse clientResponse = exchange.getAttribute("clientResponse");

            if(Objects.nonNull(clientResponse)){
                clientResponse.headers().asHttpHeaders().forEach((k,v)->{
                    response.getHeaders().put(k,v);
                });
                return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers()));
            }
            return response.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("后端服务响应异常".getBytes(StandardCharsets.UTF_8))));
        }));

    }


    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.LAST;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }


    @Override
    public String named() {
        return "GatewayResponse";
    }

}
