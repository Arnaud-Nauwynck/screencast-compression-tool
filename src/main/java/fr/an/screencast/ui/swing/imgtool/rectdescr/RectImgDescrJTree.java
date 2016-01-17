package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.ui.swing.imgtool.rectdescr.RectImgDescrTreeNodeData.NodeRectImgDescrTreeNodeData;


public class RectImgDescrJTree {

    private JScrollPane scrollPane;
    private JTree/*<RectImgDescrTreeNodeData>*/ treeView;
    private RectImgDescrTreeNode treeRoot;
    
    private RectImgDescription model;
    
    // ------------------------------------------------------------------------

    public RectImgDescrJTree() {
        createUI();
    }

    private void createUI() {
        this.treeRoot = new RectImgDescrTreeNode(new NodeRectImgDescrTreeNodeData<RectImgDescription>("", null));
        this.treeView = new JTree(treeRoot);
        this.scrollPane = new JScrollPane(treeView);
        
    }

    public static JComponent createView(RectImgDescription model) {
        RectImgDescrJTree view = new RectImgDescrJTree();
        view.setModel(model);
        
        JComponent component = view.getComponent();
        component.setPreferredSize(new Dimension(200, 800)); //??
        return component;
    }

    
    // ------------------------------------------------------------------------

    public JComponent getComponent() {
        return scrollPane;
    }

    public RectImgDescription getModel() {
        return model;
    }
    
    public void setModel(RectImgDescription model) {
        this.model = model;
        treeRoot.removeAllChildren();
        treeRoot.setValue(new NodeRectImgDescrTreeNodeData<RectImgDescription>("", model));
        
        RectImgDescrTreeNodeExpander expander = new RectImgDescrTreeNodeExpander();
        model.accept(expander, treeRoot);
    }
    
}
