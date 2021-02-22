package org.cloud.gateway.transport.webflux.plugin;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.cloud.gateway.core.rule.PluginRule;
import org.cloud.gateway.orchestration.internal.registry.GatewayOrchestrationFacade;
import org.cloud.gateway.orchestration.internal.registry.config.event.PluginChangedEvent;
import org.cloud.gateway.orchestration.internal.registry.eventbus.GatewayOrchestrationEventBus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.Objects;


public abstract class AbstractPlugin implements Plugin {

    private final EventBus eventBus = GatewayOrchestrationEventBus.getInstance();

    private  Map<String, PluginRule> pluginRuleMap;

    public AbstractPlugin(GatewayOrchestrationFacade gatewayOrchestrationFacade) {
        pluginRuleMap=null;
        eventBus.register(this);
    }



    @Subscribe
    public synchronized void renew(final PluginChangedEvent pluginChangedEvent) {
        pluginRuleMap=pluginChangedEvent.getPluginRuleMap();
    }



    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final PluginChain chain) {
        PluginRule pluginRule=pluginRuleMap.get(named());
        if (!(skip(exchange) || Objects.isNull(pluginRule)   || !pluginRule.getEnabled())) {
            return doExecute(exchange, chain);
        }
        return chain.execute(exchange);
    }


    /**
     * @Desc:
     * @param       exchange
     * @param       chain
     * @return:     reactor.core.publisher.Mono<java.lang.Void>
     * @author:     chenjianyu944
     * @Date:       2021/2/22 13:14
     *
     */
    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, PluginChain chain);


}
