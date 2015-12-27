package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;

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
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class RectImgDescrDetectorHelperTest {

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
    
}
