package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
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
        // File inputSerializedFile = new File("target/test/rectimg-" + inputImageFileName + ".ser");
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

        RectImgDescription res;
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
            res = sut.readTopLevel();
        } finally {
            bitStructInput.close();
        }

        // Post-check
        Assert.assertNotNull(res);

        // read checkRes from serialized file, then deep compare object tree 
//        RectImgDescription checkRes;
//        try (InputStream serIn = new BufferedInputStream(new FileInputStream(inputSerializedFile))) {
//            checkRes = (RectImgDescription) SerializationUtils.deserialize(serIn);
//        }
//        // deep compare equals...
//        // TODO?
        
        // dump again to debug text file...  compare debug text
        File decodeDumpFile = new File("target/test/rectimg-" + inputImageFileName + "-encodedecode-dump.txt");
        try {
            String dumpText = DumpRectImgDescrVisitor.dumpToString(res);
            FileUtils.write(decodeDumpFile, dumpText);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file " + decodeDumpFile, e);
        }

        File checkDumpFile = new File("target/test/rectimg-" + inputImageFileName + "-dump.txt");
        Assert.assertTrue(FileUtils.contentEquals(checkDumpFile, decodeDumpFile));
        
        // inversely... re-serialize res and compare serialisation bytes with check file  (no clue on assert error...)
        // byte[] serializedRes = SerializationUtils.serialize(res);
        // byte[] checkSerializedRes = FileUtils.readFileToByteArray(inputSerializedFile);
        // Assert.assertTrue(Arrays.equals(checkSerializedRes, serializedRes)); // TODO does not work??
    }

    
}
