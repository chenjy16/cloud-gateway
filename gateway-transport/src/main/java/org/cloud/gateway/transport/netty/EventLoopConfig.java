package org.cloud.gateway.transport.netty;

/**
 * Created by cjy on 2020/1/24.
 */
public interface EventLoopConfig
{
    int eventLoopCount();

    int acceptorCount();
}

