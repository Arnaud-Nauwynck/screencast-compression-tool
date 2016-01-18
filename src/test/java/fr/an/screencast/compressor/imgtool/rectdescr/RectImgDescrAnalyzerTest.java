package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DebugDrawDecoratorRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DrawRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DumpRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;
import fr.an.screencast.ui.swing.ImageViewUtils;

public class RectImgDescrAnalyzerTest {

    private static final boolean DEBUG = 
//            true;
            false;
    
    private static final boolean DEBUG_UI = 
            true;
//            false;
    
    
    protected static ImageData img_screen1920x1080;
    
    @BeforeClass
    public static void prepareImg() throws IOException {
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080_color6bits();
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        int[] imgData = ImageRasterUtils.toInts(img);
        img_screen1920x1080 = new ImageData(dim, imgData);
    }

    public static RectImgDescr analyseTstFile0() {
        return analyseTstFile(ImageTstUtils.FILENAME_screen_eclipse_1920x1080);
    }
    
    public static RectImgDescr analyseTstFile(String imageFileName) {
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(sut.getDim());
        RectImgDescr res = sut.analyze(imgRect);
        return res;
    }
    
    @Test
    public void testAnalyze_screen_eclipse_1920x1080() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(imgRect);
        // Post-check
        Assert.assertNotNull(res);
        repaintAndCheckImg(imageFileName, sut, res);
    }

    
    @Test
    public void testAnalyze_screen_eclipse_1920x1080_color6bits() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080_color6bits;
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(imgRect);
        // Post-check
        Assert.assertNotNull(res);
        repaintAndCheckImg(imageFileName, sut, res);
    }

    @Test
    public void testScreenEclipse_titleBar() {
        // Prepare
        String imageFileName = "img-0,42-1920x14.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertTrue(res instanceof ColumnsSplitRectImgDescr);
        ColumnsSplitRectImgDescr res2 = (ColumnsSplitRectImgDescr) res;
        List<Segment> expectedSegments = Segment.parseSegmentList("[0-912(, [918-919(, [941-945(, [953-958(, [965-967(, [980-982(, [989-990(, [1003-1895(, [1904-1920(");
        Assert.assertEquals(expectedSegments, Arrays.asList(res2.getSplitBorders()));
        
        RectImgDescr[] columns = res2.getColumns();
        Assert.assertEquals(8, columns.length);
        // TODO more asserts..
        
        repaintAndCheckImg(imageFileName, sut, res);
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
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
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
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }

    @Test
    public void testAnalyze_splitVert2() {
        // Prepare
        String imageFileName = "img-split-vert2.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
    }
    
    @Test
    public void testAnalyze_splitHor() {
        // Prepare
        String imageFileName = "img-split-hor1.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        Assert.assertNotNull(res);
        // re-paint from RectImgDescr and check difference
        repaintAndCheckImg(imageFileName, sut, res);
        // ImageDataAssert.assertEquals(checkImgData, sut.getImgData(), sut.getDim());
    }
    
    @Test
    public void testAnalyze_splitHor2() {
        // Prepare
        String imageFileName = "img-split-hor2.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
    }
    
    @Test
    public void testAnalyze_border() {
        // Prepare
        String imageFileName = "img-eclipse-icon-view-minimize.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
    }

    @Test
    public void testAnalyze_border2() {
        // Prepare
        String imageFileName = "img-eclipse-icon-view-minimize2.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect rect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(rect);
        // Post-check
        repaintAndCheckImg(imageFileName, sut, res);
    }

    @Test
    public void testAnalyze_screen_egit_merge() {
        // Prepare
        String imageFileName = "screen-egit-merge.png";
        RectImgDescrAnalyzer sut = prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(sut.getDim());
        // Perform
        RectImgDescr res = sut.analyze(imgRect);
        // Post-check
        Assert.assertNotNull(res);
        repaintAndCheckImg(imageFileName, sut, res);
    }

    
    // ------------------------------------------------------------------------
    
    private void repaintAndCheckImg(String imageFileName, RectImgDescrAnalyzer sut, RectImgDescr res) {
        Dim dim = sut.getDim();
        BufferedImage checkImg = new BufferedImage(dim.getWidth(), dim.getHeight(), BufferedImage.TYPE_INT_RGB);
        DrawRectImgDescrVisitor drawVisitor = new DrawRectImgDescrVisitor(checkImg);
        res.accept(drawVisitor);
        int[] checkImgData = ImageRasterUtils.toInts(checkImg);
        ImageRasterUtils.fillAlpha(checkImgData);
        ImageTstUtils.saveImg(checkImg, new File("src/test/imgs/" + "check-" + imageFileName));
        int alphaMask = RGBUtils.rgb2Int(255, 255, 255, 0);

        Pt diffPt = ImageDataAssert.findFirstPtDiffMask(checkImgData, sut.getImgData(), dim.getWidth(), dim.getHeight(), alphaMask);
        if (diffPt != null) {
            System.out.println("found difference pt:" + diffPt);
            int ptIdx = diffPt.y * dim.width + diffPt.x;
            int expectedColor = sut.getImgData()[ptIdx]; 
            int actualColor = checkImgData[ptIdx]; 
            System.out.println("expecting color:" + RGBUtils.toString(expectedColor) + ", got " + RGBUtils.toString(actualColor));
            System.out.println("Dump crossing pt");
            DumpRectImgDescrVisitor.dumpTo(System.out, res, Rect.newPtDim(diffPt, new Dim(1,1)));
            System.out.println();
            
            // redo for debug
            checkImgData[ptIdx] = 0;
            for(int i = 0; i < checkImgData.length; i++) {
                checkImgData[i] = 0;
            }
            res.accept(drawVisitor);
        }

        if (DEBUG) {
            System.out.println("Full Dump");
            DumpRectImgDescrVisitor.dumpTo(System.out, res);
            
            File glyphsDir = new File("target/glyphs-" + imageFileName);
            if (! glyphsDir.exists()) {
                glyphsDir.mkdirs();
            }
            sut.getGlyphMRUTable().debugDumpGlyphs(glyphsDir);
        }

        if (DEBUG_UI) {
            ImageViewUtils.openImageFrame(checkImg);

            BufferedImage debugImage = BufferedImageUtils.copyImage(checkImg);
            res.accept(new DebugDrawDecoratorRectImgDescrVisitor(debugImage));
            
            ImageViewUtils.openImageFrame(debugImage);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
        ImageDataAssert.assertEqualsMask(checkImgData, sut.getImgData(), dim.getWidth(), dim.getHeight(), alphaMask);
    }
    
    public static RectImgDescrAnalyzer prepareAnalyzeImage(String imageFileName) {
        BufferedImage img = ImageTstUtils.loadImg(new File("src/test/imgs/" + imageFileName));
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        RectImgDescrAnalyzer sut = new RectImgDescrAnalyzer(dim);
        sut.setImg(ImageRasterUtils.toInts(img));
        return sut;
    }
    
}
