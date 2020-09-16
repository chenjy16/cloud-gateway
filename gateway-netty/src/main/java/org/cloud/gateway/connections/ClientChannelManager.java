package org.cloud.gateway.connections;
import com.google.common.collect.Sets;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;



public class ClientChannelManager {




    protected void removeMissingServerConnectionPools(List<Server> oldList, List<Server> newList) {
        Set<Server> oldSet = new HashSet<>(oldList);
        Set<Server> newSet = new HashSet<>(newList);
        Set<Server> removedSet = Sets.difference(oldSet, newSet);
        if (!removedSet.isEmpty()) {
            for (Server s : removedSet) {
                IConnectionPool pool = perServerPools.remove(s);
                if (pool != null) {
                    pool.shutdown();
                }
            }
        }
    }



    @Override
    public void shutdown() {
        this.shuttingDown = true;
        loadBalancer.shutdown();

        for (IConnectionPool pool : perServerPools.values()) {
            pool.shutdown();
        }
    }

    @Override
    public boolean release(final PooledConnection conn) {

        conn.stopRequestTimer();
        releaseConnCounter.increment();
        connsInUse.decrementAndGet();

        final ServerStats stats = conn.getServerStats();
        stats.decrementActiveRequestsCount();
        stats.incrementNumRequests();

        if (shuttingDown) {
            return false;
        }

        boolean released = false;

        if (conn.isShouldClose() ||
                // if the connection has been around too long (i.e. too many requests), then close it
                conn.getUsageCount() > connPoolConfig.getMaxRequestsPerConnection()) {

            // Close and discard the connection, as it has been flagged (possibly due to receiving a non-channel error like a 503).
            conn.setInPool(false);
            conn.close();
        }
        else if (stats.isCircuitBreakerTripped()) {
            // Don't put conns for currently circuit-tripped servers back into the pool.
            conn.setInPool(false);
            conn.close();
        }
        else if (!conn.isActive()) {
            // Connection is already closed, so discard.
            alreadyClosedCounter.increment();
            // make sure to decrement OpenConnectionCounts
            conn.updateServerStats();
            conn.setInPool(false);
        }
        else {
            releaseHandlers(conn);

            // Attempt to return connection to the pool.
            IConnectionPool pool = perServerPools.get(conn.getServer());
            if (pool != null) {
                released = pool.release(conn);
            }
            else {
                // The pool for this server no longer exists (maybe due to it failling out of
                // discovery).
                conn.setInPool(false);
                released = false;
                conn.close();
            }

            if (LOG.isDebugEnabled()) LOG.debug("PooledConnection released: " + conn.toString());
        }

        return released;
    }




    protected void releaseHandlers(PooledConnection conn) {
        final ChannelPipeline pipeline = conn.getChannel().pipeline();
        removeHandlerFromPipeline(OriginResponseReceiver.CHANNEL_HANDLER_NAME, pipeline);
        // The Outbound handler is always after the inbound handler, so look for it.
        ChannelHandlerContext passportStateHttpClientHandlerCtx =
                pipeline.context(PassportStateHttpClientHandler.OutboundHandler.class);
        pipeline.addAfter(passportStateHttpClientHandlerCtx.name(), IDLE_STATE_HANDLER_NAME,
                new IdleStateHandler(0, 0, connPoolConfig.getIdleTimeout(), TimeUnit.MILLISECONDS));
    }

    public static void removeHandlerFromPipeline(final String handlerName, final ChannelPipeline pipeline) {
        if (pipeline.get(handlerName) != null) {
            pipeline.remove(handlerName);
        }
    }

    @Override
    public boolean remove(PooledConnection conn) {
        if (conn == null) {
            return false;
        }
        if (!conn.isInPool()) {
            return false;
        }

        // Attempt to remove the connection from the pool.
        IConnectionPool pool = perServerPools.get(conn.getServer());
        if (pool != null) {
            return pool.remove(conn);
        }
        else {
            // The pool for this server no longer exists (maybe due to it failling out of
            // discovery).
            conn.setInPool(false);
            connsInPool.decrementAndGet();
            return false;
        }
    }

    @Override
    public Promise<PooledConnection> acquire(final EventLoop eventLoop) {
        return acquire(eventLoop, null, null, null, 1, CurrentPassport.create(),
                new AtomicReference<>(), new AtomicReference<>());
    }

    @Override
    public Promise<PooledConnection> acquire(final EventLoop eventLoop, final Object key, final String httpMethod,
                                             final String uri, final int attemptNum, final CurrentPassport passport,
                                             final AtomicReference<Server> selectedServer,
                                             final AtomicReference<String> selectedHostAdddr) {

        if (attemptNum < 1) {
            throw new IllegalArgumentException("attemptNum must be greater than zero");
        }

        if (shuttingDown) {
            Promise<PooledConnection> promise = eventLoop.newPromise();
            promise.setFailure(SHUTTING_DOWN_ERR);
            return promise;
        }

        // Choose the next load-balanced server.
        final Server chosenServer = loadBalancer.chooseServer(key);
        if (chosenServer == null) {
            Promise<PooledConnection> promise = eventLoop.newPromise();
            promise.setFailure(new OriginConnectException("No servers available", OutboundErrorType.NO_AVAILABLE_SERVERS));
            return promise;
        }

        final InstanceInfo instanceInfo = chosenServer instanceof DiscoveryEnabledServer ?
                ((DiscoveryEnabledServer) chosenServer).getInstanceInfo() :
                // create mock instance info for non-discovery instances
                new InstanceInfo(chosenServer.getId(), null, null, chosenServer.getHost(), chosenServer.getId(),
                        null, null, null, null, null, null, null, null, 0, null, null, null, null, null, null, null, null, null, null, null, null);


        selectedServer.set(chosenServer);

        // Now get the connection-pool for this server.
        IConnectionPool pool = perServerPools.computeIfAbsent(chosenServer, s -> {
            // Get the stats from LB for this server.
            LoadBalancerStats lbStats = loadBalancer.getLoadBalancerStats();
            ServerStats stats = lbStats.getSingleServerStat(chosenServer);

            final ClientChannelManager clientChannelMgr = this;
            PooledConnectionFactory pcf = createPooledConnectionFactory(chosenServer, instanceInfo, stats, clientChannelMgr, closeConnCounter, closeWrtBusyConnCounter);

            // Create a new pool for this server.
            return createConnectionPool(chosenServer, stats, instanceInfo, clientConnFactory, pcf, connPoolConfig,
                    clientConfig, createNewConnCounter, createConnSucceededCounter, createConnFailedCounter,
                    requestConnCounter, reuseConnCounter, connTakenFromPoolIsNotOpen, maxConnsPerHostExceededCounter,
                    connEstablishTimer, connsInPool, connsInUse);
        });


        return pool.acquire(eventLoop, null, httpMethod, uri, attemptNum, passport, selectedHostAdddr);
    }






    protected PooledConnectionFactory createPooledConnectionFactory(Server chosenServer, InstanceInfo instanceInfo, ServerStats stats, ClientChannelManager clientChannelMgr,
                                                                    Counter closeConnCounter, Counter closeWrtBusyConnCounter) {

        return ch -> new PooledConnection(ch, chosenServer, clientChannelMgr, instanceInfo, stats, closeConnCounter, closeWrtBusyConnCounter);

    }





    protected IConnectionPool createConnectionPool(Server chosenServer, ServerStats stats, InstanceInfo instanceInfo,
                                                   NettyClientConnectionFactory clientConnFactory, PooledConnectionFactory pcf,
                                                   ConnectionPoolConfig connPoolConfig, IClientConfig clientConfig,
                                                   Counter createNewConnCounter, Counter createConnSucceededCounter,
                                                   Counter createConnFailedCounter, Counter requestConnCounter,
                                                   Counter reuseConnCounter, Counter connTakenFromPoolIsNotOpen,
                                                   Counter maxConnsPerHostExceededCounter, PercentileTimer connEstablishTimer,
                                                   AtomicInteger connsInPool, AtomicInteger connsInUse) {
        return new PerServerConnectionPool(
                chosenServer,
                stats,
                instanceInfo,
                clientConnFactory,
                pcf,
                connPoolConfig,
                clientConfig,
                createNewConnCounter,
                createConnSucceededCounter,
                createConnFailedCounter,
                requestConnCounter,
                reuseConnCounter,
                connTakenFromPoolIsNotOpen,
                maxConnsPerHostExceededCounter,
                connEstablishTimer,
                connsInPool,
                connsInUse
        );
    }
}
