package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.ui.jfx.JavafxFrameUtils;

public class RectImgDescrTreeViewTest {

    @Test
    public void testOpenView() {
        RectImgDescr model = RectImgDescrAnalyzerTest.analyseTstFile0();
        JavafxFrameUtils.openFrame("test", () -> RectImgDescrTreeView.createView(model));
    }
}
