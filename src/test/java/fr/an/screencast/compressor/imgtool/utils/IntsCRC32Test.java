package fr.an.screencast.compressor.imgtool.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

@SuppressWarnings("restriction")
public class IntsCRC32Test {

    private static final boolean DEBUG = false;
    
    @Test
    public void testCrc32InMemory_bytes() {
        // Prepare
        byte[] data = new byte[] { 0 }; //, 1, 2, 3, 4, 5, };
        // Perform
        long res = IntsCRC32.crc32InMemory(data, 0, data.length);
        // Post-check
        int checkRes = sun.security.krb5.internal.crypto.crc32.byte2crc32(data);
        Assert.assertEquals(checkRes, res);
        
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        int checkRes2 = (int) crc32.getValue();
        Assert.assertEquals(checkRes2, res);
    }

    @Test
    public void testCrc32InMemory() throws Exception {
        // Prepare
        int[] intValues = new int[] { 0x12345678 };
        // Perform
        int res = IntsCRC32.crc32InMemory(intValues, 0, intValues.length);
        // Post-check
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(buffer);
        for (int v : intValues) {
            dout.writeInt(v);
        }
        byte[] bytes = buffer.toByteArray();
        int checkRes = sun.security.krb5.internal.crypto.crc32.byte2crc32(bytes);
        Assert.assertEquals(checkRes, res);
        
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        int checkRes2 = (int) crc32.getValue();
        Assert.assertEquals(checkRes2, res);
    }
  
    @Test
    public void testCrc32_100() {
        // Prepare
        int[] intValues = generateIntArray(100);
        // Perform
        int res = IntsCRC32.crc32(intValues, 0, intValues.length);
        // Post-check
        int expected = IntsCRC32.crc32InMemory(intValues, 0, intValues.length);
        Assert.assertEquals(expected, res);
    }

    @Test
    public void testCrcUpdateSegment() {
        // Prepare
        int[] data = new int[] { 2, 3, 4, 3, 4, 5 };
        ByteBuffer bb = ByteBuffer.allocate(data.length * 4);
//        for (int i = 0; i < data.length; i++) {
//            bb.putInt(data[i]);
//        }
//        bb.flip();
        CRC32 crc = new CRC32();
        // Perform
        IntsCRC32.crcUpdateSegment(crc, data, 0, data.length, bb);
        int res = (int) crc.getValue();
        // Post-check
        int checkCrc = IntsCRC32.crc32InMemory(data, 0, data.length);
        Assert.assertEquals(checkCrc, res);
    }
    
    
    @Test
    public void testCrc32_10000() {
        // Prepare
        int[] intValues = generateIntArray(10000);
        // Perform
        int res = IntsCRC32.crc32(intValues, 0, intValues.length);
        // Post-check
        int expected = IntsCRC32.crc32InMemory(intValues, 0, intValues.length);
        Assert.assertEquals(expected, res);
    }
        
        
    @Test
    public void testBenchmark() {
        if (DEBUG) {
            System.out.println("first execute (no hotspot yet...)");
        }
        doBenchmarkCrc32Ints(10, 1000);
        if (DEBUG) {
            System.out.println();
        }
        
        doBenchmarkCrc32Ints(10, 1000);
        doBenchmarkCrc32Ints(50, 1000);
        doBenchmarkCrc32Ints(100, 1000);
        doBenchmarkCrc32Ints(250, 1000);
        doBenchmarkCrc32Ints(500, 1000);
        doBenchmarkCrc32Ints(1000, 1000);
        doBenchmarkCrc32Ints(5000, 1000);
        doBenchmarkCrc32Ints(10000, 1000);
    }

    private static int[] generateIntArray(int len) {
        int[] intValues = new int[len];
        for (int i = 0; i < len; i++) {
            intValues[i] = i;
        }
        return intValues;
    }

    private void doBenchmarkCrc32Ints(int arrayLen, final int countRepeat) {
        int[] intValues = generateIntArray(arrayLen);
        
        int expected = IntsCRC32.crc32InMemory(intValues, 0, intValues.length);
        
        long startTime = System.currentTimeMillis();
        for (int nbRepeat = 0; nbRepeat < countRepeat; nbRepeat++) {
            int res = IntsCRC32.crc32(intValues, 0, intValues.length);
            Assert.assertEquals(expected, res);
        }
        long time = System.currentTimeMillis() - startTime;
        
        
        long slowStartTime = System.currentTimeMillis();
        for (int nbRepeat = 0; nbRepeat < countRepeat; nbRepeat++) {
            int res = IntsCRC32.crc32InMemory(intValues, 0, intValues.length);
            Assert.assertEquals(expected, res);
        }
        long slowTime = System.currentTimeMillis() - slowStartTime;
        
        // compare with..
        long startTime2 = System.currentTimeMillis();
        for (int nbRepeat = 0; nbRepeat < countRepeat; nbRepeat++) {
            CRC32 crc = new CRC32();
            // byte[] bytesData = new byte[intValues.length * 4];
            ByteBuffer bb = ByteBuffer.allocate(intValues.length * 4);
            for (int i = 0; i < intValues.length; i++) {
                bb.putInt(intValues[i]);
            }
            byte[] bytesData = bb.array();
            crc.update(bytesData);
            int res = (int) crc.getValue();
            Assert.assertEquals(expected, res);
        }
        long time2 = System.currentTimeMillis() - startTime2;

        // compare with..
        // ByteBuffer bb = ByteBuffer.allocate(intValues.length * 4);
        byte[] bytesData = new byte[intValues.length * 4];
        long startTime3 = System.currentTimeMillis();
        for (int nbRepeat = 0; nbRepeat < countRepeat; nbRepeat++) {
            CRC32 crc = new CRC32();
            ByteBuffer bb = ByteBuffer.wrap(bytesData);
            for (int i = 0; i < intValues.length; i++) {
                bb.putInt(intValues[i]);
            }
            crc.update(bytesData);
            int res = (int) crc.getValue(); 
            Assert.assertEquals(expected, res);
        }
        long time3 = System.currentTimeMillis() - startTime3;

        if (DEBUG) {
            System.out.println("data[" + arrayLen + "] x repeat:" + countRepeat 
                + " => IntsCRC32: " + time 
                + " pure-java:" + slowTime 
                + ", alloc+convert-byte[]+CRC32:" + time2 
                + ", convert-byte[]+CRC32:" + time3);
        }
    }

    @Test
    public void testCrc32ImgRect() {
        // Prepare
        Dim dim = new Dim(4, 3);
        int[] img = new int[] {
            0, 1, 2, 3, //
            1, 2, 3, 4, //
            2, 3, 4, 5 //
        };
        Dim roiDim = new Dim(3, 2);
        Rect rect = Rect.newPtDim(new Pt(1, 1), roiDim);
        // Perform
        int crc = IntsCRC32.crc32ImgRect(dim, img, rect); 
        // Post-check
        int[] glyphData = ImageRasterUtils.getCopyData(dim, img, rect);
        ImageDataAssert.assertEquals(new int[] { 2, 3, 4, 3, 4, 5 }, glyphData, roiDim);
        int checkCrc = IntsCRC32.crc32(glyphData, 0, glyphData.length);
        Assert.assertEquals(checkCrc, crc);
    }
    
    
}
