package fr.an.screencast.compressor.dtos.color;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.dtos.color.ColorMRUChangeStats;

public class ColorMRUChangeStatsTest {

    @Test
    public void testAddRGB() {
        int frameIndex = 0;
        int expectedChgCount = 0;
        ColorMRUChangeStats sut = new ColorMRUChangeStats(4);
        
        // new 1 => mru:[1]
        sut.addRGB(1, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check
        Assert.assertEquals(0, sut.findRGB(1));
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        
        // new 2 => mru:[2, 1]
        sut.addRGB(2, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check
        Assert.assertEquals(0, sut.findRGB(2));
        Assert.assertEquals(1, sut.findRGB(1));

        // new 3 => mru:[3, 2, 1]
        sut.addRGB(3, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check
        Assert.assertEquals(0, sut.findRGB(3));
        Assert.assertEquals(1, sut.findRGB(2));
        Assert.assertEquals(2, sut.findRGB(1));

        // new 4 => mru:[4, 3, 2, 1]
        sut.addRGB(4, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check 
        Assert.assertEquals(0, sut.findRGB(4));
        Assert.assertEquals(1, sut.findRGB(3));
        Assert.assertEquals(2, sut.findRGB(2));
        Assert.assertEquals(3, sut.findRGB(1));

        // new 5 => mru:[5, 4, 3, 2] ... remove from mru: 1 !!
        sut.addRGB(5, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check 
        Assert.assertEquals(-1, sut.findRGB(1));
        Assert.assertEquals(0, sut.findRGB(5));
        Assert.assertEquals(1, sut.findRGB(4));
        Assert.assertEquals(2, sut.findRGB(3));
        Assert.assertEquals(3, sut.findRGB(2));

        // increment 4 => mru:[4, 5, 3, 2] ... swap  
        sut.addRGB(4, frameIndex++);
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check 
        Assert.assertEquals(0, sut.findRGB(4));
        Assert.assertEquals(1, sut.findRGB(5));
        Assert.assertEquals(2, sut.findRGB(3));
        Assert.assertEquals(3, sut.findRGB(2));
        
        // new 1 ...not in mru => mru:[4, 1, 5, 3] ... insert before 5, remove 2 !!
        sut.addRGB(1, frameIndex++);
        expectedChgCount++;
        Assert.assertEquals(expectedChgCount, sut.getCountChange());
        // check 
        Assert.assertEquals(0, sut.findRGB(4));
        Assert.assertEquals(1, sut.findRGB(1));
        Assert.assertEquals(2, sut.findRGB(5));
        Assert.assertEquals(3, sut.findRGB(3));
    }
}
