package fr.an.screencast.ui.swing.imgtool.rectdescr;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.ui.jfx.imgtool.rectdescr.RectImgDescrTreeItemData.NodeRectImgDescrTreeItemData;

public class RectImgDescrTreeNode extends DefaultMutableTreeNode/*<RectImgDescrTreeItemData>*/ {

    /** */
    private static final long serialVersionUID = 1L;
    
    // ------------------------------------------------------------------------
    
    public RectImgDescrTreeNode(RectImgDescrTreeNodeData data) {
        super(data);
    }

    // ------------------------------------------------------------------------

    public RectImgDescrTreeNodeData getValue() {
        return (RectImgDescrTreeNodeData) getUserObject();
    }
    
    public void setValue(RectImgDescrTreeNodeData value) {
        setUserObject(value);
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        RectImgDescrTreeNodeData value = getValue();
        return value != null? value.getDisplayName() : "null";
    }

    
}
