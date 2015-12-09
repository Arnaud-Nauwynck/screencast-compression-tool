package fr.an.screencast.compressor.imgtool.delta;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.delta.IntValueMRUChangeHistory;

public class IntValueMRUChangeHistoryTest {

    @Test
        public void testAddValue() {
            int frameIndex = 0;
            int expectedChgCount = 0;
            IntValueMRUChangeHistory sut = new IntValueMRUChangeHistory(4);
            
            // new 1 => mru:[1]
            sut.addValue(1, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check
            Assert.assertEquals(0, sut.findValue(1));
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            
            // new 2 => mru:[2, 1]
            sut.addValue(2, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check
            Assert.assertEquals(0, sut.findValue(2));
            Assert.assertEquals(1, sut.findValue(1));
    
            // new 3 => mru:[3, 2, 1]
            sut.addValue(3, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check
            Assert.assertEquals(0, sut.findValue(3));
            Assert.assertEquals(1, sut.findValue(2));
            Assert.assertEquals(2, sut.findValue(1));
    
            // new 4 => mru:[4, 3, 2, 1]
            sut.addValue(4, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check 
            Assert.assertEquals(0, sut.findValue(4));
            Assert.assertEquals(1, sut.findValue(3));
            Assert.assertEquals(2, sut.findValue(2));
            Assert.assertEquals(3, sut.findValue(1));
    
            // new 5 => mru:[5, 4, 3, 2] ... remove from mru: 1 !!
            sut.addValue(5, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check 
            Assert.assertEquals(-1, sut.findValue(1));
            Assert.assertEquals(0, sut.findValue(5));
            Assert.assertEquals(1, sut.findValue(4));
            Assert.assertEquals(2, sut.findValue(3));
            Assert.assertEquals(3, sut.findValue(2));
    
            // increment 4 => mru:[4, 5, 3, 2] ... swap  
            sut.addValue(4, frameIndex++);
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check 
            Assert.assertEquals(0, sut.findValue(4));
            Assert.assertEquals(1, sut.findValue(5));
            Assert.assertEquals(2, sut.findValue(3));
            Assert.assertEquals(3, sut.findValue(2));
            
            // new 1 ...not in mru => mru:[4, 1, 5, 3] ... insert before 5, remove 2 !!
            sut.addValue(1, frameIndex++);
            expectedChgCount++;
            Assert.assertEquals(expectedChgCount, sut.getCountChange());
            // check 
            Assert.assertEquals(0, sut.findValue(4));
            Assert.assertEquals(1, sut.findValue(1));
            Assert.assertEquals(2, sut.findValue(5));
            Assert.assertEquals(3, sut.findValue(3));
        }
}
