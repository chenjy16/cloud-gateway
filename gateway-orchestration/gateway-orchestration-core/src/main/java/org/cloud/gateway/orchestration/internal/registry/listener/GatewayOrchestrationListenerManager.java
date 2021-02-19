package org.cloud.gateway.orchestration.internal.registry.listener;
import org.cloud.gateway.orchestration.internal.registry.config.listener.ConfigurationChangedListenerManager;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;

import java.util.Collection;


public final class GatewayOrchestrationListenerManager {
    
    private final ConfigurationChangedListenerManager configurationChangedListenerManager;

    
    public GatewayOrchestrationListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        configurationChangedListenerManager = new ConfigurationChangedListenerManager(name, regCenter, shardingSchemaNames);
    }
    
    /**
     * Initialize all orchestration listeners.
     */
    public void initListeners() {
        configurationChangedListenerManager.initListeners();
    }
}
