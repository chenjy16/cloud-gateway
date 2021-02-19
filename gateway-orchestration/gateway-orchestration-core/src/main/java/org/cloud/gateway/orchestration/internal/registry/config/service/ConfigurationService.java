package org.cloud.gateway.orchestration.internal.registry.config.service;
import org.cloud.gateway.orchestration.internal.registry.config.node.ConfigurationNode;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import java.util.Collection;



public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    public ConfigurationService(final String name, final RegistryCenter regCenter) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
    }


    public Collection<String> getAllShardingSchemaNames() {
        return regCenter.getChildrenKeys(configNode.getSchemaPath());
    }
}
