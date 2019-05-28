package com.lpf.im.group1.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理
 *
 * @author lipengfei
 * @create 2019-05-14 16:04
 **/
public class MessageSender {
    private Logger logger = LoggerFactory.getLogger(MessageSender.class.getName());
    private ChannelGroup channels;
    private BiMap<String, ChannelId> biMap;

    private MessageSender() {
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        biMap = HashBiMap.create(16);
    }

    private static class MessageSenderHolder {
        private static MessageSender INSTANCE = new MessageSender();
    }

    public static MessageSender getInstance() {
        return MessageSenderHolder.INSTANCE;
    }

    public void addChannel(Channel channel) {
        channels.add(channel);
        System.out.println("有人加入群聊组，" + channels.name());
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel);
        String userId = biMap.inverse().get(channel.id());
        if (StringUtils.isNotBlank(userId)) {
            System.out.println("用户：" + userId + "离开im");
        } else {
            System.out.println("未注册用户离开im");
        }
    }


    public void register(String uuid, ChannelId channelId) {
        if (biMap.containsKey(uuid)) {
            System.out.println("用户：" + uuid + ",在其他端已登录");
            return;
        }
        try {
            biMap.put(uuid, channelId);
            System.out.println("用户：" + uuid + "上线了");
        } catch (IllegalArgumentException e) {
            // value已存在时，put()方法会报异常
            String oldUid = biMap.inverse().get(channelId);
            System.out.println("请在本端，退出已登录的用户：" + oldUid);
        }

    }

    public void unRegister(String uuid) {
        biMap.remove(uuid);
        System.out.println("用户：" + uuid + "下线了");
    }

    public void sendMsg(MessageInfo msgInfo) {
        String senderId = msgInfo.getSenderId();
        String recvId = msgInfo.getRecvId();
        if (recvId == null) {
            return;
        }

        Channel channel = channels.find(biMap.get(recvId));
        if (channel == null) {
            unRegister(recvId);
            logger.debug("No user");
            return;
        }

        String msg = "用户" + senderId + "对你说：" + msgInfo.getContent();
        channel.writeAndFlush(new TextWebSocketFrame(msg));
        logger.debug("Send success");
    }

    public void sendMsg(String recvId, String msg) {
        Channel channel = channels.find(biMap.get(recvId));
        if (channel == null) {
            unRegister(recvId);
            logger.debug("No user");
            return;
        }

        channel.writeAndFlush(new TextWebSocketFrame(msg));
        logger.debug("Send success");
    }


    public void getOnLineUsers(String uuid) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("当前在线用户：", JSON.parseArray(JSON.toJSONString(biMap.keySet())));
        sendMsg(uuid, jsonObject.toJSONString());
    }

    public void broadcast(MessageInfo msgInfo) {
        // 向ChannelGroup中所有channel发送消息
        channels.writeAndFlush(new TextWebSocketFrame("系统广播消息：" + msgInfo.getContent()));
    }
}
