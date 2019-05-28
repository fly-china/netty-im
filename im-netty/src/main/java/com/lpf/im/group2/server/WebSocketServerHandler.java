package com.lpf.im.group2.server;

import com.alibaba.fastjson.JSONObject;
import com.lpf.im.group1.server.MessageInfo;
import com.lpf.im.group1.server.MessageSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private Logger logger = LoggerFactory.getLogger(MessageSender.class.getName());
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;


    /**
     * 读取到客户端的消息
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        MessageSender.getInstance().addChannel(ctx.channel());
        System.out.println(ctx.channel().localAddress().toString() + " 通道已激活！");
    }

    /**
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的了。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        MessageSender.getInstance().removeChannel(ctx.channel());
        System.out.println(ctx.channel().localAddress().toString() + " 通道已断开！");
    }

    /**
     * 功能：读空闲时移除Channel
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      /*  if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (evnet.state().equals(IdleState.READER_IDLE)) {
                UserInfoManager.removeChannel(ctx.channel());
            }
        }*/
        ctx.fireUserEventTriggered(evt);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (!GET.equals(req.method())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/".equals(req.uri())) {
            ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpUtil.setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.uri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true, 5 * 1024 * 1024);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            // 返回应答消息
            String request = ((TextWebSocketFrame) frame).text();
            MessageInfo msgInfo = null;
            try {
                msgInfo = JSONObject.parseObject(request, MessageInfo.class);
                // TODO:处理消息
              /*  if (MessageInfo.CMDEnum.ONLINE.type == msgInfo.getCmd()) {
                    handleRegister(ctx, msgInfo);
                } else if (MessageInfo.CMDEnum.OFFLINE.type == msgInfo.getCmd()) {
                    handleUnRegister(ctx, msgInfo);
                } else if (MessageInfo.CMDEnum.MESSAGE.type == msgInfo.getCmd()) {
                    handleMessage(ctx, msgInfo);
                } else if (MessageInfo.CMDEnum.BROADCAST.type == msgInfo.getCmd()) {
                    handleBroadcast(ctx, msgInfo);
                } else if (MessageInfo.CMDEnum.GET_ALL_ONLINE_USERS.type == msgInfo.getCmd()) {
                    handleGetOnLineUsers(ctx, msgInfo);
                } else {
                    ctx.channel().write(new TextWebSocketFrame("消息类型非法！！！"));
                }*/
                ctx.channel().write(new TextWebSocketFrame("文本格式报文"));
            } catch (Exception e) {
                ctx.channel().write(new TextWebSocketFrame("消息格式非法！！！"));
            }

        }
        if (frame instanceof BinaryWebSocketFrame) {
            // Echo the frame
            ctx.write(frame.retain());
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        if (System.getProperty("ssl") != null) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }


    private void handleRegister(ChannelHandlerContext ctx, MessageInfo msgInfo) {
        MessageSender.getInstance().register(msgInfo.getSenderId(), ctx.channel().id());
        String successMsg = "系统：用户" + msgInfo.getSenderId() + "上线成功";
        ctx.channel().writeAndFlush(new TextWebSocketFrame(successMsg));
    }

    private void handleUnRegister(ChannelHandlerContext ctx, MessageInfo msgInfo) {
        MessageSender.getInstance().unRegister(msgInfo.getSenderId());
        String successMsg = "系统：用户" + msgInfo.getSenderId() + "下线成功";
        ctx.channel().writeAndFlush(new TextWebSocketFrame(successMsg));
    }

    private void handleMessage(ChannelHandlerContext ctx, MessageInfo msgInfo) {
        MessageSender.getInstance().sendMsg(msgInfo);
    }

    private void handleBroadcast(ChannelHandlerContext ctx, MessageInfo msgInfo) {
        MessageSender.getInstance().broadcast(msgInfo);
    }

    private void handleGetOnLineUsers(ChannelHandlerContext ctx, MessageInfo msgInfo) {
        MessageSender.getInstance().getOnLineUsers(msgInfo.getSenderId());
    }
}
