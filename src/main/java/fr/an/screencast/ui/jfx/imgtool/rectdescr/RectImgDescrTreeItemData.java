package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RootRectImgDescr;

public abstract class RectImgDescrTreeItemData {

    private String displayName;
        
    // ------------------------------------------------------------------------
    
    protected RectImgDescrTreeItemData(String displayName) {
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
    public static class NodeRectImgDescrTreeItemData<T extends RectImgDescr> extends RectImgDescrTreeItemData {

        private T node;
            
        // ------------------------------------------------------------------------
        
        public NodeRectImgDescrTreeItemData(String displayName, T node) {
            super(displayName);
            this.node = node;
        }

        // ------------------------------------------------------------------------

        public RectImgDescr getNode() {
            return node;
        }
    }

    public static class RootRectImgDescrTreeItemData extends NodeRectImgDescrTreeItemData<RootRectImgDescr> {

        public RootRectImgDescrTreeItemData(String displayName, RootRectImgDescr node) {
            super(displayName, node);
        }
        
    }

    
}
