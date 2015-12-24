package fr.an.screencast.compressor.imgstream.codecs.deltabitstream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.util.bits.BitOutputStream;
import fr.an.util.bits.OutputStreamToBitOutputStream;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;

public class ImgVarLengthBackgroundBitStreamEncoderTest {

    private static final boolean DEBUG = false;

    @Test
    public void testWriteImgData_divideVarLengthWithBg_screen_eclipse_1920x1080() {
        // Prepare
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080();
        int[] imgData = ImageRasterUtils.toInts(img);
        int backgroundColor = RGBUtils.greyRgb2Int(255);
        
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(byteBuffer);
        BitStreamStructDataOutput bitStructOut = new BitStreamStructDataOutput(bitOut );
        ImgVarLengthBackgroundBitStreamEncoder sut = new ImgVarLengthBackgroundBitStreamEncoder(bitStructOut );
        // Perform
        sut.writeImgData_divideVarLengthWithBg(imgData, backgroundColor);
        // Post-check
        if (DEBUG) {
            System.out.println(sut);
        }
    }


}
