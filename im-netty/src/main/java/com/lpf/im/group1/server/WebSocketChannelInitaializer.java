package com.lpf.im.group1.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * WebSocket的channel初始化
 *
 * @author lipengfei
 * @create 2019-05-13 16:28
 **/
public class WebSocketChannelInitaializer extends ChannelInitializer<SocketChannel> {

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        ChannelPipeline pipeline = socketChannel.pipeline();

        //HttpServerCodec: 针对http协议进行编解码
        pipeline.addLast(new HttpServerCodec());
        // ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
        pipeline.addLast(new ChunkedWriteHandler());
        /**
         * 由于netty是基于分段请求的，它的作用是将一个Http的消息组装成一个完成的HttpRequest或者HttpResponse
         * 具体的是什么,取决于是请求还是响应, 该Handler必须放在HttpServerCodec后的后面
         */
        pipeline.addLast(new HttpObjectAggregator(8192));

        /**
         *  用于处理websocket, 握手和close、ping、pong都在此处理
         *
         *  /ws为访问websocket时的uri
         */
//        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket", null, true, 65535));


        // 自定义的处理器
        pipeline.addLast(new WebSocketServerHandler());


    }
}
