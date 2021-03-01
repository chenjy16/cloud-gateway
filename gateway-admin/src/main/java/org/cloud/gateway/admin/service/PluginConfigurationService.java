package org.cloud.gateway.admin.service;
import com.google.common.base.Joiner;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.cloud.gateway.orchestration.internal.registry.yaml.ConfigurationYamlConverter;
import org.cloud.gateway.orchestration.reg.api.RegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;

public class PluginConfigurationService {

    private static final String ROOT = "config";
    private static final String PLUGIN_NODE = "plugin";


    @Autowired
    private  RegistryCenter regCenter;



    public void persistPluginRule(final PluginConfiguration pluginConfiguration) {
        regCenter.persist("", ConfigurationYamlConverter.dumpPluginConfiguration(pluginConfiguration));
    }


    public void deletePluginRule(String id) {
        regCenter.delete(Joiner.on("/").join("", ROOT, PLUGIN_NODE,id));
    }


    public String findPluginRuleById(String id) {
        return regCenter.getDirectly(id);
    }

}
