package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.HashMap;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.rectdescr.RightDownSameCountsImg;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.AnalysisProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ConnexSegmentLinesNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.NoiseAbovePartsRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.NoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.OverrideAttributesProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.PtNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * abstract RectImgDescrVisitor implementation for recursive traversal of RectImgDescr AST
 * using parent context (attributes + clip region from Above descr)
 * 
 * special treatments on  
 * caseRoot => init clip region
 * case Above / AboveNoise => update clip region with mask above parts while traversing underlying parts
 * case OverrideAttributesProxy => update attributes
 */
public abstract class AbstractInheritedCtxRectImgDescrTraversal extends AbstractRectImgDescrVisitor {
    
    protected Map<Object,Object> inheritedAttributes = new HashMap<Object,Object>();  
    protected RightDownSameCountsImg inheritedClipRegionSameCount;
    
    // ------------------------------------------------------------------------

    public AbstractInheritedCtxRectImgDescrTraversal(Dim dim) {
        this.inheritedClipRegionSameCount = new RightDownSameCountsImg(dim);
        // cf caseRoot() => inheritedClipRegionSameCount.setComputeFromUniformImg();
    }

    // ------------------------------------------------------------------------

    @Override
    public void caseRoot(RootRectImgDescr node) {
        inheritedClipRegionSameCount.setComputeFromUniformImg();
        recurse(node.getTarget());
    }
    
    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final RectImgDescr underlying = node.getUnderlying();
        final RectImgDescr[] aboves = node.getAboves();
        // first recuse on above regions
        recurse(aboves);
        // then mask clip region, and recurse i nunderlying region
        if (aboves != null) {
            for(RectImgDescr above : aboves) {
                Rect aboveRect = above.getRect();
                inheritedClipRegionSameCount.updateDiffCountsRect(aboveRect);
            }
        }
        // recurse in remaining (clipped) underlying
        recurse(underlying);
    }

    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        NoiseFragment[][] noiseFragmentsAboveParts = node.getNoiseFragmentsAboveParts();
        if (noiseFragmentsAboveParts != null) {
            for (int part = 0; part < noiseFragmentsAboveParts.length; part++) {
                NoiseFragment[] frags = noiseFragmentsAboveParts[part];
                if (frags != null) {
                    for(NoiseFragment frag : frags) {
                        frag.accept(this, node, part);
                    }
                }
            }
        }

        recurse(node.getUnderlying());
    }
    
    @Override
    public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
        final int x = node.getX(), y = node.getY();
        inheritedClipRegionSameCount.updateDiffCountsSegment(x, x+1, y);
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        final int fromX = node.getFromX(), toX = node.getToX(), y = node.getY();
        inheritedClipRegionSameCount.updateDiffCountsSegment(fromX, toX, y);
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        final int fromY = node.getFromY();
        final Segment[] lines = node.getLines();
        final int linesLen = lines != null? lines.length : 0;
        for (int i = 0, y = fromY; i != linesLen; i++,y++) {
            inheritedClipRegionSameCount.updateDiffCountsSegment(lines[i].from, lines[i].to, y);
        }
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescr target = node.getTarget();
        recurse(target);        
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        Map<Object, Object> attributeOverrides = node.getAttributeOverrides();
        Map<Object,Object> prev = inheritedAttributes;
        try {
            if (attributeOverrides != null && !attributeOverrides.isEmpty()) {
                inheritedAttributes = new HashMap<Object,Object>(inheritedAttributes);
                inheritedAttributes.putAll(attributeOverrides);
            }
            recurse(node.getUnderlying());
        } finally {
            inheritedAttributes = prev;
        }
    }
    
}
