package org.cloud.gateway.transport.webflux.plugin.function;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cloud.gateway.core.algorithm.LoadBalanceAlgorithm;
import org.cloud.gateway.core.algorithm.LoadBalanceFactory;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.ServerConfiguration;
import org.cloud.gateway.core.enums.PluginEnum;
import org.cloud.gateway.core.enums.PluginTypeEnum;
import org.cloud.gateway.core.strategy.RouteStrategyType;
import org.cloud.gateway.orchestration.internal.registry.GatewayOrchestrationFacade;
import org.cloud.gateway.orchestration.internal.registry.config.event.RouteChangedEvent;
import org.cloud.gateway.transport.webflux.plugin.AbstractPlugin;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.cloud.gateway.orchestration.reg.listener.DataChangedEvent.ChangedType.*;

@Slf4j
public class RoutePlugin extends AbstractPlugin {

    private Map<String, ClusterConfiguration> clusterConfigurationMap;

    private final WebClient webClient=WebClient.create();

    public RoutePlugin(final GatewayOrchestrationFacade gatewayOrchestrationFacade) {
        super(gatewayOrchestrationFacade);
        this.clusterConfigurationMap=gatewayOrchestrationFacade.getConfigService().loadRouteRule();
    }


    /**
     * @Desc:       路由配置变更
     * @param       routeChangedEvent
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/2/24 16:56
     *
     */
    @Subscribe
    public synchronized void changeConfiguration(final RouteChangedEvent routeChangedEvent) {
        switch (routeChangedEvent.getChangedType()) {
            case UPDATED:
            case ADDED:
                this.clusterConfigurationMap.put(routeChangedEvent.getClusterConfiguration().getId(),routeChangedEvent.getClusterConfiguration());
                break;
            case DELETED:
                this.clusterConfigurationMap.remove(routeChangedEvent.getClusterConfiguration().getId());
                break;
            default:
                break;
        }
    }



    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final PluginChain chain) {
        Optional<ClusterConfiguration>  clusterConfigurationOptional=RouteStrategyType.getDefaultRouteStrategyType().getRouteStrategy().match(clusterConfigurationMap.values().stream().collect(Collectors.toList()),exchange).stream().findFirst();
        if(clusterConfigurationOptional.isPresent()){
            LoadBalanceAlgorithm lb=LoadBalanceFactory.of(clusterConfigurationOptional.get().getLoadBalanceAlgorithm());
            final String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
            ServerConfiguration serverConfiguration=lb.select(clusterConfigurationOptional.get(),ip);
            return proxyRequest(exchange,chain,buildRealURL(serverConfiguration),3000);
        }
        return chain.execute(exchange);
    }



    private String buildRealURL(final ServerConfiguration serverConfiguration) {
        String protocol = serverConfiguration.getProtocol();
        if (StringUtils.isBlank(protocol)) {
            protocol = "http://";
        }
        return protocol + serverConfiguration.getAddr().trim();
    }



    /**
     * @Desc:       转发请求
     * @param       exchange
     * @param       chain
     * @param       url
     * @param       timeout
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:14
     *
     */
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



    /**
     * @Desc:       构建转发请求体
     * @param       exchange
     * @param       url
     * @return:     org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec<?>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:13
     *
     */
    private WebClient.RequestHeadersSpec<?> httpReqBuild(ServerWebExchange exchange,String url){
        HttpMethod method=exchange.getRequest().getMethod();
        WebClient.RequestBodySpec bodySpec=this.webClient.method(method).uri(url).headers(httpHeaders->{
           httpHeaders.addAll(exchange.getRequest().getHeaders());
           httpHeaders.remove(HttpHeaders.HOST);
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

    /**
     * @Desc:       请求方式method
     * @param       method
     * @return:     boolean
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:14
     *
     */
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
