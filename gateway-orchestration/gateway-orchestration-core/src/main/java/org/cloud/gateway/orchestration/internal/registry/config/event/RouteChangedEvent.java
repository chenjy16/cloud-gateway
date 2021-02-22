package org.cloud.gateway.orchestration.internal.registry.config.event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.core.rule.RouteRule;
import org.cloud.gateway.orchestration.internal.registry.listener.GatewayOrchestrationEvent;


@RequiredArgsConstructor
@Getter
public final class RouteChangedEvent implements GatewayOrchestrationEvent {

    private final RouteRule routeRule;

}
