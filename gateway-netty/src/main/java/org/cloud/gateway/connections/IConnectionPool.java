package org.cloud.gateway.connections;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.atomic.AtomicReference;

public interface IConnectionPool {

    public Promise<PooledConnection> acquire(EventLoop eventLoop, Object key, String httpMethod, String uri,int attemptNum, AtomicReference<String> selectedHostAddr);
}
