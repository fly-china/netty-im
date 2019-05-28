package com.lpf.im.group1.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageInfo {
    // 上线：{"cmd":1, "senderId":"1"}
    // 发送消息：{"cmd":3, "senderId":"1", "recvId":"2",  "content":"你好，在吗？[鼓掌]"}
    // 广播：{"cmd":4,  "content":"全体成员，今晚吃鸡[xkl转圈]"}
    // 版本号
    private String version;

    // 请求接口命令字： 1-上线  2-下线  3-消息 4-广播
    private Integer cmd;

    // 发送人Id
    private String senderId;

    // 收件人Id
    private String recvId;

    // 消息类型（请求1，应答2，通知3，响应4  format）
    private String msgtype;

    //请求数据
    private String content;

    enum CMDEnum {
        ONLINE(1, "上线"),
        OFFLINE(2, "下线"),
        MESSAGE(3, "发送消息"),
        BROADCAST(4, "广播"),
        GET_ALL_ONLINE_USERS(5, "获取所有在线用户");

        int type;
        String name;

        CMDEnum(int type, String name) {
            this.name = name;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}