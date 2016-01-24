package fr.an.screencast.ui.swing.imgtool.rectdescr;

import javax.swing.JFrame;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.ui.swing.JFrameUtils;

public class RectImgDescrJTreeTest {

    @Test
    public void testOpenView() {
        RectImgDescr model = RectImgDescrAnalyzerTest.analyseTstFile0();
        JFrame frame = JFrameUtils.openFrame("test", () -> RectImgDescrJTree.createView(model));
        frame.dispose();
    }

}
