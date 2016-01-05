package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.util.bits.BitOutputStream;
import fr.an.util.bits.OutputStreamToBitOutputStream;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;

public class BitStreamOutputRectImgDescrVisitorTest {

    @Test
    public void testAnalyze_screen_eclipse_1920x1080() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(analyzer.getDim());
        RectImgDescription imgRectDescr = analyzer.analyze(imgRect);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(buffer);
        BitStreamStructDataOutput bitStructOut = new BitStreamStructDataOutput(bitOut);
        GlyphMRUTable glyphMRUTable = analyzer.getGlyphMRUTable();
        BitStreamOutputRectImgDescrVisitor sut = new BitStreamOutputRectImgDescrVisitor(bitStructOut, glyphMRUTable );
        // Perform
        sut.recursiveWriteTo(imgRectDescr);
        // Post-check
        byte[] resultBytes = buffer.toByteArray();
        int resultLen = resultBytes.length;
        // System.out.println("encoding img " + imageFileName + " " + analyzer.getDim() + " => rect descr bytes: " + resultLen);
        Assert.assertTrue(resultLen <= 1810); // only 1810 bytes !!
    }

}
