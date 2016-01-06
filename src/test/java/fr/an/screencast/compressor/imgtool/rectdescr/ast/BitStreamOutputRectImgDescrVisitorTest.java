package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.util.bits.BitOutputStream;
import fr.an.util.bits.OutputStreamToBitOutputStream;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;
import fr.an.util.encoder.structio.DebugStructDataOutput;
import fr.an.util.encoder.structio.StructDataOutput;

public class BitStreamOutputRectImgDescrVisitorTest {

    private static final boolean DEBUG = true;
    
    @Test
    public void testWriteTopLevel_screen_eclipse_1920x1080() {
        // Prepare
        String inputImageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        String imageFileName = inputImageFileName;
        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            imageFileName = "DEBUG-" + imageFileName;
        }

        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(inputImageFileName);
        Rect imgRect = Rect.newDim(analyzer.getDim());
        RectImgDescription imgRectDescr = analyzer.analyze(imgRect);

        RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(buffer);
        StructDataOutput bitStructOut = new BitStreamStructDataOutput(bitOut);
        
        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            codecConfig.setDebugAddMarkers(true);
            bitStructOut = new DebugStructDataOutput(new PrintStream(buffer));
        }
        
        BitStreamOutputRectImgDescrVisitor sut = new BitStreamOutputRectImgDescrVisitor(codecConfig, bitStructOut);
        // Perform
        sut.writeTopLevel(imgRectDescr);
        // Post-check
        byte[] resultBytes = buffer.toByteArray();
        int resultLen = resultBytes.length;
        // System.out.println("encoding img " + imageFileName + " " + analyzer.getDim() + " => rect descr bytes: " + resultLen);
        if (!BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            Assert.assertTrue(resultLen <= 2000); // amazing compressions for 1920x1080 rgb image !!
        } else {
            // big file for debug dump text 
            Assert.assertTrue(100000 < resultLen && resultLen <= 150000);
        }
        
        File outputFile = new File("target/test/rectimg-" + imageFileName + ".dat");
        try {
            FileUtils.writeByteArrayToFile(outputFile, resultBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file " + outputFile, e);
        }
        
        if (DEBUG) {
            File dumpFile = new File("target/test/rectimg-" + imageFileName + "-dump.txt");
            try {
                String dumpText = DumpRectImgDescrVisitor.dumpToString(imgRectDescr);
                FileUtils.write(dumpFile, dumpText);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file " + outputFile, e);
            }
        }
    }

}
