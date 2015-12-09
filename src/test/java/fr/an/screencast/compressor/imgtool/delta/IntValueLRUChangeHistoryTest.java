package fr.an.screencast.compressor.imgtool.delta;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.delta.IntValueLRUChangeHistory;

public class IntValueLRUChangeHistoryTest {

    @Test
    public void testAddTimeValue() {
        int frameIndex = 1;
        int expectedChgCount = 0;
        IntValueLRUChangeHistory sut = new IntValueLRUChangeHistory(2);

        // addValue 1 => lru:[v0(t:1)]
        int value0 = 100;
        sut.addTimeValue(frameIndex++, value0);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        Assert.assertEquals(1, sut.getNthPrevFrameIndex(0));
        Assert.assertEquals(value0, sut.getNthPrevValue(0));

        // same value (no change) => lru:[v0(t:1)]   t:2 unchanged
        sut.addTimeValue(frameIndex++, value0);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        Assert.assertEquals(1, sut.getNthPrevFrameIndex(0));
        Assert.assertEquals(value0, sut.getNthPrevValue(0));
        
        // addValue => lru:[v1(t:3), v0(t:1)]
        int value1 = 101;
        sut.addTimeValue(frameIndex++, value1);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        Assert.assertEquals(value1, sut.getNthPrevValue(0));
        Assert.assertEquals(3, sut.getNthPrevFrameIndex(0));
        Assert.assertEquals(value0, sut.getNthPrevValue(1));
        Assert.assertEquals(1, sut.getNthPrevFrameIndex(1));
        
        // addValue => lru:[v2(t4), v1(t:3)]    rolled lost history v0(t:1)
        int value2 = 102;
        sut.addTimeValue(frameIndex++, value2);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        Assert.assertEquals(value2, sut.getNthPrevValue(0));
        Assert.assertEquals(4, sut.getNthPrevFrameIndex(0));
        Assert.assertEquals(value1, sut.getNthPrevValue(1));
        Assert.assertEquals(3, sut.getNthPrevFrameIndex(1));
        try {
            sut.getNthPrevFrameIndex(2);
            Assert.fail();
        } catch(IllegalArgumentException ex) {
            // OK!
        }
    }
}
