package fr.an.screencast.compressor;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.BlockImageUtils;

public class BlockImageUtilsTest {

    @Test
    public void testBase2Exp() {
        Assert.assertEquals(1, BlockImageUtils.base2Exp(2, true));
        Assert.assertEquals(2, BlockImageUtils.base2Exp(3, false));
        Assert.assertEquals(2, BlockImageUtils.base2Exp(4, true));
        Assert.assertEquals(10, BlockImageUtils.base2Exp(1023, false));
        Assert.assertEquals(10, BlockImageUtils.base2Exp(1024, true));
    }
}
