package org.cloud.gateway.transport.webflux.plugin.function;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.cloud.gateway.core.enums.PluginEnum;
import org.cloud.gateway.core.enums.PluginTypeEnum;
import org.cloud.gateway.core.rule.RouteRule;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent;
import org.cloud.gateway.transport.webflux.plugin.AbstractPlugin;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
public class RoutePlugin extends AbstractPlugin {

    private RouteRule routeRule;

    private WebClient webClient=WebClient.create();


    @Subscribe
    public synchronized void renew(final DataChangedEvent dataChangedEvent) {
        routeRule=null;
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final PluginChain chain) {
        return proxyRequest(exchange,chain,"",3000);
    }

    private Mono<Void> proxyRequest(ServerWebExchange exchange,PluginChain chain,String url,Integer timeout){
        ServerHttpResponse gatewayResp=exchange.getResponse();
        WebClient.RequestHeadersSpec<?> headersSpec=httpReqBuild(exchange,url);
        return headersSpec.exchange()
                .timeout(Duration.ofMillis(timeout)).doOnError(ex->{
                   log.error("后的服务异常：{}",ex);
                }).flatMap(clientResponse->{
                    gatewayResp.setStatusCode(clientResponse.statusCode());
                    gatewayResp.getHeaders().putAll(clientResponse.headers().asHttpHeaders());
                    exchange.getAttributes().put("clientResponse",clientResponse);
                    return chain.execute(exchange);
                });
    }


    private WebClient.RequestHeadersSpec<?> httpReqBuild(ServerWebExchange exchange,String url){
        HttpMethod method=exchange.getRequest().getMethod();
        WebClient.RequestBodySpec bodySpec=this.webClient.method(method).uri(url).headers(httpHeaders->{
           httpHeaders.addAll(exchange.getRequest().getHeaders());
            //httpHeaders.remove(httpHeaders.HOST);
        });
        WebClient.RequestHeadersSpec headersSpec;
        if(requiresIsNeedBody(method)){
            String bodyString=exchange.getAttribute("Cached_req_body_attr");
            headersSpec=bodySpec.syncBody(bodyString);
        }else{
            headersSpec=bodySpec;
        }

        return headersSpec;
    }


    private boolean requiresIsNeedBody(HttpMethod method){
        switch(method){
            case PUT:
            case POST:
            case PATCH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String named() {
        return PluginEnum.ROUTE.getName();
    }


    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        return null;
    }


    @Override
    public PluginTypeEnum pluginType() {
        return PluginTypeEnum.FUNCTION;
    }

    @Override
    public int getOrder() {
        return PluginEnum.ROUTE.getCode();
    }

}
