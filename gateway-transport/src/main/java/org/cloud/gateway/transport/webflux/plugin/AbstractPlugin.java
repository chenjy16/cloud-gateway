package org.cloud.gateway.transport.webflux.plugin;
import com.google.common.eventbus.Subscribe;
import org.cloud.gateway.core.rule.PluginRule;
import org.cloud.gateway.orchestration.internal.registry.config.event.PluginChangedEvent;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


public abstract class AbstractPlugin implements Plugin {

    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, PluginChain chain);


    private PluginRule pluginRule;


    @Subscribe
    public synchronized void renew(final PluginChangedEvent pluginChangedEvent) {
        pluginRule=null;
    }


    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final PluginChain chain) {


        return chain.execute(exchange);
    }


}
