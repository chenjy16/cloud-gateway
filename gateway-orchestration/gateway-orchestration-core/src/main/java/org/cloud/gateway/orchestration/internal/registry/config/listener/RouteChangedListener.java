package org.cloud.gateway.orchestration.internal.registry.config.listener;
import org.cloud.gateway.orchestration.internal.registry.config.event.RouteChangedEvent;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.internal.registry.listener.PostGatewayOrchestrationEventListener;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent;


public final class RouteChangedListener extends PostGatewayOrchestrationEventListener {
    
    public RouteChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new ConfigurationNode(name).getPropsPath());
    }
    
    @Override
    protected RouteChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new RouteChangedEvent(ConfigurationYamlConverter.loadProperties(event.getValue()));
    }
}
