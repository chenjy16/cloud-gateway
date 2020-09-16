package org.cloud.gateway.connections;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class PooledConnection {


    private final Channel channel;
    private final ClientChannelManager channelManager;
    private ConnectionState connectionState;
    private final Server server;
    private boolean inPool = false;
    private boolean shouldClose = false;
    private boolean released = false;


    /**
     * Connection State
     */
    public enum ConnectionState {
        /**
         * valid state in pool
         */
        WRITE_READY,
        /**
         * Can not be put in pool
         */
        WRITE_BUSY
    }


    public PooledConnection(Channel channel, Server server, ClientChannelManager channelManager) {
        this.channel = channel;
        this.channelManager = channelManager;
        this.server = server;
    }

    public void setInUse(){
        this.connectionState = ConnectionState.WRITE_BUSY;
        this.released = false;
    }


    public void setConnectionState(ConnectionState state) {
        this.connectionState = state;
    }


    public boolean isActive() {
        return (channel.isActive() && channel.isRegistered());
    }

    public boolean isInPool()
    {
        return inPool;
    }

    public void setInPool(boolean inPool)
    {
        this.inPool = inPool;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelFuture close() {

        return channel.close();
    }

    public void release() {
        if (released) {
            return;
        }
        connectionState = ConnectionState.WRITE_READY;
        released = true;
        channelManager.release(this);
    }


    public Server getServer(){
        return server;
    }

}
