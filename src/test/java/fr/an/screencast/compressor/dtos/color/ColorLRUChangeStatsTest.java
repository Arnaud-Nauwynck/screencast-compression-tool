package fr.an.screencast.compressor.dtos.color;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.dtos.color.ColorLRUChangeStats;

public class ColorLRUChangeStatsTest {

    @Test
    public void testAddRGB() {
        int frameIndex = 0;
        int expectedChgCount = 0;
        ColorLRUChangeStats sut = new ColorLRUChangeStats(4);
        
        // new 1 => lru:[1]
        sut.addRGB(1, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use
        sut.addRGB(1, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        
        // new 2 => lru:[1, 2]
        sut.addRGB(2, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use 
        sut.addRGB(1, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(2, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());

        // new 3 => lru:[1, 2, 3]
        sut.addRGB(3, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use 
        sut.addRGB(1, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(2, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(3, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());


        // new 4 => lru:[1, 2, 3, 4]
        sut.addRGB(4, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use 
        sut.addRGB(1, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(2, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(3, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(4, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());

        // new 5 => lru:[2, 3, 4, 5] ... remove from lru: 1 !!
        sut.addRGB(5, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use
        sut.addRGB(2, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(3, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(4, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(5, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        
        // new 1 ...not in lru => lru:[3, 4, 5, 1] ... remove from lru: 2 !!
        sut.addRGB(1, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // re-use
        sut.addRGB(3, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(4, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(5, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        sut.addRGB(1, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
    }
}
