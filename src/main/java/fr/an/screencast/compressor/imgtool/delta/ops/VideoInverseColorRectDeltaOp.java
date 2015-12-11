package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Rect;

public class VideoInverseColorRectDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Rect rect;
    private final Map<Integer,Integer> colorMap;
    
    public VideoInverseColorRectDeltaOp(Rect rect, Map<Integer, Integer> colorMap) {
        this.rect = new Rect(rect);
        this.colorMap = new HashMap<Integer,Integer>(colorMap);
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        final int width = dest.getWidth();
        final int[] imgData = ImageRasterUtils.toInts(dest);
        int idx = rect.fromY * width + rect.fromX;
        final int incIdxY = width - rect.toX + rect.fromX;
        for(int y = rect.fromY; y < rect.toY; y++,idx+=incIdxY) {
            for(int x = rect.fromX; x < rect.toX; x++,idx++) {
                int prevColor = imgData[idx];
                Integer replacedColor = colorMap.get(prevColor);
                if (replacedColor != null) {
                    imgData[idx] = replacedColor;
                }
            }
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (! colorMap.isEmpty()) {
            for(Map.Entry<Integer,Integer> e : colorMap.entrySet()) {
                sb.append(RGBUtils.toString(e.getKey()) + "->" + RGBUtils.toString(e.getValue()) + ", ");
            }
            sb.delete(sb.length()-2, sb.length());
        }
        return "VideoInv[rect:" + rect + ", colorMap:" + sb + "]";
    }
    
}

