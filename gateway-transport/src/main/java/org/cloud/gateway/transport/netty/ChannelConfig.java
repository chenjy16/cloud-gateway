package org.cloud.gateway.transport.netty;

import java.util.HashMap;

/**
 * Created by cjy on 2020/1/4.
 */
public class ChannelConfig implements Cloneable
{
    private final HashMap<ChannelConfigKey, ChannelConfigValue> parameters;

    public ChannelConfig()
    {
        parameters = new HashMap<>();
    }

    public ChannelConfig(HashMap<ChannelConfigKey, ChannelConfigValue> parameters)
    {
        this.parameters = (HashMap<ChannelConfigKey, ChannelConfigValue>) parameters.clone();
    }

    public void add(ChannelConfigValue param)
    {
        this.parameters.put(param.key(), param);
    }

    public <T> void set(ChannelConfigKey<T> key, T value)
    {
        this.parameters.put(key, new ChannelConfigValue<>(key, value));
    }

    public <T> T get(ChannelConfigKey<T> key)
    {
        ChannelConfigValue<T> ccv = parameters.get(key);
        T value = ccv == null ? null : (T) ccv.value();

        if (value == null) {
            value = key.defaultValue();
        }

        return value;
    }

    public <T> ChannelConfigValue<T> getConfig(ChannelConfigKey<T> key)
    {
        return (ChannelConfigValue<T>) parameters.get(key);
    }

    public <T> boolean contains(ChannelConfigKey<T> key)
    {
        return parameters.containsKey(key);
    }

    @Override
    public ChannelConfig clone()
    {
        return new ChannelConfig(parameters);
    }
}
