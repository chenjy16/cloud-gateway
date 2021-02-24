package org.cloud.gateway.core.algorithm;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.ServerConfiguration;

import java.util.List;

public interface LoadBalanceAlgorithm {

    public ServerConfiguration select(final ClusterConfiguration clusterConfiguration, final String ip);

    String algorithm();

}
