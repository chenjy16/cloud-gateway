package org.cloud.gateway.orchestration.internal.registry.config.service;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import java.util.Map;
import java.util.stream.Collectors;


public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    public ConfigurationService(final String name, final RegistryCenter regCenter) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
    }


    public Map<String, ClusterConfiguration> loadRouteRule() {
        return regCenter.getChildrenKeys(configNode.getRulePath()).stream().map(c->{
            return ConfigurationYamlConverter.loadClusterConfiguration(regCenter.getDirectly(c));
        }).collect(Collectors.toConcurrentMap(x->x.getId(),y->y));

    }



    public Map<String, PluginConfiguration> loadPluginRule() {

        return regCenter.getChildrenKeys(configNode.getPluginNode()).stream().map(p->{
            return ConfigurationYamlConverter.loadPluginConfiguration(regCenter.getDirectly(p));
        }).collect(Collectors.toConcurrentMap(x->x.getName(),y->y));
    }

}
