package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.ui.jfx.imgtool.rectdescr.RectImgDescrTreeItemData.NodeRectImgDescrTreeItemData;
import javafx.scene.control.TreeView;


@SuppressWarnings("restriction")
public class RectImgDescrTreeView {

    private javafx.scene.control.TreeView<RectImgDescrTreeItemData> treeView;
    private RectImgDescrTreeItem treeRoot;
    
    private RectImgDescription model;
    
    // ------------------------------------------------------------------------

    public RectImgDescrTreeView() {
        createUI();
    }

    private void createUI() {
        this.treeRoot = new RectImgDescrTreeItem(new NodeRectImgDescrTreeItemData<RectImgDescription>("", null));
        this.treeView = new TreeView<RectImgDescrTreeItemData>(treeRoot);
        
    }

    public static javafx.scene.Parent createView(RectImgDescription model) {
        RectImgDescrTreeView view = new RectImgDescrTreeView();
        view.setModel(model);
        
        TreeView<RectImgDescrTreeItemData> component = view.getComponent();
        return component;
    }

    
    // ------------------------------------------------------------------------

    public TreeView<RectImgDescrTreeItemData> getComponent() {
        return treeView;
    }

    public RectImgDescription getModel() {
        return model;
    }
    
    public void setModel(RectImgDescription model) {
        this.model = model;
        treeRoot.getChildren().clear();
        treeRoot.setValue(new NodeRectImgDescrTreeItemData<RectImgDescription>("", model));
        
        RectImgDescrTreeItemExpander expander = new RectImgDescrTreeItemExpander();
        model.accept(expander, treeRoot);
    }
    
}
