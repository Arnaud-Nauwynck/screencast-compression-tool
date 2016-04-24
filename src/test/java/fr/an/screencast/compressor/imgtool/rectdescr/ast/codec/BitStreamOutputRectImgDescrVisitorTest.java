package fr.an.screencast.compressor.imgtool.rectdescr.ast.codec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.an.bitwise4j.bits.BitOutputStream;
import fr.an.bitwise4j.bits.OutputStreamToBitOutputStream;
import fr.an.bitwise4j.encoder.structio.BitStreamStructDataOutput;
import fr.an.bitwise4j.encoder.structio.StructDataOutput;
import fr.an.bitwise4j.encoder.structio.helpers.DebugStructDataOutput;
import fr.an.bitwise4j.encoder.structio.helpers.DebugTeeStructDataOutput;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DumpRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Rect;

public class BitStreamOutputRectImgDescrVisitorTest {

    /*pp*/ static final boolean DEBUG = true;
    
    private static final Logger LOG = LoggerFactory.getLogger(BitStreamOutputRectImgDescrVisitorTest.class);

    @Test
    public void testWriteTopLevel_screen_eclipse_1920x1080() throws Exception {
        // Prepare
        String inputImageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        File outputFile = new File("target/test/rectimg-" + inputImageFileName + ".dat");
        File outputSerializedFile = new File("target/test/rectimg-" + inputImageFileName + ".ser");
        File debugOutputFile = new File("target/test/rectimg-" + inputImageFileName + "-dat-debug.txt");

        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(inputImageFileName);
        Rect imgRect = Rect.newDim(analyzer.getDim());
        RectImgDescr imgRectDescr = analyzer.analyze(imgRect);

        RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();
        if (BitStreamOutputRectImgDescrVisitor.DEBUG_MARK) {
            codecConfig.setDebugAddMarkers(true);
        }

        OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(fileOutputStream);
        StructDataOutput fileStructOut = new BitStreamStructDataOutput(bitOut);
        
        OutputStream debugFileOutputStream = new BufferedOutputStream(new FileOutputStream(debugOutputFile));
        DebugStructDataOutput debugStructOutput = new DebugStructDataOutput(new PrintStream(debugFileOutputStream));
        StructDataOutput structOut = new DebugTeeStructDataOutput(debugStructOutput, fileStructOut); 
        
        try {
            BitStreamOutputRectImgDescrVisitor sut = new BitStreamOutputRectImgDescrVisitor(codecConfig, structOut);
            // Perform
            sut.writeTopLevel(imgRectDescr);
            
            String debugCountersText = debugStructOutput.getCounters().toStringAllCounters();
            if (DEBUG) {
                System.out.println("counters:" + debugCountersText);
            }
            debugStructOutput.debugComment(debugCountersText);
        } finally {
            structOut.close();
        }
        // Post-check
        int outputFileLen = (int) outputFile.length();
        
        // check size in bytes (upper round for size in bits/8)
//        int nBits = 719171; // TODO .. get from...
//        int expectedBytesLen = (nBits+8-1)/8; 
//        Assert.assertEquals(expectedBytesLen, outputFileLen);
        
        LOG.info("encoded rect im descr for image " + inputImageFileName + " as compressed binary => " + outputFileLen + " bytes = " + (outputFileLen/1024) + " ko");
        // System.out.println("encoding img " + imageFileName + " " + analyzer.getDim() + " => rect descr bytes: " + resultLen);
        
        if (DEBUG) {
            File dumpFile = new File("target/test/rectimg-" + inputImageFileName + "-dump.txt");
            try {
                String dumpText = DumpRectImgDescrVisitor.dumpToString(imgRectDescr);
                FileUtils.write(dumpFile, dumpText);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file " + dumpFile, e);
            }
        }
        
        try (OutputStream serOut = new BufferedOutputStream(new FileOutputStream(outputSerializedFile))) {
            SerializationUtils.serialize(imgRectDescr, serOut);
        }

        // serialize to json to compare naive encoding (but quite efficient!!)
        // pb => should not repeat glyph data
        // pb => raw data not gzip encoded
        ObjectMapper jsonMapper = new ObjectMapper();
        String imgRectDescrJson = jsonMapper.writeValueAsString(imgRectDescr);
        File imgRectDescrJsonFile = new File("target/test/rectimg-" + inputImageFileName + ".json");
        try {
            FileUtils.write(imgRectDescrJsonFile, imgRectDescrJson);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file " + imgRectDescrJsonFile, e);
        }
        
    }

}
