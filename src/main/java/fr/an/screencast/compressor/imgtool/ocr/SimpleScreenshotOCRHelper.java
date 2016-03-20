package fr.an.screencast.compressor.imgtool.ocr;

import java.awt.image.BufferedImage;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.search.MarkerConnexComponentHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;

public class SimpleScreenshotOCRHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleScreenshotOCRHelper.class);
    
    private int paramScanHeight = 10;
    private int thresholdBgColor = 250;
    
    // ------------------------------------------------------------------------

    public SimpleScreenshotOCRHelper() {
    }

    // ------------------------------------------------------------------------
    
    public void loadSettings(File dir) {
        
    }
    
    
    
    public String imgToText(BufferedImage img) {
        StringBuilder sb = new StringBuilder();
        
        final int W = img.getWidth();
        final int H = img.getHeight();
        Dim dim = new Dim(W, H);
        final int imgLen = dim.getArea();
        int[] imgData = ImageRasterUtils.toInts(img);
        int[] ptMarkers = new int[imgLen];
        
        // detect background color .. TOCHG? use hard-coded thresholdBgColor
        // mark background pts (exclude from connexe components detection)
        for(int idx = 0; idx < imgLen; idx++) {
            int color = imgData[idx];
            int r = RGBUtils.redOf(color);
            int g = RGBUtils.greenOf(color);
            int b = RGBUtils.blueOf(color);
            if (r > thresholdBgColor 
                    && g > thresholdBgColor
                    && b > thresholdBgColor) {
                ptMarkers[idx] = 1;
            }
        }
        
        MarkerConnexComponentHelper colorConnexCompHelper = new MarkerConnexComponentHelper(dim, imgData, ptMarkers);
        
        // scan horyzontal: left to right, then top to bottom  (several lines at a time, taking lefmost non background)
        int currY = 0;
        int currX = 0;
        Pt nextDetectPt = new Pt();
        int connexCompCount = 0;
        for(;;) {
            // scan horyzontal then vertical to detect next point not already marked
            if (! detectNextHoryzontalPt(nextDetectPt, currX, currY, ptMarkers, W, H)) {
                currX = 0;
                currY += paramScanHeight;
                if (currY >= H) {
                    break;
                } else {
                    continue;
                }
            } else {
                currX = nextDetectPt.x;
            }
            // ok detected pt... compute colors connexe component starting from this point
            PtImageData connexeComp = colorConnexCompHelper.markAndExtractConnexeComponentAt(nextDetectPt);
            
            LOG.info("found connexe comp: " + connexeComp);
        }
        
        return sb.toString();
    }

    private boolean detectNextHoryzontalPt(Pt resultNextDetectPt, int currX, int currY, int[] ptMarkers, int W, int H) {
        int foundMinX = W;
        int maxY = Math.min(H, currY + paramScanHeight);
        int currYIdx = currY*W + currX;
        for (int y = currY; y < maxY; y++,currYIdx+=W) {
            for(int x = currX, currIdx = currYIdx; x < foundMinX; x++,currIdx++) {
                if (ptMarkers[currIdx] == 0) {
                    foundMinX = Math.min(x, foundMinX);
                    resultNextDetectPt.x = x;
                    resultNextDetectPt.y = y;
                    break;
                }
            }
        }
        return foundMinX != W;
    }
    
}
