package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.ui.jfx.JavafxFrameUtils;

public class RectImgDescrTreeViewTest {

    @Test
    public void testOpenView() {
        RectImgDescription model = RectImgDescrAnalyzerTest.analyseTstFile0();
        JavafxFrameUtils.openFrame("test", () -> RectImgDescrTreeView.createView(model));
    }
}
