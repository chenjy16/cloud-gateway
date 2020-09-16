package org.cloud.gateway.connections;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultClientChannelManager {


    public static final String IDLE_STATE_HANDLER_NAME = "idleStateHandler";

    private volatile boolean shuttingDown = false;

    private final ConcurrentHashMap<Server, IConnectionPool> perServerPools;

    public DefaultClientChannelManager(ConcurrentHashMap<Server, IConnectionPool> perServerPools) {
        this.perServerPools = perServerPools;
    }

    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/12 9:52
     **/
    public static void removeHandlerFromPipeline(final String handlerName, final ChannelPipeline pipeline) {
        if (pipeline.get(handlerName) != null) {
            pipeline.remove(handlerName);
        }
    }



    public Promise<PooledConnection> acquire(final EventLoop eventLoop, final Object key, final String httpMethod,
                                             final String uri, final AtomicReference<Server> selectedServer,
                                             final AtomicReference<String> selectedHostAdddr){


        if (shuttingDown) {
            Promise<PooledConnection> promise = eventLoop.newPromise();
            promise.setFailure(SHUTTING_DOWN_ERR);
            return promise;
        }

        // Choose the next load-balanced server.




        // Now get the connection-pool for this server.
        IConnectionPool pool = perServerPools.computeIfAbsent(chosenServer, s -> {
            // Get the stats from LB for this server.
            final ClientChannelManager clientChannelMgr = this;

            PooledConnectionFactory pcf = createPooledConnectionFactory(chosenServer, clientChannelMgr);
            // Create a new pool for this server.
            return createConnectionPool(chosenServer,clientConnFactory, pcf);
        });

        return pool.acquire(eventLoop, null, httpMethod, uri, 5, selectedHostAdddr);
    }




    protected PooledConnectionFactory createPooledConnectionFactory(Server chosenServer, ClientChannelManager clientChannelMgr) {
        return ch -> new PooledConnection(ch, chosenServer, clientChannelMgr);
    }


    protected IConnectionPool createConnectionPool(Server chosenServer,NettyClientConnectionFactory clientConnFactory, PooledConnectionFactory pcf){
        return new PerServerConnectionPool(chosenServer, clientConnFactory, pcf);
    }

}
