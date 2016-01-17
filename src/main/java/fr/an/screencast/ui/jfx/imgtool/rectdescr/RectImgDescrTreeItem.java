package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import javafx.scene.control.TreeItem;

@SuppressWarnings("restriction")
public class RectImgDescrTreeItem extends TreeItem<RectImgDescrTreeItemData> {

    // ------------------------------------------------------------------------
    
    public RectImgDescrTreeItem(RectImgDescrTreeItemData data) {
        super(data);
    }

    // ------------------------------------------------------------------------

    
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        RectImgDescrTreeItemData value = getValue();
        return value != null? value.getDisplayName() : "null";
    }
    
}
