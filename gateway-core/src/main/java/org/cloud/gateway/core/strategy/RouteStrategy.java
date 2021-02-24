package org.cloud.gateway.core.strategy;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

public interface RouteStrategy {



    List<ClusterConfiguration> match(List<ClusterConfiguration> clusterConfigurations, ServerWebExchange exchange);

}
