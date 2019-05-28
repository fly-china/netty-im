package com.lpf.im.group2.server;

import com.lpf.im.group2.model.MsgInfoProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

/**
 * Protobuf消息的处理类
 *
 * @author lipengfei
 * @create 2019-05-16 19:53
 **/
public class ProtobufMessageHandler  extends SimpleChannelInboundHandler<Object> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        final MsgInfoProto.MsgInfo message = (MsgInfoProto.MsgInfo) msg;   //这里获取消息

        String stringUtf8 = message.getContent().toStringUtf8();
        ctx.channel().writeAndFlush(new TextWebSocketFrame(stringUtf8));


    }
}
