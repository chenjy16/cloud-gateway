package org.cloud.gateway.orchestration.internal.registry.config.event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.orchestration.internal.registry.listener.GatewayOrchestrationEvent;

import java.util.Map;


@RequiredArgsConstructor
@Getter
public final class RouteChangedEvent implements GatewayOrchestrationEvent {

    private final ClusterConfiguration clusterConfiguration;

}
