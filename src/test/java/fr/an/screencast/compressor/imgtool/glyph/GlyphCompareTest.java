package fr.an.screencast.compressor.imgtool.glyph;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;

public class GlyphCompareTest {

    @Test
    public void testDiff() {
        BufferedImage img1 = ImageTstUtils.loadImg(new File("src/test/glyphs/glyph-77.png"));
        BufferedImage img2 = ImageTstUtils.loadImg(new File("src/test/glyphs/glyph-61.png"));
        Dim dim = new Dim(img1.getWidth(), img1.getHeight());
        
        int[] img1Data = ImageRasterUtils.toInts(img1);
        int[] img2Data = ImageRasterUtils.toInts(img2);
        
        int crc1 = IntsCRC32.crc32(img1Data, 0, img1Data.length);
        int crc2 = IntsCRC32.crc32(img1Data, 0, img1Data.length);
        if (crc1 != crc2) {
            System.out.println("CRC " + crc1 + " != " + crc2);
        } else {
            System.out.println("CRC: " + crc1 + " == " + crc2);
        }
        
        int idx = 0;
        for(int y=0; y < dim.height; y++) {
            for(int x = 0; x < dim.width; x++,idx++) {
                int c1 = img1Data[idx];
                int c2 = img2Data[idx];
                if (c1 != c2) {
                    System.out.print("[y:" + y + ",x:" + x +"] " + RGBUtils.toString(c1) + "!= " + RGBUtils.toString(c2) + " ");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }
}
