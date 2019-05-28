package netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 绝对值编码器
 *
 * @author lipengfei
 * @create 2019-05-27 19:14
 **/
public class AbsIntegerEncoder extends MessageToMessageEncoder<ByteBuf> {


    // 这里是我把msg.readInt()方法提出来并加了打印的代码。我书上的其实没变化
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        while (msg.readableBytes() >= 4) {
            // 在while方法内部debug,会出现描述中的问题
            int readInt = msg.readInt();
            System.out.println(Thread.currentThread().getName()  + "读入的值：" + readInt);
            int value = Math.abs(readInt);
            out.add(value);
        }
    }


    // 这是《netty实战》的代码
//    @Override
//    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
//        while (msg.readableBytes() >= 4) {
//            int value = Math.abs(msg.readInt());
//            out.add(value);
//        }
//    }
}
