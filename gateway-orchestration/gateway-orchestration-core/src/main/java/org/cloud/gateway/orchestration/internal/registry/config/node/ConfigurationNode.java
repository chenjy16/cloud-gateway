package org.cloud.gateway.orchestration.internal.registry.config.node;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public final class ConfigurationNode {
    
    private static final String ROOT = "config";
    
    private static final String SCHEMA_NODE = "schema";
    
    private static final String DATA_SOURCE_NODE = "datasource";
    
    private static final String RULE_NODE = "rule";
    
    private static final String AUTHENTICATION_NODE = "authentication";
    
    private static final String CONFIG_MAP_NODE = "configmap";
    
    private static final String PROPS_NODE = "props";
    
    private final String name;
    
    /**
     * Get schema path.
     *
     * @return schema path
     */
    public String getSchemaPath() {
        return Joiner.on("/").join("", name, ROOT, SCHEMA_NODE);
    }
    
    /**
     * Get data source path.
     *
     * @param schemaName schema name
     * @return data source path
     */
    public String getDataSourcePath(final String schemaName) {
        return getFullPath(schemaName, DATA_SOURCE_NODE);
    }
    
    /**
     * Get rule path.
     * 
     * @param schemaName schema name
     * @return rule path
     */
    public String getRulePath(final String schemaName) {
        return getFullPath(schemaName, RULE_NODE);
    }
    
    /**
     * Get authentication path.
     *
     * @return authentication path
     */
    public String getAuthenticationPath() {
        return getFullPath(AUTHENTICATION_NODE);
    }
    
    /**
     * Get config map path.
     *
     * @return config map path
     */
    public String getConfigMapPath() {
        return getFullPath(CONFIG_MAP_NODE);
    }
    
    /**
     * Get props path.
     *
     * @return props path
     */
    public String getPropsPath() {
        return getFullPath(PROPS_NODE);
    }
    
    private String getFullPath(final String schemaName, final String node) {
        return Joiner.on("/").join("", name, ROOT, SCHEMA_NODE, schemaName, node);
    }
    
    private String getFullPath(final String node) {
        return Joiner.on("/").join("", name, ROOT, node);
    }
}
