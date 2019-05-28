package netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * netty单元测试
 *
 * @author lipengfei
 * @create 2019-05-28 10:06
 **/
public class TestNetty {

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
