package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.DrawRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.DumpRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

public class RectImgDescrAnalyzerTest {

    private static final boolean DEBUG = true;
    
    protected static ImageData img_screen1920x1080;
    
    @BeforeClass
    public static void prepareImg() throws IOException {
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080_color6bits();
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        int[] imgData = ImageRasterUtils.toInts(img);
        img_screen1920x1080 = new ImageData(dim, imgData);
    }

    @Test
    public void testAnalyze() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrAnalyzer sut = new RectImgDescrAnalyzer(dim);
        sut.setImg(img_screen1920x1080.getData());
        Rect imgRect = Rect.newDim(dim);
        // Perform
        RectImgDescription res = sut.analyze(imgRect);
        // Post-check
        Assert.assertNotNull(res);
    }

    @Test
    public void testScreenEclipse_titleBar() {
        // Prepare
        String imageFileName = "img-0,42-1920x14.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        Assert.assertTrue(res instanceof ColumnsSplitRectImgDescr);
        ColumnsSplitRectImgDescr res2 = (ColumnsSplitRectImgDescr) res;
        List<Segment> expectedSegments = Segment.parseSegmentList("[0-912(, [918-919(, [941-945(, [953-958(, [965-967(, [980-982(, [989-990(, [1003-1895(, [1904-1920(");
        Assert.assertEquals(expectedSegments, Arrays.asList(res2.getSplitBorders()));
        
        RectImgDescription[] columns = res2.getColumns();
        Assert.assertEquals(8, columns.length);
        // TODO more asserts..
        
        repainAndCheckImg(imageFileName, sut, res);
        // sut.getGlyphMRUTable().debugDumpGlyphs(new File("target/dumpE"));
        Assert.assertEquals(8, sut.getGlyphMRUTable().size());
    }

    @Test
    public void testScreenEclipse_titleBar_repeated() {
        // Prepare
        String imageFileName = "img-title-repeated-0,42-1920x14.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        repainAndCheckImg(imageFileName, sut, res);
        // sut.getGlyphMRUTable().debugDumpGlyphs(new File("target/dumpE"));
        Assert.assertEquals(8, sut.getGlyphMRUTable().size());
    }
    
    @Test
    public void testAnalyze_splitVert() {
        // Prepare
        String imageFileName = "img-split-vert1.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        // re-paint from RectImgDescr and check difference
        repainAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }

    @Test
    public void testAnalyze_splitVert2() {
        // Prepare
        String imageFileName = "img-split-vert2.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        // re-paint from RectImgDescr and check difference
        repainAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }
    
    @Test
    public void testAnalyze_splitHor() {
        // Prepare
        String imageFileName = "img-split-hor1.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        // re-paint from RectImgDescr and check difference
        repainAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }
    
    @Test
    public void testAnalyze_splitHor2() {
        // Prepare
        String imageFileName = "img-split-hor2.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescription res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
        }
        // re-paint from RectImgDescr and check difference
        repainAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }
    
    
    private void repainAndCheckImg(String imageFileName, RectImgDescrAnalyzer sut, RectImgDescription res) {
        BufferedImage checkImg = new BufferedImage(sut.getDim().getWidth(), sut.getDim().getHeight(), BufferedImage.TYPE_INT_RGB);
        DrawRectImgDescrVisitor drawVisitor = new DrawRectImgDescrVisitor(checkImg, sut.getGlyphMRUTable());
        res.accept(drawVisitor);
        int[] checkImgData = ImageRasterUtils.toInts(checkImg);
        ImageRasterUtils.fillAlpha(checkImgData);
        ImageTstUtils.saveImg(checkImg, new File("src/test/imgs/" + "check-" + imageFileName));
        int alphaMask = RGBUtils.rgb2Int(255, 255, 255, 0);
        ImageDataAssert.assertEqualsMask(checkImgData, sut.getImgData(), sut.getDim().getWidth(), sut.getDim().getHeight(), alphaMask);
    }
    
    private static RectImgDescrAnalyzer prepareAnalyzeImage(String imageFileName) {
        BufferedImage img = ImageTstUtils.loadImg(new File("src/test/imgs/" + imageFileName));
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        RectImgDescrAnalyzer sut = new RectImgDescrAnalyzer(dim);
        sut.setImg(ImageRasterUtils.toInts(img));
        return sut;
    }
    
}
