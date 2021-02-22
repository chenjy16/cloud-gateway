package org.cloud.gateway.core.strategy;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.springframework.web.server.ServerWebExchange;
import java.util.List;

public final class AntRouteStrategy implements  RouteStrategy{

    @Override
    public Boolean match(List<ClusterConfiguration> clusterConfigurations, ServerWebExchange exchange) {
        return null;
    }
}
