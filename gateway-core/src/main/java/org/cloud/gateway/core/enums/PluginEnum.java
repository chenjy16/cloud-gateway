package org.cloud.gateway.core.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;


@RequiredArgsConstructor
@Getter
public enum PluginEnum {


    GLOBAL(1, "global"),

    DIVIDE(50, "divide");

    private final int code;

    private final String name;


    public static PluginEnum getPluginEnumByCode(final int code) {
        return Arrays.stream(PluginEnum.values())
                .filter(pluginEnum -> pluginEnum.getCode() == code)
                .findFirst().orElse(PluginEnum.GLOBAL);
    }


    public static PluginEnum getPluginEnumByName(final String name) {
        return Arrays.stream(PluginEnum.values())
                .filter(pluginEnum -> pluginEnum.getName().equals(name))
                .findFirst().orElse(PluginEnum.GLOBAL);
    }
}
