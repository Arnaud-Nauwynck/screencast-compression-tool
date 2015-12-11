package fr.an.screencast.compressor.imgtool.delta.ops;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.delta.DeltaContext;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Pt;

public class DrawLineDeltaOp extends DeltaOperation {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final Pt[] pts;
    private final int color;
    private final float strokeWidth;
    
    public DrawLineDeltaOp(Pt[] pts, int color, float strokeWidth) {
        this.pts = pts;
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public void apply(DeltaContext context, BufferedImage dest) {
        Graphics2D g2d = dest.createGraphics();
        g2d.setColor(new Color(color));
        g2d.setStroke(new BasicStroke(strokeWidth));
        for (int i = 0; i < pts.length-1; i++) {
            g2d.drawLine(pts[i].x, pts[i].y, pts[i+1].x, pts[i+1].y);
        }
    }
    
    public String toString() {
        return "DrawLine[pts:" + Pt.polyLineToString(pts) + ", color:" + RGBUtils.toString(color) + "]";
    }
    
}

