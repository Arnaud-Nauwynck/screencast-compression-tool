package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.Graphics2DHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;
import fr.an.screencast.ui.ImageViewUtils;

public class RectImgDescrDetectorHelperTest {

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_UI = true;
    
    protected static ImageData img_screen1920x1080;
    
    @BeforeClass
    public static void prepareImg() throws IOException {
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080_color6bits();
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        int[] imgData = ImageRasterUtils.toInts(img);
        img_screen1920x1080 = new ImageData(dim, imgData);
    }

    @Test
    public void testDetectDilateBorder() {
        // Prepare
        Dim maxDim = new Dim(30, 30);
        Dim dim = img_screen1920x1080.getDim();
        Rect maxWithinRect = Rect.newDim(dim);
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(img_screen1920x1080.getData());
        Dim dim1 = new Dim(1, 1);
        Dim dim4 = new Dim(4, 4);
        // Perform&Post-check
        
        // pixel already in background
        doTestDetectDilateBorder(Rect.newPtToPt(10, 10, 11, 11), sut, Rect.newPtDim(new Pt(10, 10), dim1), maxDim, maxWithinRect);
        
        // pixel in "A" => rect around "A"
        doTestDetectDilateBorder(Rect.newPtToPt(11, 8, 22, 19), sut, Rect.newPtDim(new Pt(17, 15), dim4), maxDim, maxWithinRect);

        // pixel in "new icon" => rect around icon.... pb: non uniform background!! TODO ...
        doTestDetectDilateBorder(null, sut, Rect.newPtDim(new Pt(18, 114), dim4), maxDim, maxWithinRect);
        
        // pixel in "package icon" => rect around icon
        doTestDetectDilateBorder(Rect.newPtToPt(11, 159, 21, 173), sut, Rect.newPtDim(new Pt(15, 163), dim4), maxDim, maxWithinRect);
        
        // Post-check
    }

    private void doTestDetectDilateBorder(Rect expectedRect, RectImgDescrDetectorHelper sut, Rect rect, Dim maxDim, Rect maxWithinRect) {
        Rect res = sut.detectDilateBorder(rect, maxDim, maxWithinRect);
        if (expectedRect == null) {
            Assert.assertNull(res);
        } else {
            Assert.assertEquals(expectedRect, res);
        }
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

    @Test
    public void testScanListLargestBorderRightThenDown() {
        // Prepare
        Dim dim = new Dim(10, 3);
        int[] imgData = new int[] {
            // 1  2  3  4  5  6  7  8  9  10
            1, 1, 1, 0, 2, 2, 2, 2, 3, 3, //  0
            1, 1, 1, 0, 2, 2, 2, 2, 4, 4, //  1
            1, 1, 3, 0, 2, 0, 0, 2, 4, 4, //  2
        };
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        int retainRectMinW = 1, retainRectMinH = 1;
        Rect rect = Rect.newDim(dim);
        // Perform
        List<Rect> res = sut.scanListLargestBorderRightThenDown(rect, retainRectMinW, retainRectMinH);
        // Post-check
        Assert.assertNotNull(res);
        int i = 0;
        // line 0
        Assert.assertEquals(Rect.newPtDim(0, 0, 3, 2), res.get(i++));    // 1 1 1 
        Assert.assertEquals(Rect.newPtDim(3, 0, 1, 3), res.get(i++));    //       0
        Assert.assertEquals(Rect.newPtDim(4, 0, 4, 2), res.get(i++));    //         2 2 2 2 
        Assert.assertEquals(Rect.newPtDim(8, 0, 2, 1), res.get(i++));    //                 3 3
        // line 1
        Assert.assertEquals(Rect.newPtDim(8, 1, 2, 2), res.get(i++));    //                 4 4
        // line 2
        Assert.assertEquals(Rect.newPtDim(0, 2, 2, 1), res.get(i++));    // 1 1 
        if (1 > retainRectMinW || 1 > retainRectMinH) {
            Assert.assertEquals(Rect.newPtDim(2, 2, 1, 1), res.get(i++));//     3  
            Assert.assertEquals(Rect.newPtDim(4, 2, 5, 3), res.get(i++)); //         2
        }
        Assert.assertEquals(Rect.newPtToPt(5, 2, 7, 3), res.get(i++));    //           0 0   
        if (1 > retainRectMinW || 1 > retainRectMinH) {
            Assert.assertEquals(Rect.newPtDim(7, 2, 1, 1), res.get(i++));    //               2
        }
        Assert.assertEquals(i, res.size());
    }
    
    @Test
    public void testScanListLargestBorderRightThenDown_10x6() {
        // Prepare
        Dim dim = new Dim(10, 6);
        int[] imgData = new int[] {
            // 1  2  3  4  5  6  7  8  9  10
            1, 1, 1, 0, 2, 2, 2, 2, 3, 3, //  0
            1, 1, 1, 0, 2, 2, 2, 2, 4, 4, //  1
            1, 1, 3, 0, 2, 0, 0, 2, 4, 4, //  2
            0, 0, 4, 4, 4, 4, 4, 0, 0, 0, //  3
            1, 1, 1, 0, 2, 2, 2, 2, 3, 3, //  4 
            1, 1, 1, 0, 2, 2, 2, 2, 4, 4, //  5
        };
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        int retainRectMinW = 1, retainRectMinH = 1; 
        // Perform
        List<Rect> res = sut.scanListLargestBorderRightThenDown(Rect.newDim(dim), retainRectMinW, retainRectMinH);
        // Post-check
        Assert.assertNotNull(res);
        int i = 0;
        // line 0
        Assert.assertEquals(Rect.newPtDim(0, 0, 3, 2), res.get(i++));    // 1 1 1 
        Assert.assertEquals(Rect.newPtDim(3, 0, 1, 3), res.get(i++));    //       0
        Assert.assertEquals(Rect.newPtDim(4, 0, 4, 2), res.get(i++));    //         2 2 2 2 
        Assert.assertEquals(Rect.newPtDim(8, 0, 2, 1), res.get(i++));    //                 3 3
        // line 1
        Assert.assertEquals(Rect.newPtDim(8, 1, 2, 2), res.get(i++));    //                 4 4
        // line 2
        Assert.assertEquals(Rect.newPtDim(0, 2, 2, 1), res.get(i++));    // 1 1 
        if (1 > retainRectMinW || 1 > retainRectMinH) {
            Assert.assertEquals(Rect.newPtDim(2, 2, 1, 1), res.get(i++));//     3  
            Assert.assertEquals(Rect.newPtDim(4, 2, 5, 3), res.get(i++)); //         2
        }
        Assert.assertEquals(Rect.newPtDim(5, 2, 2, 1), res.get(i++));    //            0 0   
        if (1 > retainRectMinW || 1 > retainRectMinH) {
            Assert.assertEquals(Rect.newPtDim(7, 2, 1, 1), res.get(i++));    //               2
        }
        // line 3
        Assert.assertEquals(Rect.newPtDim(0, 3, 2, 1), res.get(i++)); // 0, 0
        Assert.assertEquals(Rect.newPtDim(2, 3, 5, 1), res.get(i++)); //       4 4 4 4 4
        Assert.assertEquals(Rect.newPtDim(7, 3, 3, 1), res.get(i++)); //                      0, 0, 0
        // line 4
        Assert.assertEquals(Rect.newPtDim(0, 4, 3, 2), res.get(i++));    // 1 1 1 
        Assert.assertEquals(Rect.newPtDim(3, 4, 1, 2), res.get(i++));    //       0
        Assert.assertEquals(Rect.newPtDim(4, 4, 4, 2), res.get(i++));    //         2 2 2 2 
        Assert.assertEquals(Rect.newPtDim(8, 4, 2, 1), res.get(i++));    //                 3 3
        // line 5
        Assert.assertEquals(Rect.newPtDim(8, 5, 2, 1), res.get(i++));    //                 4 4
    }
    
    
    @Test
    public void testDetectLineBreaksInScannedRightThenDownRects_uniformSplitColor() {
        Dim dim = new Dim(4, 3);
        int[] imgData = new int[] {
            // 1  2  3    
            1, 1, 1, 0, //  0
            2, 2, 2, 2, //  1
            1, 1, 2, 3, //  2
        };
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        int retainRectMinW = 1, retainRectMinH = 1; 
        Rect rect = Rect.newDim(dim);
        List<Rect> scannedRects = sut.scanListLargestBorderRightThenDown(rect, retainRectMinW, retainRectMinH);
        // Perform
        LinesSplitRectImgDescr res = sut.detectLineBreaksInScannedRightThenDownRects(rect, scannedRects);
        // Post-check
        Assert.assertNotNull(res);
        Segment[] splits = res.getSplitBorders();
        Assert.assertEquals(3, splits.length);
        Assert.assertEquals(new Segment(1,2), splits[0]);
        Assert.assertEquals(new Segment(3,3), splits[2]); // useless?
        RectImgDescription[] lines = res.getLines();
        Assert.assertEquals(2, lines.length);
        RectImgAboveRectImgDescr line0 = (RectImgAboveRectImgDescr) lines[0];
        Assert.assertEquals(scannedRects.subList(0, 1), Arrays.asList(line0.getAboveRects()));
        RectImgAboveRectImgDescr line1 = (RectImgAboveRectImgDescr) lines[1];
        Assert.assertEquals(scannedRects.subList(3, 5), Arrays.asList(line1.getAboveRects()));
    }
    
    @Test
    public void testDetectLineBreaksInScannedRightThenDownRects_10x6() {
        Dim dim = new Dim(10, 6);
        int[] imgData = new int[] {
            // 1  2  3  4  5  6  7  8  9  10
            1, 1, 1, 0, 2, 2, 2, 2, 3, 3, //  0
            1, 1, 1, 0, 2, 2, 2, 2, 4, 4, //  1
            1, 1, 3, 0, 2, 0, 0, 2, 4, 4, //  2
            0, 0, 4, 4, 4, 4, 4, 0, 0, 0, //  3
            1, 1, 1, 0, 2, 2, 2, 2, 3, 3, //  4 
            1, 1, 1, 0, 2, 2, 2, 2, 4, 4, //  5
        };
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        int retainRectMinW = 1, retainRectMinH = 1; 
        Rect rect = Rect.newDim(dim);
        List<Rect> scannedRects = sut.scanListLargestBorderRightThenDown(rect, retainRectMinW, retainRectMinH);
        // Perform
        LinesSplitRectImgDescr res = sut.detectLineBreaksInScannedRightThenDownRects(rect, scannedRects);
        // Post-check
        Assert.assertNotNull(res);
        Segment[] splits = res.getSplitBorders();
        Assert.assertEquals(3, splits.length);
        Assert.assertEquals(new Segment(3,3), splits[0]);
        Assert.assertEquals(new Segment(4,4), splits[1]);
        Assert.assertEquals(new Segment(6,6), splits[2]); // useless?
        RectImgDescription[] lines = res.getLines();
        Assert.assertEquals(3, lines.length);
        RectImgAboveRectImgDescr line0 = (RectImgAboveRectImgDescr) lines[0];
        Assert.assertEquals(scannedRects.subList(0, 7), Arrays.asList(line0.getAboveRects()));
        RectImgAboveRectImgDescr line1 = (RectImgAboveRectImgDescr) lines[1];
        Assert.assertEquals(scannedRects.subList(7, 10), Arrays.asList(line1.getAboveRects()));
        RectImgAboveRectImgDescr line2 = (RectImgAboveRectImgDescr) lines[2];
        Assert.assertEquals(scannedRects.subList(10, scannedRects.size()), Arrays.asList(line2.getAboveRects()));
    }
    
    @Test
    public void testScanListLargestBorderRightThenDown_screen_egit_merge() {
        doTestScanListLargestBorderRightThenDown("check-screen-egit-merge.png");
    }

    @Test
    public void testScanListLargestBorderRightThenDown_screen_eclipse() {
        doTestScanListLargestBorderRightThenDown(ImageTstUtils.FILENAME_screen_eclipse_1920x1080);
    }

    private void doTestScanListLargestBorderRightThenDown(String testFilename) {
        // Prepare
        BufferedImage img = ImageTstUtils.loadTestImg(testFilename);        
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        Rect rect = Rect.newDim(dim);
        int[] imgData = ImageRasterUtils.toInts(img);
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        int retainMinRectW = 10, retainMinRectH = 10;
        // Perform
        List<Rect> res = sut.scanListLargestBorderRightThenDown(rect, retainMinRectW, retainMinRectH);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG_UI) {
            Graphics2DHelper g2dHelper = new Graphics2DHelper(img);
            for(Rect r : res) {
                if (r.getWidth() < retainMinRectW) {
                    g2dHelper.setColorStroke(Color.ORANGE, 1);
                    if (r.getWidth() == 1) {
                        continue;
                    }
                } else if (r.getHeight() < retainMinRectH) {
                    g2dHelper.setColorStroke(Color.GREEN, 1);
                    if (r.getHeight() == 1) {
                        continue;
                    }
                } else {
                    g2dHelper.setColorStroke(Color.BLUE, 1);
                }
                g2dHelper.drawRect(r);
            }
            ImageViewUtils.openImageFrame(img);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    @Test
    public void testInnerBorderImgDescrAtBorder() {
        // Prepare
        Dim dim = new Dim(10, 6);
        int[] imgData = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //
            1, 1, 1, 1, 1, 1, 1, 1, 1, 0, //   
            2, 1, 1, 1, 1, 1, 1, 1, 1, 0, //   
            3, 1, 1, 1, 0, 0, 1, 1, 1, 0, //
            4, 1, 1, 1, 1, 1, 1, 1, 1, 0, //   
            5, 1, 1, 1, 1, 1, 1, 1, 1, 0, //   
        };
        RectImgDescrDetectorHelper sut = new RectImgDescrDetectorHelper(dim);
        sut.setImg(imgData);
        Rect borderRect = Rect.newPtToPt(1, 1, 9, 6);
        Assert.assertNotNull(sut.detectBorder1AtUL(new Pt(1, 1), new MutableDim(8, 4)));
        // Perform
        RectImgDescription res = sut.innerBorderImgDescrAtBorder(borderRect);
        // Post-check
        Assert.assertTrue(res instanceof BorderRectImgDescr);
        BorderRectImgDescr res2 = (BorderRectImgDescr) res;
        Assert.assertEquals(new Border(3, 3, 2, 2), res2.getBorder());
    }
    

}
