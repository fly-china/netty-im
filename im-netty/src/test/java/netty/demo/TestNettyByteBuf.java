package netty.demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.junit.Test;

/**
 * ByteBuf的API的使用
 *
 * @author lipengfei
 * @create 2019-05-21 19:46
 **/
public class TestNettyByteBuf {

    @Test
    public void testSlice() {

        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8);

        ByteBuf sliced = buf.slice(0, 5);
        System.out.println(sliced.toString(CharsetUtil.UTF_8));

        buf.setByte(0, (byte) 'J');
        System.out.println("旧Buf首位字符：" + buf.getByte(0));
        System.out.println("新Buf首位字符：" + sliced.getByte(0));
        // 只更改老buf中首位字符，发现新、老buf的首位字符都被更改更改成了'J'
        // 所以，说明slice()方法，新产生的buf和老buf还指向同一内存区，修改一处，另外一处也被更改
        // slice()方法无新的内存开销
    }


    @Test
    public void testCopy() {

        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8);

        ByteBuf sliced = buf.copy(0, 5);
        System.out.println(sliced.toString(CharsetUtil.UTF_8));

        buf.setByte(0, (byte) 'J');
        System.out.println("旧Buf首位字符：" + buf.getByte(0));
        System.out.println("新Buf首位字符：" + sliced.getByte(0));
        // 只更改老buf中首位字符，老buf的首位字符都被更改更改成了'J',新的并未被更改
        // 所以，说明copy()方法，新产生的buf和老buf还已无任何关联
        // copy()方法会有新的内存开销
    }

    @Test
    public void testGetAndSet() {
        // N的acs码为78
        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8);
        System.out.println("读指针：" + buf.readerIndex() + "---写指针：" + buf.writerIndex());

        System.out.println((char) buf.getByte(0)); // N

        buf.setByte(0, 'J');
        System.out.println((char) buf.getByte(0)); // J

        buf.setBoolean(0, false);
        System.out.println(buf.getByte(0)); // 0

        System.out.println("读指针：" + buf.readerIndex() + "---写指针：" + buf.writerIndex());
        // 与第一次打印的读写指针一致，说明get()和set()不会导致指针移动

        buf.setByte(0, 'N');// 恢复原貌
        byte readByte = buf.readByte();
        System.out.println((char) readByte); // N
        System.out.println("读指针：" + buf.readerIndex() + "---写指针：" + buf.writerIndex());
        // 读指针由0->1，说明read*()方法，使读指针后移

        buf.writeByte('A');
        System.out.println(buf.toString(CharsetUtil.UTF_8));
        System.out.println("读指针：" + buf.readerIndex() + "---写指针：" + buf.writerIndex());
        // 写指针由22->23，说明write*()方法，使写指针后移


        // 将读指针重置为0，首位字符则又变为N
        buf.readerIndex(0);
        System.out.println((char) buf.readByte()); // N
        System.out.println("读指针：" + buf.readerIndex() + "---写指针：" + buf.writerIndex());

        System.out.println("-------------------------------------");
        System.out.println("可容纳的字节数:" + buf.capacity());
        System.out.println("可以容纳的最大字节数:" + buf.maxCapacity());
        System.out.println("是否由一个字节数组支撑:" + buf.hasArray());
        System.out.println("-------------------------------------");


        System.out.println("当前可读字节数：" + buf.readableBytes());
        System.out.println(" buf.isReadable()=" + buf.isReadable());
        buf.readerIndex(buf.writerIndex());
        System.out.println("当前可读字节数：" + buf.readableBytes());
        System.out.println("设置readerIndex=writerIndex后buf.isReadable()=" + buf.isReadable());
    }

    @Test
    public void testHasArray() {

        ByteBuf buf = Unpooled.copiedBuffer("Netty in Action rocks!", CharsetUtil.UTF_8);

        if (buf.hasArray()) {
            // 说明使用的【堆缓冲区】
            byte[] array = buf.array();
            int offset = buf.arrayOffset() + buf.readerIndex(); // 计算第一个字节的偏移量
            int length = buf.readableBytes();   // 获取可读字节数

            System.out.println("buf.arrayOffset()=" + buf.arrayOffset());
            System.out.println("buf.readerIndex()=" + buf.readerIndex());
            System.out.println("buf.readableBytes()=" + buf.readableBytes());
//            handleArray(array, offset, length); // 处理数据
        }

        if (!buf.hasArray()) {
            // 说明使用的【直接缓冲区】,传输速度快。但是相对于基于堆的缓冲区，它们的分配和释放都较为昂贵
            int length = buf.readableBytes(); // 获取可读字节数
            byte[] array = new byte[length]; // 分配一个新的数组来保存具有该长度的字节数据
            buf.getBytes(buf.readerIndex(), array); // 将字节复制到该数组
//            handleArray(array, 0, length); // 处理数据
        }
    }


    @Test
    public void testByteBufAllocator() {
//        ByteBufAllocator byteBufAllocator = new UnpooledByteBufAllocator(false); // 分配堆缓冲区
        ByteBufAllocator byteBufAllocator = new UnpooledByteBufAllocator(true); // 分配直接缓冲区，不池化ByteBuf实例，并且在每次它被调用时都会返回一个新的实例
//        ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator(true); // 分配直接缓冲区,池化了ByteBuf的实例以提高性能并最大限度地减少内存碎片

//        ByteBuf byteBuf =  Unpooled.directBuffer(100); // 可直接使用Unpooled工具类声明ByteBuf，省去ByteBufAllocator的过程
        ByteBuf byteBuf = byteBufAllocator.buffer();

        byteBuf.writeBytes("Netty in Action rocks!".getBytes(CharsetUtil.UTF_8));

        System.out.println("引用计数：" + byteBuf.refCnt());
        System.out.println("是否为堆缓冲区：" + byteBuf.hasArray());

        byteBuf.retain();
        System.out.println("byteBuf.retain()后引用计数：" + byteBuf.refCnt());

        // 减少到该对象的活动引用。当减少到0 时，该对象被释放，并且该方法返回 true
        // 试图访问一个已经被释放的引用计数的对象，将会导致一个 IllegalReferenceCountException
        byteBuf.release(1);
        System.out.println("byteBuf.release(1)后引用计数：" + byteBuf.refCnt());

        if (!byteBuf.hasArray()) {
            // 说明使用的【直接缓冲区】,传输速度快。但是相对于基于堆的缓冲区，它们的分配和释放都较为昂贵
            int length = byteBuf.readableBytes(); // 获取可读字节数
            byte[] array = new byte[length]; // 分配一个新的数组来保存具有该长度的字节数据
            byteBuf.getBytes(byteBuf.readerIndex(), array); // 将字节复制到该数组
            System.out.println((char) array[0]);
//            handleArray(array, 0, length); // 处理数据
        }

    }
}
