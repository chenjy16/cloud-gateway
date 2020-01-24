/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.cloud.gateway.transport.webflux.plugin.function;
import org.cloud.gateway.transport.webflux.plugin.AbstractPlugin;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.cloud.gateway.common.constant.Constants;
import org.cloud.gateway.common.dto.convert.rule.DivideRuleHandle;
import org.cloud.gateway.common.dto.zk.RuleZkDTO;
import org.cloud.gateway.common.dto.zk.SelectorZkDTO;
import org.cloud.gateway.common.enums.PluginEnum;
import org.cloud.gateway.common.enums.PluginTypeEnum;
import org.cloud.gateway.common.utils.GSONUtils;
import org.cloud.gateway.cache.UpstreamCacheManager;
import org.cloud.gateway.cache.ZookeeperCacheManager;
import org.cloud.gateway.webflux.request.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.time.Duration;


public class RoutePlugin extends AbstractPlugin {


    private static final Logger LOGGER = LoggerFactory.getLogger(RoutePlugin.class);

    private final UpstreamCacheManager upstreamCacheManager;


    public RoutePlugin(final ZookeeperCacheManager zookeeperCacheManager, final UpstreamCacheManager upstreamCacheManager) {
        super(zookeeperCacheManager);
        this.upstreamCacheManager = upstreamCacheManager;
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final PluginChain chain, final SelectorZkDTO selector, final RuleZkDTO rule) {
        final RequestDTO requestDTO = exchange.getAttribute(Constants.REQUESTDTO);

        final DivideRuleHandle ruleHandle = GSONUtils.getInstance().fromJson(rule.getHandle(), DivideRuleHandle.class);




    }

    private Mono<Void> proxyRequest(ServerWebExchange exchange,PluginChain chain,String url,Integer timeout){

        ServerHttpResponse gatewayResp=exchange.getResponse();

        WebClient.RequestHeadersSpec<?> headersSpec=httpReqBuild(exchange,url);
        return headersSpec.exchange()
                .timeout(Duration.ofMillis(timeout)).doOnError(ex->{
                   /* if(ex instanceof TimeoutException){


                    }else{

                    } */
                }).flatMap(backendResponse->{
                    gatewayResp.setStatusCode(backendResponse.statusCode());
                    gatewayResp.getHeaders().putAll(backendResponse.headers().asHttpHeaders());
                    exchange.getAttributes().put("",backendResponse);
                    return chain.execute(exchange);
                });
    }

    WebClient webClient=WebClient.create();

    private WebClient.RequestHeadersSpec<?> httpReqBuild(ServerWebExchange exchange,String url){
        HttpMethod method=exchange.getRequest().getMethod();
        WebClient.RequestBodySpec bodySpec=this.webClient.method(method).uri(url).headers(httpHeaders->{
           httpHeaders.addAll(exchange.getRequest().getHeaders());
            httpHeaders.remove(httpHeaders.HOST);
        });
        WebClient.RequestHeadersSpec headersSpec;
        if(requiresIsNeedBody(method)){
            String bodyString=exchange.getAttribute("");
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
        return PluginEnum.DIVIDE.getName();
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
        return PluginEnum.DIVIDE.getCode();
    }

}
