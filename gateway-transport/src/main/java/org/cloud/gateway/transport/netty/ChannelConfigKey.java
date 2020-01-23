
package org.cloud.gateway.transport.netty;


public class ChannelConfigKey<T>
{
    private final String key;
    private final T defaultValue;

    public ChannelConfigKey(String key, T defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public ChannelConfigKey(String key)
    {
        this.key = key;
        this.defaultValue = null;
    }

    public String key() {
        return key;
    }

    public T defaultValue()
    {
        return defaultValue;
    }

    public boolean hasDefaultValue()
    {
        return defaultValue != null;
    }

    @Override
    public String toString()
    {
        return "ChannelConfigKey{" +
                "key='" + key + '\'' +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
