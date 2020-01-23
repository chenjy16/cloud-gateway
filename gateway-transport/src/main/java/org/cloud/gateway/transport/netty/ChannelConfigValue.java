
package org.cloud.gateway.transport.netty;


public class ChannelConfigValue<T>
{
    private final ChannelConfigKey<T> key;
    private final T value;

    public ChannelConfigValue(ChannelConfigKey<T> key, T value)
    {
        this.key = key;
        this.value = value;
    }

    public ChannelConfigKey<T> key()
    {
        return key;
    }

    public T value()
    {
        return value;
    }
}
