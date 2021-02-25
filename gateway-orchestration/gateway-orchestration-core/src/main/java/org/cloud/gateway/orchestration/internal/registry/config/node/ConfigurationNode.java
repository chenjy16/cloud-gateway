package org.cloud.gateway.orchestration.internal.registry.config.node;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public final class ConfigurationNode {
    
    private static final String ROOT = "config";
    
    private static final String SCHEMA_NODE = "schema";
    
    private static final String RULE_NODE = "rule";
    
    private static final String AUTHENTICATION_NODE = "authentication";

    private static final String PLUGIN_NODE = "plugin";

    
    private final String name;

    public String getSchemaPath() {
        return Joiner.on("/").join("", name, ROOT, SCHEMA_NODE);
    }
    

    public String getRulePath() {
        return getFullPath(RULE_NODE);
    }
    

    public String getAuthenticationPath() {
        return getFullPath(AUTHENTICATION_NODE);
    }
    

    public String getPluginNode() {
        return getFullPath(PLUGIN_NODE);
    }
    

    private String getFullPath(final String schemaName, final String node) {
        return Joiner.on("/").join("", name, ROOT, SCHEMA_NODE, schemaName, node);
    }
    
    private String getFullPath(final String node) {
        return Joiner.on("/").join("", name, ROOT, node);
    }
}
