package com.lpf.im.group2.server;

import com.lpf.im.group2.model.MsgInfoProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

//处理文本协议数据，处理TextWebSocketFrame类型的数据，websocket专门处理文本的frame就是TextWebSocketFrame
public class ServerFrameHandler extends SimpleChannelInboundHandler<MsgInfoProto.MsgInfo> {

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //读到客户端的内容并且向客户端去写内容
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgInfoProto.MsgInfo msg) throws Exception {
        // channelGroup.add();

        Channel channel = ctx.channel();
        System.out.println(msg.getContent());
        System.out.println(msg.getCmd());
        System.out.println(msg.getSenderId());
        System.out.println(msg.getRecvId());

        MsgInfoProto.MsgInfo respMsg = MsgInfoProto.MsgInfo.newBuilder()
                .setCmd(MsgInfoProto.MsgInfo.CMDEnum.ONLINE)
                .setRecvId("小芳")
                .setSenderId("sys")
                .build();

        channel.writeAndFlush(respMsg);
    }

    //每个channel都有一个唯一的id值
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //打印出channel唯一值，asLongText方法是channel的id的全名
        // System.out.println("handlerAdded："+ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // System.out.println("handlerRemoved：" + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getStackTrace());
        System.out.println("异常发生");
        ctx.close();
    }

}
