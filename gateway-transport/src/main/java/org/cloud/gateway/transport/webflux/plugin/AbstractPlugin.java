package org.cloud.gateway.transport.webflux.plugin;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.cloud.gateway.core.rule.PluginRule;
import org.cloud.gateway.orchestration.internal.registry.GatewayOrchestrationFacade;
import org.cloud.gateway.orchestration.internal.registry.config.event.PluginChangedEvent;
import org.cloud.gateway.orchestration.internal.registry.eventbus.GatewayOrchestrationEventBus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Objects;


public abstract class AbstractPlugin implements Plugin {

    private final EventBus eventBus = GatewayOrchestrationEventBus.getInstance();

    private  PluginRule pluginRule;

    public AbstractPlugin(GatewayOrchestrationFacade gatewayOrchestrationFacade) {
        eventBus.register(this);
        this.pluginRule=gatewayOrchestrationFacade.getConfigService().loadPluginRule();
    }


    /**
     * @Desc:       插件配置变更
     * @param       pluginChangedEvent
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/2/24 16:57
     *
     */
    @Subscribe
    public synchronized void renew(final PluginChangedEvent pluginChangedEvent) {
        this.pluginRule=new PluginRule("",pluginChangedEvent.getPluginConfigurationMap()) ;
    }



    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final PluginChain chain) {
        PluginConfiguration pluginConfiguration=pluginRule.getPluginConfigurationMap().get(named());
        if (!(skip(exchange) || Objects.isNull(pluginConfiguration)   || !pluginConfiguration.getEnabled())) {
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
