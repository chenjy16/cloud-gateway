
package org.cloud.gateway.orchestration.internal.registry.eventbus;

import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GatewayOrchestrationEventBus {
    
    private static final EventBus INSTANCE = new EventBus();
    

    public static EventBus getInstance() {
        return INSTANCE;
    }
}
