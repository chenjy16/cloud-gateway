package org.cloud.gateway.core.algorithm;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.ServerConfiguration;



public final class RandomLoadBalanceAlgorithm implements LoadBalanceAlgorithm {
    
    @Override
    public ServerConfiguration select(final ClusterConfiguration clusterConfiguration, final String ip) {
        return null;
    }
    @Override
    public String algorithm() {
        return null;
    }
}
