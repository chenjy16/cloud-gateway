package org.cloud.gateway.admin.service;
import com.google.common.base.Joiner;
import org.cloud.gateway.admin.utils.RegistryCenterUtils;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.keygen.DefaultKeyGenerator;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class RouteConfigurationService {

    private static final DefaultKeyGenerator keygen= new DefaultKeyGenerator();

    @Autowired
    private  RegistryCenter regCenter;



    /**
     * @Desc:
     * @param       clusterConfiguration
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/3/2 14:34
     *
     */
    public void persistRouteRule(final ClusterConfiguration clusterConfiguration) {
        regCenter.persist(RegistryCenterUtils.getPluginNode(keygen.generateKey().toString()), ConfigurationYamlConverter.dumpClusterConfiguration(clusterConfiguration));
    }

    /**
     * @Desc:
     * @param       id
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/3/2 14:34
     *
     */
    public void deleteRouteRule(String id) {
        regCenter.delete(RegistryCenterUtils.getRulePath(id));
    }

    /**
     * @Desc:
     * @param       id
     * @return:     org.cloud.gateway.core.configuration.ClusterConfiguration
     * @author:     chenjianyu944
     * @Date:       2021/3/2 14:34
     *
     */
    public ClusterConfiguration findRouteRuleById(String id) {
        String str=regCenter.getDirectly(RegistryCenterUtils.getRulePath(id));
        return ConfigurationYamlConverter.loadClusterConfiguration(str);
    }


    /**
     * @Desc:
     * @param
     * @return:     java.util.List<org.cloud.gateway.core.configuration.ClusterConfiguration>
     * @author:     chenjianyu944
     * @Date:       2021/3/2 14:34
     *
     */
    public List<ClusterConfiguration> findRouteRules() {
        return regCenter.getChildrenKeys(RegistryCenterUtils.getRulePath()).stream().filter(Objects::nonNull)
                .map(p->{
                    return ConfigurationYamlConverter.loadClusterConfiguration(regCenter.getDirectly(p));
                }).collect(Collectors.toList());
    }

}
