package org.cloud.gateway.core.rule;
import lombok.Data;
import lombok.ToString;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import java.util.List;


@Data
@ToString
public class RouteRule {
    private String name;
    private final List<ClusterConfiguration> clusterConfigurations;

}
