package org.cloud.gateway.orchestration.internal.registry.config.listener;
import org.cloud.gateway.orchestration.internal.registry.config.event.PluginChangedEvent;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.internal.registry.listener.PostGatewayOrchestrationEventListener;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent;



public final class PluginChangedListener extends PostGatewayOrchestrationEventListener {
    
    public PluginChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new ConfigurationNode(name).getConfigMapPath());
    }
    
    @Override
    protected PluginChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new PluginChangedEvent(ConfigurationYamlConverter.loadConfigMap(event.getValue()));
    }

}
