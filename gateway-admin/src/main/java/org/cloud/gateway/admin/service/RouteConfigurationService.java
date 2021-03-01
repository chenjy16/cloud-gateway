package org.cloud.gateway.admin.service;
import com.google.common.base.Joiner;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;

public class RouteConfigurationService {


    private static final String ROOT = "config";

    private static final String RULE_NODE = "rule";


    @Autowired
    private  RegistryCenter regCenter;



    public void persistRouteRule(final ClusterConfiguration clusterConfiguration) {
        regCenter.persist("", ConfigurationYamlConverter.dumpClusterConfiguration(clusterConfiguration));
    }



    public void deleteRouteRule(String id) {
        regCenter.delete(Joiner.on("/").join("", ROOT, RULE_NODE,id));
    }


    public String findRouteRuleById(String id) {
        return regCenter.getDirectly(id);
    }

}
