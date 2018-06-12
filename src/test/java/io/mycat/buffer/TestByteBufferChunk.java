package io.mycat.buffer;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Rolandz
 * Date: 2018/6/6
 * Time: 15:51
 */
public class TestByteBufferChunk {
    int pageSize = 1024;
    int chunkSize = 1024 * 8;

    @Test
    public void testAllocate() {
        ByteBufferChunk chunk = new ByteBufferChunk(pageSize, chunkSize);

        for (int reqCapacity = 1; reqCapacity <= chunkSize; reqCapacity++) {
            ByteBuffer bb = chunk.allocateRun(reqCapacity);

            Assert.assertNotNull("reqCapacity: " + reqCapacity, bb);
            int pagesNeeded = 0;
            if (reqCapacity % pageSize == 0) {
                pagesNeeded = (reqCapacity / pageSize);
            }
            else {
                pagesNeeded = (reqCapacity / pageSize + 1);
            }

            int actualPages = 0;
            if (Integer.numberOfLeadingZeros(pagesNeeded) + Integer.numberOfTrailingZeros(pagesNeeded) == 31) {
                actualPages = pagesNeeded;
            }
            else {
                actualPages = (1 << (32 - Integer.numberOfLeadingZeros(pagesNeeded)));
            }

            Assert.assertEquals("reqCapacity: " + reqCapacity, actualPages * pageSize, bb.capacity());

            chunk.freeByteBuffer(bb);
        }

        Assert.assertEquals(0, chunk.usage());
    }

    @Test
    public void testAllocateByPageSize() {
        ByteBufferChunk chunk = new ByteBufferChunk(pageSize, chunkSize);
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        for (int i = 0; i < chunkSize / pageSize; i++) {
            ByteBuffer bb = chunk.allocateRun(pageSize);
            Assert.assertNotNull(bb);
            byteBuffers.add(bb);
        }

        for (ByteBuffer bb: byteBuffers) {
            chunk.freeByteBuffer(bb);
        }

        Assert.assertEquals(0, chunk.usage());
    }

    @Test
    public void testAllocateByPageSizeTimes2() {
        ByteBufferChunk chunk = new ByteBufferChunk(pageSize, chunkSize);
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        for (int i = 0; i < chunkSize / pageSize / 2; i++) {
            ByteBuffer bb = chunk.allocateRun(pageSize * 2);
            Assert.assertNotNull(bb);
            byteBuffers.add(bb);
        }

        for (ByteBuffer bb: byteBuffers) {
            chunk.freeByteBuffer(bb);
        }

        Assert.assertEquals(0, chunk.usage());
    }

    @Test
    public void testAllocateByPageSizeTimes3() {
        ByteBufferChunk chunk = new ByteBufferChunk(pageSize, chunkSize);
        List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
        for (int i = 0; i < chunkSize / pageSize / 3; i++) {
            ByteBuffer bb = chunk.allocateRun(pageSize * 3);
            Assert.assertNotNull(bb);
            byteBuffers.add(bb);
        }

        Assert.assertNull(chunk.allocateRun(pageSize));

        for (ByteBuffer bb: byteBuffers) {
            chunk.freeByteBuffer(bb);
        }

        Assert.assertEquals(0, chunk.usage());
    }

    @Test
    public void testAllocate2() {
        ByteBufferChunk chunk = new ByteBufferChunk(pageSize, chunkSize);
        ByteBuffer bb = chunk.allocateRun(chunkSize);
        chunk.freeByteBuffer(bb);
        bb = chunk.allocateRun(4098);
        chunk.freeByteBuffer(bb);
    }

    @Test
    public void testIntegerOpt() {
        System.out.println(Integer.numberOfLeadingZeros(1));
        System.out.println(Integer.numberOfLeadingZeros(2));
        System.out.println(Integer.numberOfLeadingZeros(Integer.MAX_VALUE));
        System.out.println(Integer.numberOfLeadingZeros(-1));
    }

    @Test
    public void testLog2() {
        System.out.println(log2(1025));
        System.out.println(log2(1024));
        System.out.println(log2(1023));
    }

    static int log2(int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException();
        }
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(chunkSize);
    }
}
