package com.dahuaboke.meeting;

import com.dahuaboke.meeting.websocket.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author dahua
 * @time 2021/9/16 16:59
 */
public class MeetingMain {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup subGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer());
        ChannelFuture channelFuture = serverBootstrap.bind("127.0.0.1", 8000).sync();
        channelFuture.channel().closeFuture().sync();
    }
}
