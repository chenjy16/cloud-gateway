
package org.cloud.gateway.orchestration.internal.registry.config.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.orchestration.internal.registry.listener.GatewayOrchestrationEvent;

import java.util.Map;


@RequiredArgsConstructor
@Getter
public final class PluginChangedEvent implements GatewayOrchestrationEvent {
    
    private final Map<String, Object> configMap;
}
