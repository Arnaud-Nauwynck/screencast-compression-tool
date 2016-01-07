package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.util.bits.BitInputStream;
import fr.an.util.bits.InputStreamToBitInputStream;
import fr.an.util.encoder.structio.BitStreamStructDataInput;
import fr.an.util.encoder.structio.StructDataInput;
import fr.an.util.encoder.structio.helpers.DebugStructDataInput;
import fr.an.util.encoder.structio.helpers.DebugTeeStructDataInput;

public class BitStreamInputRectImgDescrVisitorTest {

    private static final boolean DEBUG = BitStreamOutputRectImgDescrVisitorTest.DEBUG;
    
    // cf corresponding test to run before, to fill input file..
    @Test
    public void testReadTopLevel_screen_eclipse_1920x1080() throws Exception {
        // Prepare
        String inputImageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        File inputFile = new File("target/test/rectimg-" + inputImageFileName + ".dat");
        File debugInputFile = null;
        if (DEBUG) {
            debugInputFile = new File("target/test/rectimg-DEBUG-" + inputImageFileName + ".txt");
        }
        
        boolean forceRewrite = false;
        if (forceRewrite || ! inputFile.exists()) {
            // run corresponding encoder test before decoding!
            BitStreamOutputRectImgDescrVisitorTest encoderTest = new BitStreamOutputRectImgDescrVisitorTest();
            encoderTest.testWriteTopLevel_screen_eclipse_1920x1080();
            Assert.assertTrue(inputFile.exists());
        }

        RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();
        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            codecConfig.setDebugAddMarkers(true);
        }

        StructDataInput bitStructInput = null;
        try {
            InputStream fileInput = new BufferedInputStream(new FileInputStream(inputFile));
            BitInputStream bitInput = new InputStreamToBitInputStream(fileInput);
            bitStructInput = new BitStreamStructDataInput(bitInput);
    
            if (DEBUG) {
                BufferedReader debugFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(debugInputFile)));
                StructDataInput debugStructInput = new DebugStructDataInput(debugFileReader);
                bitStructInput = new DebugTeeStructDataInput(debugStructInput, bitStructInput);
            }
    
            BitStreamInputRectImgDescrVisitor sut = new BitStreamInputRectImgDescrVisitor(codecConfig, bitStructInput);
            // Perform
            RectImgDescription res = sut.readTopLevel();
            // Post-check
            Assert.assertNotNull(res);
            // System.out.println("decoded img descr " + imageFileName + " ");
        } finally {
            bitStructInput.close();
        }
    }

    
}
