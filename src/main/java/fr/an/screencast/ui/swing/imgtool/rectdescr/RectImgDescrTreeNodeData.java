package fr.an.screencast.ui.swing.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RootRectImgDescr;

public abstract class RectImgDescrTreeNodeData {

    private String displayName;
        
    // ------------------------------------------------------------------------
    
    protected RectImgDescrTreeNodeData(String displayName) {
        this.displayName = displayName;
    }

    // ------------------------------------------------------------------------

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     *
     */
    public static class NodeRectImgDescrTreeNodeData<T extends RectImgDescription> extends RectImgDescrTreeNodeData {

        private T node;
            
        // ------------------------------------------------------------------------
        
        public NodeRectImgDescrTreeNodeData(String displayName, T node) {
            super(displayName);
            this.node = node;
        }

        // ------------------------------------------------------------------------

        public RectImgDescription getNode() {
            return node;
        }
    }

    public static class RootRectImgDescrTreeNodeData extends NodeRectImgDescrTreeNodeData<RootRectImgDescr> {

        public RootRectImgDescrTreeNodeData(String displayName, RootRectImgDescr node) {
            super(displayName, node);
        }
        
    }

    
}
