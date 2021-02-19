
package org.cloud.gateway.orchestration.internal.registry.listener;

import org.cloud.gateway.orchestration.reg.listener.DataChangedEvent.ChangedType;


public interface GatewayOrchestrationListener {
    

    void watch(ChangedType... watchedChangedTypes);
}
