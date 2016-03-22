package fr.an.screencast.compressor.imgtool.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettings;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettingsIOUtils;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRGlyphConnexeComponent;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRGlyphDescr;
import fr.an.screencast.compressor.imgtool.search.MarkerConnexComponentHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class SimpleScreenshotOCRHelper {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleScreenshotOCRHelper.class);
    
    private static final boolean DEBUG = false;
    
    private int paramScanHeight = 10;
    private int thresholdBgColor = 250;
    private int whiteSpaceDetectMinWidth = 3;
    
    private File ocrSettingsFile;
    private OCRSettings ocrSettings;
    
    private OCRInteractivePrompter ocrInteractivePrompter;
    
    // ------------------------------------------------------------------------

    public SimpleScreenshotOCRHelper() {
    }

    // ------------------------------------------------------------------------

    public void initSettings(File ocrSettingsFile) {
        this.ocrSettingsFile = ocrSettingsFile;
        this.ocrSettings = new OCRSettings();
        OCRSettingsIOUtils.writeOCRSettings(ocrSettingsFile, ocrSettings);
    }

    public void loadSettings(File ocrSettingsFile) {
        this.ocrSettingsFile = ocrSettingsFile;
        this.ocrSettings = OCRSettingsIOUtils.readOCRSettings(ocrSettingsFile);
    }
    
    public OCRInteractivePrompter getOcrInteractivePrompter() {
        return ocrInteractivePrompter;
    }

    public void setOcrInteractivePrompter(OCRInteractivePrompter ocrInteractivePrompter) {
        this.ocrInteractivePrompter = ocrInteractivePrompter;
    }

    public String imgToText(BufferedImage img) {
        StringBuilder sb = new StringBuilder();
        
        final int W = img.getWidth();
        final int H = img.getHeight();
        Dim dim = new Dim(W, H);
        final int imgLen = dim.getArea();
        int[] imgData = ImageRasterUtils.toInts(img);
        int[] ptMarkers = new int[imgLen];
        
        if (ocrInteractivePrompter != null) {
            ocrInteractivePrompter.startScreenshotOCR(ocrSettingsFile, ocrSettings, img);
        }
        
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
        OCRGlyphDescr prevGlyph = null;
        Pt prevGlyphOrigin = null;
        
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
            
            LOG.debug("found connexe comp: " + connexeComp);
            
            // find matching connexe component in OCR Settings->glyphs->scannedConnexeComponents
            ImageData connexeCompImageData = connexeComp.getImageData();
            Pt connexeCompUpperLeft = connexeComp.getPt();
            
            List<OCRGlyphConnexeComponent> glyphConnexeCompCandidates = ocrSettings.getMatchingGlyphConnexeComps(connexeCompImageData);
            
            if (DEBUG) {
                if (glyphConnexeCompCandidates.isEmpty()
                    // && !ocrSettings.getGlyphDescrs().isEmpty()
                    ) {
                    System.out.println();
                    System.out.println("img ROI:");
                    RGBUtils.dumpFixedRGBString(dim, imgData, connexeComp.getRect(), System.out);
                    System.out.println();
    
                    System.out.println("pt marker:");
                    for (int y = 0; y < H; y++) {
                        for(int x = 0; x < W; x++) {
                            int idx = y*W + x;
                            boolean mark = 1 == ptMarkers[idx];
                            //         "123,123,123 |"
                            System.out.print((mark)? "x" : " ");
                        }
                        System.out.println();
                    }
                    
                    System.out.println();
                    System.out.println("connex comp: " + connexeComp.getImageData().getDim());
                    RGBUtils.dumpFixedRGBString(connexeComp.getImageData(), System.out);
                    System.out.println();
                    
                    for(OCRGlyphDescr glyph : ocrSettings.getGlyphDescrs()) {
                        System.out.println("Glyph: " + glyph.getGlyphDisplayName());
                        for(OCRGlyphConnexeComponent connexComp : glyph.getConnexComponents()) {
                            System.out.println("connex component: " + connexComp.getOffset() + " file:" + connexComp.getImageDataFilename() + " crc32:" + connexComp.getCrc32());
                            ImageData imageData = connexComp.getImageData();
                            RGBUtils.dumpFixedRGBString(imageData, System.out);
                        }
                        System.out.println();
                    }
                }
            }
            
            OCRGlyphDescr foundGlyph = null;
            Rect foundEnclosingRect = null;
            Pt foundGlyphOrigin = null;
            if (glyphConnexeCompCandidates.size() == 1) {
                // no ambiguities.. check if all connexe components of glyph are present
                OCRGlyphConnexeComponent glyphComp0 = glyphConnexeCompCandidates.get(0);
                OCRGlyphDescr glyph = glyphComp0.getOwnerGlyph();
                if (glyph.getConnexComponents().size() == 1) {
                    // ok easy case: found simple(=1 connexe component) non-ambiguous glyph
                    foundGlyph = glyph;
                    foundEnclosingRect = connexeComp.getRect();
                    foundGlyphOrigin = connexeCompUpperLeft.newMinus(glyphComp0.getOffset());
                }
            }
            
            if (foundGlyph == null && ocrInteractivePrompter != null) {
                ocrInteractivePrompter.promptForGlyphConnexeComp(connexeComp);
            }
            
            if (foundGlyph != null) {
                if (prevGlyph != null) {
                    // check for white space separation ... (single space, several spaces, newlines ..)
                    Rect prevGlyphEnclosingRectOffset = prevGlyph.getEnclosingRectOffset();
                    if (prevGlyphOrigin.getY() + prevGlyphEnclosingRectOffset.getHeight() < foundGlyphOrigin.getY()) { // ???
                        sb.append("\n");
                    }
                    if (prevGlyphOrigin.getX() + prevGlyphEnclosingRectOffset.getWidth() + whiteSpaceDetectMinWidth < foundGlyphOrigin.getX()) { // ???
                        sb.append(" ");
                    }
                    
                }
                sb.append(foundGlyph.getGlyphText());
                prevGlyph = foundGlyph;
                prevGlyphOrigin = foundGlyphOrigin;
            }

        }

        if (ocrInteractivePrompter != null) {
            ocrInteractivePrompter.finishScreenshotOCR();
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
