package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.ui.swing.JFrameUtils;

public class RectImgDescrViewTest {

    @Test
    public void testOpenView() {
        BufferedImage img = ImageTstUtils.loadTestImg(ImageTstUtils.FILENAME_screen_eclipse_1920x1080);
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        RectImgDescrAnalyzer descrAnalyzer = new RectImgDescrAnalyzer(dim);
        descrAnalyzer.setImg(ImageRasterUtils.toInts(img));
        Rect rect = Rect.newDim(dim);
        RectImgDescr rectDescr = descrAnalyzer.analyze(rect);
        
        JFrame frame = JFrameUtils.openFrame("test", () -> {
            RectImgDescrView view = new RectImgDescrView(img, rectDescr);
            return view.getJComponent();
        });
        frame.dispose();
    }

}
