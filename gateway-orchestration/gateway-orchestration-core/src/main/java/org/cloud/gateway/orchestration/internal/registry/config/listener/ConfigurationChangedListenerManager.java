
package org.cloud.gateway.orchestration.internal.registry.config.listener;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent.ChangedType;
import java.util.Collection;



public final class ConfigurationChangedListenerManager {

    private final RouteChangedListener routeChangedListener;
    private final PluginChangedListener pluginChangedListener;


    public ConfigurationChangedListenerManager(final String name, final RegistryCenter regCenter) {
        routeChangedListener = new RouteChangedListener(name, regCenter);
        pluginChangedListener = new PluginChangedListener(name, regCenter);
    }
    

    public void initListeners() {
        routeChangedListener.watch(ChangedType.UPDATED,ChangedType.DELETED,ChangedType.ADDED);
        pluginChangedListener.watch(ChangedType.UPDATED,ChangedType.DELETED,ChangedType.ADDED);
    }
}
