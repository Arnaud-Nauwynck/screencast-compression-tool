package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.color.ColorBitsReducer;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

public class RectImgDescrDetectorHelperTest {

    private static final boolean DEBUG = false;
    
    protected static ImageData img_screen1920x1080;
    
    @BeforeClass
    public static void prepareImg() {
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080();
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        int[] imgData = ImageRasterUtils.toInts(img);
        // clear least significant bits!!! (color 0;0;0 ~= 0;0;1 !!)
        ColorBitsReducer.maskLeastSignificantBits(imgData, 1);
        img_screen1920x1080 = new ImageData(dim, imgData);
    }

    @Test
    public void testDetectExactFillRect_screen1920x1080() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());

        // Perform
        Rect rect1 = Rect.newPtToPt(0, 0, 12, 27);
        FillRectImgDescr res1 = sut.detectExactFillRect(rect1);
        // Post-check
        Assert.assertNotNull(res1);

        // Perform
        Rect rect2 = Rect.newPtToPt(76, 0, 924, 27);
        FillRectImgDescr res2 = sut.detectExactFillRect(rect2);
        // Post-check
        Assert.assertNotNull(res2);
    }

    
    @Test
    public void testDetectBorder_screen1920x1080() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Pt pt = new Pt(0, 0);
        MutableDim rectDim = new MutableDim(dim.width, 27);
        // Perform
        BorderRectImgDescr res = sut.detectBorder1AtUL(pt, rectDim);
        // Post-check
        Assert.assertNotNull(res);
    }

    
    @Test @Ignore
    public void testDetectRoundCorner_screen1920x1080() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Pt pt = new Pt(0, 28);
        MutableDim rectDim = new MutableDim(dim.width, dim.height-pt.y);
        MutableDim topCornerDim = new MutableDim(10, 10);
        MutableDim bottomCornerDim = new MutableDim(0, 0);
        StringBuilder reason = new StringBuilder();
        // Perform
        RoundBorderRectImgDescr res = sut.detectRoundBorderStartAtULWithCorners(pt, rectDim, false, topCornerDim, bottomCornerDim, reason);
        // Post-check
        Assert.assertNotNull(res);
    }

    @Test
    public void testDetectVerticalBorderSplits_osTitleBar() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Rect rect = Rect.newPtToPt(0, 0, dim.getWidth(), 27);
        // Perform
        Map<Integer, List<Segment>> color2BorderSplits = sut.detectVerticalBorderSplits(rect);
        // Post-check
        Assert.assertNotNull(color2BorderSplits);
        Assert.assertEquals(1, color2BorderSplits.size());
        List<Segment> splitBorders = color2BorderSplits.values().iterator().next();
        Assert.assertEquals(82, splitBorders.size());
        Assert.assertEquals(new Segment(2, 12), splitBorders.get(0));
        Assert.assertEquals(new Segment(22, 23), splitBorders.get(1));
        Assert.assertEquals(new Segment(35, 36), splitBorders.get(2));
        Assert.assertEquals(new Segment(38, 39), splitBorders.get(3));
        // ...
        Assert.assertEquals(new Segment(1905, 1918), splitBorders.get(splitBorders.size()-1));
    }

    @Test
    public void testDetectVerticalBorderSplits_eclipseMenu() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Rect rect = Rect.newPtToPt(0, 30, dim.getWidth(), 100); // dim.getHeight());
        // Perform
        Map<Integer, List<Segment>> color2BorderSplits = sut.detectVerticalBorderSplits(rect);
        // Post-check
        Assert.assertNotNull(color2BorderSplits);
        Assert.assertEquals(1, color2BorderSplits.size());
        List<Segment> greySplitBorders = color2BorderSplits.get(RGBUtils.greyRgb2Int(236));
        Assert.assertEquals(63, greySplitBorders.size());
        Assert.assertEquals(new Segment(1, 8), greySplitBorders.get(0));
        Assert.assertEquals(new Segment(14, 16), greySplitBorders.get(1));
        // ..
        Assert.assertEquals(new Segment(1904, 1919), greySplitBorders.get(62));
    }

    
    @Test
    public void testDetectHorizontalBorderSplits() {
        // Prepare
        Dim dim = img_screen1920x1080.getDim();
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Rect rect = Rect.newPtToPt(0, 0, dim.getWidth(), dim.getHeight());
        // Perform
        Map<Integer, List<Segment>> color2BorderSplits = sut.detectHorizontalBorderSplits(rect);
        // Post-check
        Assert.assertNotNull(color2BorderSplits);
        if (DEBUG) {
            for(Map.Entry<Integer,List<Segment>> e : color2BorderSplits.entrySet()) {
                System.out.println("split color: " + RGBUtils.toString(e.getKey()) + " : " + e.getValue());
            }
        }
        Assert.assertEquals(3, color2BorderSplits.size());
        List<Segment> blackSplitBorders = color2BorderSplits.get(0);
        List<Segment> greySplitBorders = color2BorderSplits.get(RGBUtils.greyRgb2Int(236));
        List<Segment> grey2SplitBorders = color2BorderSplits.get(RGBUtils.greyRgb2Int(226));
        
        Assert.assertEquals(2, blackSplitBorders.size());
        Assert.assertEquals(new Segment(0, 5), blackSplitBorders.get(0));
        Assert.assertEquals(new Segment(20, 27), blackSplitBorders.get(1));

        Assert.assertEquals(3, greySplitBorders.size());
        Assert.assertEquals(new Segment(33, 42), greySplitBorders.get(0));
        Assert.assertEquals(new Segment(56, 74), greySplitBorders.get(1));
        Assert.assertEquals(new Segment(89, 93), greySplitBorders.get(2));
        
        Assert.assertEquals(1, grey2SplitBorders.size());
        Assert.assertEquals(new Segment(144, 147), grey2SplitBorders.get(0));
    }
    
}