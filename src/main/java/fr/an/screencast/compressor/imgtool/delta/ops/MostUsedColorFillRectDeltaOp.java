package fr.an.screencast.compressor.imgtool.delta.ops;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Rect;

public class MostUsedColorFillRectDeltaOp extends FillRectDeltaOp {

    /** */
    private static final long serialVersionUID = 1L;
    
    private int colorCount;
    
    public MostUsedColorFillRectDeltaOp(Rect rect, int fillColor, int colorCount) {
        super(rect, fillColor);
        this.colorCount = colorCount;
    }
    
    public int getColorCount() {
        return colorCount;
    }

    private static final NumberFormat FMT_DBL_1 = new DecimalFormat("##.#");

    public String toString() {
        int area = rect.getArea();
        double ratio = colorCount * 100.0 / (area != 0? area : 1);
        return "MostUsedColorFillRectDeltaOp[rect:" + rect + ", color:" + RGBUtils.toString(fillColor)
            + ", count:" + colorCount + "/" + area + ":" + FMT_DBL_1.format(ratio) + "% ]";
    }
    
}

