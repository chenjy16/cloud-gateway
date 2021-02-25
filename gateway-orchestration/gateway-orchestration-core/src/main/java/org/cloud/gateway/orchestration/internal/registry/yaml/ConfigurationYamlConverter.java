package org.cloud.gateway.orchestration.internal.registry.yaml;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.yaml.snakeyaml.Yaml;
import java.util.LinkedHashMap;
import java.util.Map;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {


    @SuppressWarnings("unchecked")
    public static Map<String, ClusterConfiguration> loadClusterConfigurationMap(final String data) {
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, ClusterConfiguration>() : (Map) new Yaml().load(data);
    }


    @SuppressWarnings("unchecked")
    public static Map<String, PluginConfiguration> loadPluginConfigurationMap(final String data) {
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, PluginConfiguration>() : (Map) new Yaml().load(data);
    }





}
