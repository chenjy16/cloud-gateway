package org.cloud.gateway.core.configuration;
import lombok.Data;
import org.cloud.gateway.core.algorithm.LoadBalanceAlgorithm;

import java.util.List;

@Data
public class ClusterConfiguration {

    private String id;
    private String name;
    private String urlPattern;
    private String method;
    private LoadBalanceAlgorithm loadBalanceAlgorithm;
    private List<ServerConfiguration> serverConfigurations;



}
