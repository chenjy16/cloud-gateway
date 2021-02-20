package org.cloud.gateway.core.rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginRule {

    private String id;

    private String pluginId;

    private String pluginName;

    private String name;

    private Integer matchMode;

    private Integer sort;

    private Boolean enabled;

}
