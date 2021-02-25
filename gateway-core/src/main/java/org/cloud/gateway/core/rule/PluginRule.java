package org.cloud.gateway.core.rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cloud.gateway.core.configuration.PluginConfiguration;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginRule {

    private String name;
    private Map<String, PluginConfiguration> pluginConfigurationMap;

}
