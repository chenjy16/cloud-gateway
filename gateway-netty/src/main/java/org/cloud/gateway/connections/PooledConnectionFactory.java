package org.cloud.gateway.connections;
import io.netty.channel.Channel;

public interface PooledConnectionFactory {
    PooledConnection create(Channel ch);
}
