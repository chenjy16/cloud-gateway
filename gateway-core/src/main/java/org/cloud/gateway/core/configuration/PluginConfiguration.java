package org.cloud.gateway.core.configuration;
import lombok.Data;

@Data
public class PluginConfiguration {

    private String id;

    private String pluginId;

    private String pluginName;

    private String name;

    private Integer matchMode;

    private Integer sort;

    private Boolean enabled;


}
