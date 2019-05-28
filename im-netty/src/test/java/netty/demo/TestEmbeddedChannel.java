package netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ByteBuf的API的使用
 *
 * @author lipengfei
 * @create 2019-05-21 19:46
 **/
public class TestEmbeddedChannel {

    @Test
    public void testFramesDecoded() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }

        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));
        ByteBuf input = buf.duplicate();

        // 将数据写入EmbeddedChannel
        assertTrue(channel.writeInbound(input.retain()));
        // 标记Channel为完成状态
        assertTrue(channel.finish());

        // 读取所生成的消息，并且验证是否有 3 帧（切片），其中每帧（切片）
        ByteBuf read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();

        assertNull(channel.readInbound());
        buf.release();

    }


    @Test
    public void testFramesDecoded2() {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));

        /**
         * 操作：将数据写入EmbeddedChannel
         * 预期结果：返回 false，因为没有一个完整的可供读取的帧
         * 因为对writeInbound的定义是：如果可以通过 readInbound()方法从 EmbeddedChannel 中读取数据，则返回 true
         */
        assertFalse(channel.writeInbound(input.readBytes(2)));
        assertTrue(channel.writeInbound(input.readBytes(7)));
        assertTrue(channel.finish());

        ByteBuf read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();

        read = (ByteBuf) channel.readInbound();
        System.out.println("当前read输出字字节，第三位为：" + read.getByte(2));
        assertEquals(buf.readSlice(3), read);
        read.release();


        assertNull(channel.readInbound());
        buf.release();
    }


    /**
     * TODO：AbsIntegerEncoder内部断点，debug模式下，不能正常读到-9至-1的9个数；run模式没问题
     * TODO：暂时没找到原因
     */
    @Test
    public void testFramesEncoder() {

        ByteBuf buf = Unpooled.buffer(256);
        for (int i = 1; i < 10; i++) {
            buf.writeInt(i * -1);
        }

        EmbeddedChannel channel = new EmbeddedChannel(new AbsIntegerEncoder());
        // 写入 ByteBuf
        assertTrue(channel.writeOutbound(buf));
        assertTrue(channel.finish());
        // read bytes
        for (int i = 1; i < 10; i++) {
            int readNum = (int) channel.readOutbound();
            System.out.println(readNum);
            assertEquals(i, readNum);
        }
        assertNull(channel.readOutbound());

    }

}
