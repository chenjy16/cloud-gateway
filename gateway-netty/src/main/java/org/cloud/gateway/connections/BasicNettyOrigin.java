package org.cloud.gateway.connections;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import org.cloud.gateway.message.HttpRequestMessage;

import java.util.concurrent.atomic.AtomicReference;

public class BasicNettyOrigin {


    private final DefaultClientChannelManager clientChannelManager;


    public BasicNettyOrigin(DefaultClientChannelManager clientChannelManager) {
        this.clientChannelManager = clientChannelManager;
    }

    public Promise<PooledConnection> connectToOrigin(HttpRequestMessage zuulReq, EventLoop eventLoop, int attemptNumber,AtomicReference<Server> chosenServer,
                                                     AtomicReference<String> chosenHostAddr) {


        return clientChannelManager.acquire(eventLoop, null, null,
                null, chosenServer, chosenHostAddr);
    }
}
