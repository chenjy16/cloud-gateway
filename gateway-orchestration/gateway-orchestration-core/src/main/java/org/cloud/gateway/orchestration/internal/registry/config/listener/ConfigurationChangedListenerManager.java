
package org.cloud.gateway.orchestration.internal.registry.config.listener;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent.ChangedType;
import java.util.Collection;



public final class ConfigurationChangedListenerManager {

    private final RouteChangedListener propertiesChangedListener;
    private final PluginChangedListener configMapChangedListener;


    public ConfigurationChangedListenerManager(final String name, final RegistryCenter regCenter, final Collection<String> shardingSchemaNames) {
        propertiesChangedListener = new RouteChangedListener(name, regCenter);
        configMapChangedListener = new PluginChangedListener(name, regCenter);
    }
    

    public void initListeners() {
        propertiesChangedListener.watch(ChangedType.UPDATED);
        configMapChangedListener.watch(ChangedType.UPDATED);
    }
}
