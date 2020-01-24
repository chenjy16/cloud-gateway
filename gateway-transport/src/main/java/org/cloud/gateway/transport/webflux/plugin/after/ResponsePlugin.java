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

package org.cloud.gateway.transport.webflux.plugin.after;

import org.cloud.gateway.common.constant.Constants;
import org.cloud.gateway.common.enums.PluginTypeEnum;
import org.cloud.gateway.common.enums.RpcTypeEnum;
import org.cloud.gateway.common.exception.SoulException;
import org.cloud.gateway.common.result.SoulResult;
import org.cloud.gateway.common.utils.JsonUtils;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.cloud.gateway.web.plugin.SoulPlugin;
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

            ClientResponse clientResponse = exchange.getAttribute("");

            if(Objects.nonNull(clientResponse)){
                clientResponse.headers().asHttpHeaders().forEach((k,v)->{
                    response.getHeaders().put(k,v);
                });
                return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers()));
            }
            return response.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("".getBytes(StandardCharsets.UTF_8))));
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
        return "";
    }

}
