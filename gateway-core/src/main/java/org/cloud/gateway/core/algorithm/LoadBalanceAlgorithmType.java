package org.cloud.gateway.core.algorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum LoadBalanceAlgorithmType {
    
    ROUND_ROBIN(new RoundRobinLoadBalanceAlgorithm()),
    RANDOM(new RandomLoadBalanceAlgorithm());
    
    private final LoadBalanceAlgorithm algorithm;

    public static LoadBalanceAlgorithmType getDefaultAlgorithmType() {
        return ROUND_ROBIN;
    }
}
