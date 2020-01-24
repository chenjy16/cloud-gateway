package org.cloud.gateway.transport.netty;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by cjy on 2020/1/24.
 */
public class ClientConnectionsShutdown
{
    private static final Logger LOG = LoggerFactory.getLogger(ClientConnectionsShutdown.class);
    private static final DynamicBooleanProperty ENABLED = new DynamicBooleanProperty("server.outofservice.connections.shutdown", false);
    private static final DynamicIntProperty DELAY_AFTER_OUT_OF_SERVICE_MS =
            new DynamicIntProperty("server.outofservice.connections.delay", 2000);

    private final ChannelGroup channels;
    private final EventExecutor executor;

    public ClientConnectionsShutdown(ChannelGroup channels, EventExecutor executor)
    {
        this.channels = channels;
        this.executor = executor;


    }



    /**
     * Note this blocks until all the channels have finished closing.
     */
    public void gracefullyShutdownClientChannels()
    {
        LOG.warn("Gracefully shutting down all client channels");
        try {


            // Mark all active connections to be closed after next response sent.
            LOG.warn("Flagging CLOSE_AFTER_RESPONSE on " + channels.size() + " client channels.");
            // Pick some arbitrary executor.
            PromiseCombiner closeAfterPromises = new PromiseCombiner(ImmediateEventExecutor.INSTANCE);
            for (Channel channel : channels)
            {
                ConnectionCloseType.setForChannel(channel, ConnectionCloseType.DELAYED_GRACEFUL);

                ChannelPromise closePromise = channel.pipeline().newPromise();
                channel.attr(ConnectionCloseChannelAttributes.CLOSE_AFTER_RESPONSE).set(closePromise);
                closeAfterPromises.add((Future<Void>) closePromise);
            }

            // Wait for all of the attempts to close connections gracefully, or max of 30 secs each.
            Promise<Void> combinedCloseAfterPromise = executor.newPromise();
            closeAfterPromises.finish(combinedCloseAfterPromise);
            combinedCloseAfterPromise.await(30, TimeUnit.SECONDS);

            // Close all of the remaining active connections.
            LOG.warn("Closing remaining active client channels.");
            List<ChannelFuture> forceCloseFutures = new ArrayList<>();
            channels.forEach(channel -> {
                if (channel.isActive()) {
                    ChannelFuture f = channel.pipeline().close();
                    forceCloseFutures.add(f);
                }
            });

            LOG.warn("Waiting for " + forceCloseFutures.size() + " client channels to be closed.");
            PromiseCombiner closePromisesCombiner = new PromiseCombiner(ImmediateEventExecutor.INSTANCE);
            closePromisesCombiner.addAll(forceCloseFutures.toArray(new ChannelFuture[0]));
            Promise<Void> combinedClosePromise = executor.newPromise();
            closePromisesCombiner.finish(combinedClosePromise);
            combinedClosePromise.await(5, TimeUnit.SECONDS);
            LOG.warn(forceCloseFutures.size() + " client channels closed.");
        }
        catch (InterruptedException ie) {
            LOG.warn("Interrupted while shutting down client channels");
        }
    }
}