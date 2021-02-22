package org.cloud.gateway.core.strategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cloud.gateway.core.algorithm.LoadBalanceAlgorithm;
import org.cloud.gateway.core.algorithm.LoadBalanceAlgorithmType;

@RequiredArgsConstructor
@Getter
public enum RouteStrategyType {

    ANT(new AntRouteStrategy());

    private final RouteStrategy routeStrategy;

    public static RouteStrategyType getDefaultRouteStrategyType() {
        return ANT;
    }

}
