package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.util.bits.BitInputStream;
import fr.an.util.bits.InputStreamToBitInputStream;
import fr.an.util.encoder.structio.BitStreamStructDataInput;
import fr.an.util.encoder.structio.DebugStructDataInput;
import fr.an.util.encoder.structio.StructDataInput;

public class BitStreamInputRectImgDescrVisitorTest {

    
    // cf corresponding test to run before, to fill input file..
    @Test
    public void testReadTopLevel_screen_eclipse_1920x1080() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            imageFileName = "DEBUG-" + imageFileName;
        }
        
        File inputFile = new File("target/test/rectimg-" + imageFileName + ".dat");
        boolean forceRewrite = false;
        if (forceRewrite || ! inputFile.exists()) {
            // run corresponding encoder test before decoding!
            BitStreamOutputRectImgDescrVisitorTest encoderTest = new BitStreamOutputRectImgDescrVisitorTest();
            encoderTest.testWriteTopLevel_screen_eclipse_1920x1080();
            Assert.assertTrue(inputFile.exists());
        }
        byte[] resultBytes;
        try {
            resultBytes = FileUtils.readFileToByteArray(inputFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file " + inputFile, e);
        }
        
//        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(imageFileName);
//        Rect imgRect = Rect.newDim(analyzer.getDim());
//        GlyphMRUTable glyphMRUTable = analyzer.getGlyphMRUTable();
//        RectImgDescription checkImgRectDescr = analyzer.analyze(imgRect);

        RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();

        ByteArrayInputStream buffer = new ByteArrayInputStream(resultBytes);
        BitInputStream bitInput = new InputStreamToBitInputStream(buffer);
        StructDataInput bitStructInput = new BitStreamStructDataInput(bitInput);

        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            codecConfig.setDebugAddMarkers(true);
            bitStructInput = new DebugStructDataInput(new BufferedReader(new InputStreamReader(buffer)));
        }

        BitStreamInputRectImgDescrVisitor sut = new BitStreamInputRectImgDescrVisitor(codecConfig, bitStructInput);
        // Perform
        RectImgDescription res = sut.readTopLevel();
        // Post-check
        Assert.assertNotNull(res);
        // System.out.println("decoded img descr " + imageFileName + " ");
    }

    
}
