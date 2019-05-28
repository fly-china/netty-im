package com.lpf.im.group2.model;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author lipengfei
 * @create 2019-05-16 14:48
 **/
public class ProtocDemo {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        MsgInfoProto.MsgInfo.Builder builder = MsgInfoProto.MsgInfo.newBuilder();
        builder.setVersion("1.0.0");
        builder.setCmd(MsgInfoProto.MsgInfo.CMDEnum.ONLINE);
        builder.setSenderId("user1");
        builder.setRecvId("user2");
        byte[] contentByte = "你好吗？".getBytes();
        builder.setContent(ByteString.copyFrom(contentByte));

        MsgInfoProto.MsgInfo msg = builder.build();
        System.out.println(msg.toString());

        System.out.println("===========Person Byte==========");
        for (byte b : msg.toByteArray()) {
            System.out.print(b);
        }
        System.out.println();
        System.out.println(msg.toByteString());
        System.out.println("================================");

        //模拟接收Byte[]，反序列化成Person类
        byte[] byteArray = msg.toByteArray();

        MsgInfoProto.MsgInfo decodeModel = MsgInfoProto.MsgInfo.parseFrom(byteArray);
        System.out.println("after :" + decodeModel);

        JSONObject object = new JSONObject();
        object.put("version", "1.0.0");
        object.put("cmd", "ONLINE");
        object.put("senderId", "user1");
        object.put("recvId", "user2");
        object.put("content", "你好吗？");
        String string = object.toJSONString();
        byte[] bytes = string.getBytes();
        System.out.println("protoc二进制长度：" + byteArray.length);
        System.out.println("json二进制长度：" + bytes.length);

    }
}
