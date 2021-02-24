package org.cloud.gateway.core.algorithm;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.ServerConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class RoundRobinLoadBalanceAlgorithm implements LoadBalanceAlgorithm {
    
    private static final ConcurrentHashMap<String, AtomicInteger> COUNT_MAP = new ConcurrentHashMap<>();
    
    @Override
    public ServerConfiguration select(final ClusterConfiguration clusterConfiguration, final String ip) {

      return null;
    }

    @Override
    public String algorithm() {
        return null;
    }
}
