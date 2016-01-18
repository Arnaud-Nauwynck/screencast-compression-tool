package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzerTest;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.RectImgDescrBitsCountEstimater.SynthetisedBitsCount;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.utils.Rect;

public class RectImgDescrBitsCountEstimaterTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrBitsCountEstimaterTest.class);
    
    private static boolean DEBUG = true;
    
    
    @Test
    public void testRecursiveEvalSynthetisedStatsBitsCount() {
        // Prepare
        String imageFileName = ImageTstUtils.FILENAME_screen_eclipse_1920x1080;
        RectImgDescrAnalyzer analyzer = RectImgDescrAnalyzerTest.prepareAnalyzeImage(imageFileName);
        Rect imgRect = Rect.newDim(analyzer.getDim());
        RectImgDescr descr = analyzer.analyze(imgRect);
        Map<RectImgDescr, SynthetisedBitsCount> nodeResults = new HashMap<RectImgDescr, SynthetisedBitsCount>(); 
        // Perform
        SynthetisedBitsCount res = RectImgDescrBitsCountEstimater.recursiveEvalSynthetisedStatsBitsCount(descr, nodeResults);
        // Post-check
        Assert.assertNotNull(res);
        if (DEBUG) {
            LOG.info(imageFileName + " => synthetised estimated bits count:" + res.toStringFilter(200));
        }
    }
    
}
