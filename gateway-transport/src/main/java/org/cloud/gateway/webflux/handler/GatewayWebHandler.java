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

package org.cloud.gateway.webflux.handler;

import org.cloud.gateway.webflux.plugin.Plugin;
import org.cloud.gateway.webflux.plugin.PluginChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import java.util.List;


public final class GatewayWebHandler implements WebHandler {

    private List<Plugin> plugins;


    public GatewayWebHandler(final List<Plugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * Handle the web server exchange.
     *
     * @param exchange the current server exchange
     * @return {@code Mono<Void>} to indicate when request handling is complete
     */
    @Override
    public Mono<Void> handle(final ServerWebExchange exchange) {
        return new DefaultPluginChain(plugins)
                .execute(exchange)
                .doOnError(Throwable::printStackTrace);
    }

    private static class DefaultPluginChain implements PluginChain {

        private int index;

        private final List<Plugin> plugins;

        /**
         * Instantiates a new Default soul plugin chain.
         *
         * @param plugins the plugins
         */
        DefaultPluginChain(final List<Plugin> plugins) {
            this.plugins = plugins;
        }

        /**
         * Delegate to the next {@code WebFilter} in the chain.
         *
         * @param exchange the current server exchange
         * @return {@code Mono<Void>} to indicate when request handling is complete
         */
        @Override
        public Mono<Void> execute(final ServerWebExchange exchange) {
            if (this.index < plugins.size()) {
                Plugin plugin = plugins.get(this.index++);
                return plugin.execute(exchange, this);
            } else {
                return Mono.empty();
            }
        }
    }
}
