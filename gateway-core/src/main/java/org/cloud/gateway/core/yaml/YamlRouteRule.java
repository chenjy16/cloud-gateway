package org.cloud.gateway.core.yaml;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.rule.RouteRule;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class YamlRouteRule {

    private String name;
    private List<ClusterConfiguration> clusterConfigurations;

    private String loadBalanceAlgorithmClassName;

    public YamlRouteRule(RouteRule routeRule) {
        this.name = routeRule.getName();
        this.clusterConfigurations=routeRule.getClusterConfigurations();
    }


    public RouteRule getRouteRule(){
        return new RouteRule(name,clusterConfigurations);
    }
}
