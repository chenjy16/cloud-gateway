package org.cloud.gateway.transport.webflux.plugin;
import com.google.common.eventbus.Subscribe;
import org.cloud.gateway.core.rule.PluginRule;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;




public abstract class AbstractPlugin implements Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlugin.class);
    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, PluginChain chain);


    private PluginRule pluginRule;


    @Subscribe
    public synchronized void renew(final DataChangedEvent dataChangedEvent) {
        pluginRule=null;
    }


    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final PluginChain chain) {

        return chain.execute(exchange);
    }


}
