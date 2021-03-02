package org.cloud.gateway.admin.service;
import org.cloud.gateway.admin.utils.RegistryCenterUtils;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.cloud.gateway.core.keygen.DefaultKeyGenerator;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class PluginConfigurationService {


    private static final DefaultKeyGenerator keygen= new DefaultKeyGenerator();

    @Autowired
    private  RegistryCenter regCenter;

    
    /**
     * @Desc:       保存或者更新插件信息
     * @param       pluginConfiguration
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/3/2 9:35
     *
     */
    public void persistPluginRule(final PluginConfiguration pluginConfiguration) {
        regCenter.persist(RegistryCenterUtils.getPluginNode(keygen.generateKey().toString()), ConfigurationYamlConverter.dumpPluginConfiguration(pluginConfiguration));
    }


    /**
     * @Desc:       删除插件信息
     * @param       id
     * @return:     void
     * @author:     chenjianyu944
     * @Date:       2021/3/2 9:35
     *
     */
    public void deletePluginRule(String id) {
        regCenter.delete(RegistryCenterUtils.getPluginNode(id));
    }


    /**
     * @Desc:       查找插件
     * @param       id
     * @return:     java.lang.String
     * @author:     chenjianyu944
     * @Date:       2021/3/2 9:35
     *
     */
    public PluginConfiguration findPluginRuleById(String id) {
        String str=regCenter.getDirectly(RegistryCenterUtils.getPluginNode(id));
        return ConfigurationYamlConverter.loadPluginConfiguration(str);
    }


    /**
     * @Desc:       查询插件列表
     * @param
     * @return:     java.util.List<org.cloud.gateway.core.configuration.PluginConfiguration>
     * @author:     chenjianyu944
     * @Date:       2021/3/2 14:17
     *
     */
    public List<PluginConfiguration> findPluginRules() {
        return regCenter.getChildrenKeys(RegistryCenterUtils.getPluginNode()).stream().filter(Objects::nonNull)
                .map(p->{
                    return ConfigurationYamlConverter.loadPluginConfiguration(regCenter.getDirectly(p));
                }).collect(Collectors.toList());

    }

}
