package com.lpf.im.group1.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

/**
 * im的服务端
 *
 * @author lipengfei
 * @create 2019-05-13 16:04
 **/
public class NettyServer {

    public static void main(String[] args) throws Exception {
        new NettyServer().run();
    }

    private static final int HOST_PORT = 8888;

    public void run() throws Exception {


        // 进行服务器端的启动处理
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(5);
        System.out.println("服务器启动成功，监听端口为：" + HOST_PORT);
        try {
            // 服务器端启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 设置要使用的线程池以及当前的Channel类型为：主从多线程
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketChannelInitaializer());

            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = serverBootstrap.bind(HOST_PORT).sync();
            future.channel().closeFuture().sync();// 等待Socket被关闭
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
