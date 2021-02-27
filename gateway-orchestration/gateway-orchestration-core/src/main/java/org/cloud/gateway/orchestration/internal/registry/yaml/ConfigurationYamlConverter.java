package org.cloud.gateway.orchestration.internal.registry.yaml;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cloud.gateway.core.configuration.ClusterConfiguration;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import org.yaml.snakeyaml.Yaml;



@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {



    @SuppressWarnings("unchecked")
    public static  ClusterConfiguration loadClusterConfiguration(final String data) {
        return Strings.isNullOrEmpty(data) ? new ClusterConfiguration() : (ClusterConfiguration) new Yaml().load(data);
    }


    @SuppressWarnings("unchecked")
    public static PluginConfiguration loadPluginConfiguration(final String data) {
        return Strings.isNullOrEmpty(data) ? new PluginConfiguration() : (PluginConfiguration) new Yaml().load(data);
    }

}
