package org.cloud.gateway.core.strategy;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class AntRouteStrategy implements  RouteStrategy{


    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public List<ClusterConfiguration> match(List<ClusterConfiguration> clusterConfigurations, ServerWebExchange exchange){
        return clusterConfigurations.stream().filter(cc -> Objects.nonNull(cc)&&antPathMatcher.match(cc.getUrlPattern(),exchange.getRequest().getPath().toString()))
                .collect(Collectors.toList());

    }
}
