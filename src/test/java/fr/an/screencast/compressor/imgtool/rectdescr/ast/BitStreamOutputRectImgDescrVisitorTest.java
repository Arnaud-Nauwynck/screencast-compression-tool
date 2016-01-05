package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

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

public class BitStreamOutputRectImgDescrVisitorTest {

    private static final boolean DEBUG = false;
    
    @Test
    public void testWriteTopLevel_screen_eclipse_1920x1080() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(analyzer.getDim());
        RectImgDescription imgRectDescr = analyzer.analyze(imgRect);

        RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(buffer);
        BitStreamStructDataOutput bitStructOut = new BitStreamStructDataOutput(bitOut);
        BitStreamOutputRectImgDescrVisitor sut = new BitStreamOutputRectImgDescrVisitor(codecConfig, bitStructOut);
        // Perform
        sut.writeTopLevel(imgRectDescr);
        // Post-check
        byte[] resultBytes = buffer.toByteArray();
        int resultLen = resultBytes.length;
        // System.out.println("encoding img " + imageFileName + " " + analyzer.getDim() + " => rect descr bytes: " + resultLen);
        Assert.assertTrue(resultLen <= 2000); // amazing compressions for 1920x1080 rgb image !!

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
