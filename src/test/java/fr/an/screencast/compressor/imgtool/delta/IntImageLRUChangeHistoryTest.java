package fr.an.screencast.compressor.imgtool.delta;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.delta.IntImageLRUChangeHistory.FrameIndexPrevValue;
import fr.an.screencast.compressor.utils.Dim;

public class IntImageLRUChangeHistoryTest {

    @Test
    public void testAddTimeValue_1x1() {
        Dim dim = new Dim(1, 1);
        int[] img = new int[] {
            0, //
        };
        doTestAddValue(dim, img);
    }
    
    @Test
    public void testAddTimeValue_2x3() {
        Dim dim = new Dim(2, 3);
        int[] img = new int[] {
            1000, 2000, 3000, //
            4000, 5000, 6000
        };
        doTestAddValue(dim, img);
    }

    private void doTestAddValue(Dim dim, int[] img) {
        IntImageLRUChangeHistory sut = new IntImageLRUChangeHistory(dim, 2);
        FrameIndexPrevValue tmpchg = new FrameIndexPrevValue();

        for (int y = 0, idx=0; y < dim.height; y++) {
            for(int x = 0; x < dim.width; x++,idx++) {
                // code following is like IntValueLRUChangeHistoryTest, but for x,y pixel
                int frameIndex = 1;
                int expectedChgCount = 0;

                // addValue 1 => lru:[v0(t:1)]
                int value0 = img[idx] + 100;
                sut.addTimeValue(frameIndex++, idx, value0);
                expectedChgCount++;
                Assert.assertEquals(expectedChgCount, sut.getCountChange(idx));
                assertEquals(1, value0, sut.getNthChange(tmpchg, idx, 0));
                assertEquals(frameIndex, value0, sut.findPrevFrameIndex(tmpchg, idx, frameIndex));
                
                // same value (no change) => lru:[v0(t:1)]   t:2 unchanged
                sut.addTimeValue(frameIndex++, idx, value0);
                Assert.assertEquals(expectedChgCount, sut.getCountChange(idx));
                assertEquals(1, value0, sut.getNthChange(tmpchg, idx, 0));
                assertEquals(frameIndex, value0, sut.findPrevFrameIndex(tmpchg, idx, frameIndex));
                assertEquals(frameIndex, value0, sut.findPrevFrameIndex(tmpchg, idx, frameIndex-1));
                
                // addValue => lru:[v1(t:3), v0(t:1)]
                int value1 = img[idx] + 101;
                sut.addTimeValue(frameIndex++, idx, value1);
                expectedChgCount++;
                Assert.assertEquals(expectedChgCount, sut.getCountChange(idx));
                assertEquals(3, value1, sut.getNthChange(tmpchg, idx, 0));
                assertEquals(1, value0, sut.getNthChange(tmpchg, idx, 1));

                // addValue => lru:[v2(t4), v1(t:3)]    rolled lost history v0(t:1)
                int value2 = img[idx] + 102;
                sut.addTimeValue(frameIndex++, idx, value2);
                expectedChgCount++;
                Assert.assertEquals(expectedChgCount, sut.getCountChange(idx));
                assertEquals(4, value2, sut.getNthChange(tmpchg, idx, 0));
                assertEquals(3, value1, sut.getNthChange(tmpchg, idx, 1));
                try {
                    sut.getNthChange(tmpchg, idx, 2);
                    Assert.fail();
                } catch(IllegalArgumentException ex) {
                    // OK!
                }
                
            }
        }
    }

    private static void assertEquals(int expectedFrameIndex, int expectedValue, FrameIndexPrevValue actual) {
        Assert.assertEquals(expectedFrameIndex, actual.frameIndex);
        Assert.assertEquals(expectedValue, actual.prevValue);
    }
}
