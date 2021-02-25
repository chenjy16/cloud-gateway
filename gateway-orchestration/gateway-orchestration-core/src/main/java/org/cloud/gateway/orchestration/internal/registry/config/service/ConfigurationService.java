package org.cloud.gateway.orchestration.internal.registry.config.service;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.rule.PluginRule;
import org.cloud.gateway.core.rule.RouteRule;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;


public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    public ConfigurationService(final String name, final RegistryCenter regCenter) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
    }


    public RouteRule loadRouteRule() {
        return new RouteRule("",ConfigurationYamlConverter.loadClusterConfigurationMap(regCenter.getDirectly(configNode.getRulePath()))) ;
    }


    public PluginRule loadPluginRule() {
        return new PluginRule("",ConfigurationYamlConverter.loadPluginConfigurationMap(regCenter.getDirectly(configNode.getPluginNode()))) ;
    }

}
