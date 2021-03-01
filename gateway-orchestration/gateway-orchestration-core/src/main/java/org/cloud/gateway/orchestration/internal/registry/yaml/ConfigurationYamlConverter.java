package org.cloud.gateway.orchestration.internal.registry.yaml;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.yaml.snakeyaml.Yaml;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {


    public static  ClusterConfiguration loadClusterConfiguration(final String data) {
        return Strings.isNullOrEmpty(data) ? new ClusterConfiguration() : new Yaml().loadAs(data,ClusterConfiguration.class);
    }



    public static PluginConfiguration loadPluginConfiguration(final String data) {
        return Strings.isNullOrEmpty(data) ? new PluginConfiguration() : new Yaml().loadAs(data,PluginConfiguration.class);
    }


    public static String dumpClusterConfiguration(final ClusterConfiguration clusterConfiguration) {
        return new Yaml().dumpAsMap(clusterConfiguration);
    }

    public static String dumpPluginConfiguration(final PluginConfiguration pluginConfiguration) {
        return new Yaml().dumpAsMap(pluginConfiguration);
    }

}
