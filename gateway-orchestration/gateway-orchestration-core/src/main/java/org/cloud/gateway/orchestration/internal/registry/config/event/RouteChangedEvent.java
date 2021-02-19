package org.cloud.gateway.orchestration.internal.registry.config.event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.orchestration.internal.registry.listener.GatewayOrchestrationEvent;

import java.util.Properties;


@RequiredArgsConstructor
@Getter
public final class RouteChangedEvent implements GatewayOrchestrationEvent {
    
    private final Properties props;
}
