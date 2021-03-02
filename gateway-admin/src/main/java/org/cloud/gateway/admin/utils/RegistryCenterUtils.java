package org.cloud.gateway.admin.utils;
import com.google.common.base.Joiner;

public class RegistryCenterUtils {


    private static final String ROOT = "config";

    private static final String RULE_NODE = "rule";

    private static final String PLUGIN_NODE = "plugin";


    public static String getRulePath(final String key) {
        return getFullPath(RULE_NODE,key);
    }

    public static String getPluginNode(String key) {
        return getFullPath(PLUGIN_NODE,key);
    }

    public static String getPluginNode() {
        return  Joiner.on("/").join("", ROOT, PLUGIN_NODE);
    }



    private static String getFullPath(final String node,final String key) {
        return Joiner.on("/").join("", ROOT, node,key);
    }

    public static  String getRulePath() {
        return Joiner.on("/").join("", ROOT,RULE_NODE);
    }


}
