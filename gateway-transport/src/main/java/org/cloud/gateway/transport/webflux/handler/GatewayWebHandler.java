package org.cloud.gateway.transport.webflux.handler;
import org.cloud.gateway.transport.webflux.plugin.Plugin;
import org.cloud.gateway.transport.webflux.plugin.PluginChain;
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
     * @Desc:       web server exchange 扩展
     * @param       exchange
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:16
     *
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

        DefaultPluginChain(final List<Plugin> plugins) {
            this.plugins = plugins;
        }

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
