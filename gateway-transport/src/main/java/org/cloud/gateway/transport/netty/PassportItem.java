package org.cloud.gateway.transport.netty;

public class PassportItem
{
    private final long time;
    private final PassportState state;

    public PassportItem(PassportState state, long time)
    {
        this.time = time;
        this.state = state;
    }

    public long getTime()
    {
        return time;
    }

    public PassportState getState()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return time + "=" + state;
    }
}
