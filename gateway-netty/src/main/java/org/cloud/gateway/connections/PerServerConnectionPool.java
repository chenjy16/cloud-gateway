package org.cloud.gateway.connections;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

public class PerServerConnectionPool implements IConnectionPool{

    private final NettyClientConnectionFactory connectionFactory;
    private ConcurrentHashMap<EventLoop, Deque<PooledConnection>> connectionsPerEventLoop = new ConcurrentHashMap<>();
    private final Server server;
    private final PooledConnectionFactory pooledConnectionFactory;


    public PerServerConnectionPool( Server server,NettyClientConnectionFactory connectionFactory, PooledConnectionFactory pooledConnectionFactory) {
        this.connectionFactory = connectionFactory;
        this.server = server;
        this.pooledConnectionFactory = pooledConnectionFactory;
    }



    /**
     * @desc   用于在将连接返回给调用方之前获取运行时连接
     * @author chenjianyu944@gmail.com
     * @date   2020/9/11 22:32
     **/
    private void onAcquire(final PooledConnection conn) {
        removeIdleStateHandler(conn);
        conn.setInUse();
    }

    /**
     * @desc   去除空闲连接
     * @author chenjianyu944@gmail.com
     * @date   2020/9/11 22:31
     **/
    public  void removeIdleStateHandler(PooledConnection conn) {
        DefaultClientChannelManager.removeHandlerFromPipeline(DefaultClientChannelManager.IDLE_STATE_HANDLER_NAME, conn.getChannel().pipeline());
    }


    /**
     * @desc   获得连接
     * @author chenjianyu944@gmail.com
     * @date   2020/9/11 22:34
     **/
    public Promise<PooledConnection> acquire(EventLoop eventLoop, Object key, String httpMethod, String uri,int attemptNum, AtomicReference<String> selectedHostAddr) {

        Promise<PooledConnection> promise = eventLoop.newPromise();
        // Try getting a connection from the pool.
        final PooledConnection conn = tryGettingFromConnectionPool(eventLoop);
        if (conn != null) {
            // There was a pooled connection available, so use this one.
            conn.getChannel().read();
            onAcquire(conn);
            initPooledConnection(conn, promise);
            selectedHostAddr.set(getHostFromServer(conn.getServer()));
        }else {
            // connection pool empty, create new connection using client connection factory.
            tryMakingNewConnection(eventLoop, promise, httpMethod, uri, attemptNum, selectedHostAddr);
        }
        return promise;
    }





    public void initPooledConnection(PooledConnection conn, Promise<PooledConnection> promise) {
        // add custom init code by overriding this method
        promise.setSuccess(conn);
    }





    /**
     * @desc   尝试获得新连接
     * @author chenjianyu944@gmail.com
     * @date   2020/9/11 22:39
     **/
    public void tryMakingNewConnection(final EventLoop eventLoop, final Promise<PooledConnection> promise,final String httpMethod, final String uri, final int attemptNum,final AtomicReference<String> selectedHostAddr) {

        try {
            // Choose to use either IP or hostname.
            String host = getHostFromServer(server);
            selectedHostAddr.set(host);
            final ChannelFuture cf = connectToServer(eventLoop, host);
            if (cf.isDone()) {
                handleConnectCompletion(cf, promise, httpMethod, uri, attemptNum);
            }else {
                cf.addListener(future -> {
                    try {
                        handleConnectCompletion((ChannelFuture) future, promise, httpMethod, uri, attemptNum);
                    }
                    catch (Throwable e) {
                        if (! promise.isDone()) {
                            promise.setFailure(e);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            promise.setFailure(e);
        }
    }



     void handleConnectCompletion(final ChannelFuture cf,final Promise<PooledConnection> callerPromise,final String httpMethod,final String uri,final int attemptNum){
        if (cf.isSuccess()) {
            createConnection(cf, callerPromise, httpMethod, uri, attemptNum);
        }else {
            callerPromise.setFailure(new OriginConnectException(cf.cause().getMessage(), OutboundErrorType.CONNECT_ERROR));
        }
    }


     /**
      * @desc   创建连接
      * @author chenjianyu944@gmail.com
      * @date   2020/9/11 22:40
      **/
     void createConnection(ChannelFuture cf, Promise<PooledConnection> callerPromise, String httpMethod, String uri,int attemptNum) {
        final PooledConnection conn = pooledConnectionFactory.create(cf.channel());
        conn.getChannel().read();
        onAcquire(conn);
        callerPromise.setSuccess(conn);
    }





    /**
     * @desc
     * @author chenjianyu944@gmail.com
     * @date   2020/9/12 9:44
     **/
    public PooledConnection tryGettingFromConnectionPool(EventLoop eventLoop){
        PooledConnection conn;
        Deque<PooledConnection> connections = getPoolForEventLoop(eventLoop);
        while ((conn = connections.poll()) != null) {
            conn.setInPool(false);
            /* Check that the connection is still open. */
            if (isValidFromPool(conn)) {
                return conn;
            }else {
                conn.close();
            }
        }
        return null;
    }


    
    public boolean isValidFromPool(PooledConnection conn) {
        return conn.isActive() && conn.getChannel().isOpen();
    }



    /**
     * @desc   连接到服务器
     * @author chenjianyu944@gmail.com
     * @date   2020/9/11 23:21
     **/
    public  ChannelFuture connectToServer(EventLoop eventLoop,String host) {
        return connectionFactory.connect(eventLoop, host, server.getPort());
    }




    public boolean release(PooledConnection conn) {
        if (conn == null) {
            return false;
        }
        if (conn.isInPool()) {
            return false;
        }

        // Get the eventloop for this channel.
        EventLoop eventLoop = conn.getChannel().eventLoop();
        Deque<PooledConnection> connections = getPoolForEventLoop(eventLoop);

        // Attempt to return connection to the pool.
        if (connections.offer(conn)) {
            conn.setInPool(true);
            return true;
        } else {
            // If the pool is full, then close the conn and discard.
            conn.close();
            conn.setInPool(false);
            return false;
        }
    }


     Deque<PooledConnection> getPoolForEventLoop(EventLoop eventLoop) {
        Deque<PooledConnection> pool = connectionsPerEventLoop.get(eventLoop);
        if (pool == null) {
            pool = new ConcurrentLinkedDeque<>();
            connectionsPerEventLoop.putIfAbsent(eventLoop, pool);
        }
        return pool;
    }


    public void shutdown() {
        for (Deque<PooledConnection> connections : connectionsPerEventLoop.values()) {
            for (PooledConnection conn : connections) {
                conn.close();
            }
        }
    }



    public String getHostFromServer(Server server){
        String host = server.getHost();
        return host;
    }

}
