
package org.cloud.gateway.orchestration.internal.registry.yaml;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {


    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadConfigMap(final String data) {
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, Object>() : (Map) new Yaml().load(data);
    }
    

    public static Properties loadProperties(final String data) {
        return Strings.isNullOrEmpty(data) ? new Properties() : new Yaml().loadAs(data, Properties.class);
    }
    

}
