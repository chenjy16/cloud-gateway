package org.cloud.gateway.connections;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import org.cloud.gateway.netty.service.Server;
import java.net.InetSocketAddress;

public class NettyClientConnectionFactory {


    private final ChannelInitializer<? extends Channel> channelInitializer;


    public NettyClientConnectionFactory(ChannelInitializer<? extends Channel> channelInitializer) {
        this.channelInitializer = channelInitializer;
    }



    public ChannelFuture connect(final EventLoop eventLoop, String host, final int port) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);
        if (socketAddress.isUnresolved()) {
            //LOGGER.warn("NettyClientConnectionFactory got an unresolved address, host: {}, port: {}", host,  port);
        }

        final Bootstrap bootstrap = new Bootstrap()
                .channel(Server.defaultOutboundChannelType.get())
                .handler(channelInitializer)
                .group(eventLoop)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connPoolConfig.getConnectTimeout())
                .option(ChannelOption.SO_KEEPALIVE, connPoolConfig.getTcpKeepAlive())
                .option(ChannelOption.TCP_NODELAY, connPoolConfig.getTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, connPoolConfig.getTcpSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, connPoolConfig.getTcpReceiveBufferSize())
                .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, connPoolConfig.getNettyWriteBufferHighWaterMark())
                .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, connPoolConfig.getNettyWriteBufferLowWaterMark())
                .option(ChannelOption.AUTO_READ, connPoolConfig.getNettyAutoRead())
                .remoteAddress(socketAddress);
        return bootstrap.connect();
    }

}
