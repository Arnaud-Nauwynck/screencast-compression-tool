package fr.an.screencast.compressor.imgtool.search;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.QuadDirection;
import fr.an.screencast.compressor.utils.Rect;

public class MarkerConnexComponentHelper {

    private final Dim dim;
    private final int[] imgData;
    
    private final int[] ptsMarkers;

    private final int[] tmpComponentImgData;

    // ------------------------------------------------------------------------
    
    public MarkerConnexComponentHelper(Dim dim, int[] imgData, int[] ptsMarkers) {
        this.dim = dim;
        this.imgData = imgData;
        this.ptsMarkers = ptsMarkers;
        this.tmpComponentImgData = new int[dim.getArea()];
        Arrays.fill(tmpComponentImgData, RGBUtils.TRANSPARENT_COLOR);
    }
    
    // ------------------------------------------------------------------------

    

    public PtImageData markAndExtractConnexeComponentAt(final Pt initPt) {
        final int W = dim.width, H = dim.height;
        Rect enclosingRect = Rect.newPtDim(initPt, 1, 1); 
        Pt pt = initPt;
        int ptIdx = pt.y * W + pt.x;
        if (ptsMarkers[ptIdx] == 1) {
            return null;
        }
        ptsMarkers[ptIdx] = 1;
        List<Pt> remainPts = new LinkedList<Pt>();
        remainPts.add(initPt);
        
        // transitive closure : traverse all neighboor pts having color != excludeBackgroundColor
        while(! remainPts.isEmpty()) {
            pt = remainPts.remove(0);
            ptIdx = pt.y * W + pt.x;
            tmpComponentImgData[ptIdx] = imgData[ptIdx];
            enclosingRect.setDilateToContain(pt);
            
            for(QuadDirection dir : QuadDirection.values()) {
                Pt neightboorPt = dir.newShiftPt(pt);
                if (neightboorPt.x < 0 || neightboorPt.x >= W || neightboorPt.y < 0 || neightboorPt.y >= H) {
                    continue;
                }
                int neightboorPtIdx = dir.shiftPtIdx(ptIdx, W);
                if (ptsMarkers[neightboorPtIdx] == 1) {
                    continue;
                }
                ptsMarkers[neightboorPtIdx] = 1;
                remainPts.add(neightboorPt);
            }
        }
        
        // find enclosing rect
        Pt upperLeftPt = enclosingRect.getFromPt();
        ImageData connexCompImgData = ImageRasterUtils.getCopyImgData(dim, tmpComponentImgData, enclosingRect);
        PtImageData res = new PtImageData(upperLeftPt, connexCompImgData);
        
        // reset tmpComponentImgData .. equivalent optim for Arrays.fill(tmpComponentImgData, RGBUtils.TRANSPARENT_COLOR);
        ImageRasterUtils.fillRect(dim, tmpComponentImgData, enclosingRect, RGBUtils.TRANSPARENT_COLOR);
        
        return res;
    }

    public int[] getPtMarkers() {
        return ptsMarkers;
    }
}
