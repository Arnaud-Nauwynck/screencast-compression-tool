package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.ui.swing.imgtool.rectdescr.RectImgDescrTreeNodeData.NodeRectImgDescrTreeNodeData;


public class RectImgDescrJTree {

    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrJTree.class);
    
    private JScrollPane scrollPane;
    private JTree/*<RectImgDescrTreeNodeData>*/ treeView;
    private RectImgDescrTreeNode treeRoot;
    
    private RectImgDescr model;
    
    public static final String PROP_selectedRectDescrPath = "selectedRectDescrPath";
    private RectImgDescr[] selectedRectDescrPath;
    
    private SwingPropertyChangeSupport propChangeSupport = new SwingPropertyChangeSupport(this);
    
    // ------------------------------------------------------------------------

    public RectImgDescrJTree() {
        createUI();
    }

    private void createUI() {
        this.treeRoot = new RectImgDescrTreeNode(new NodeRectImgDescrTreeNodeData<RectImgDescr>("", null));
        this.treeView = new JTree(treeRoot);
        this.scrollPane = new JScrollPane(treeView);
        scrollPane.setPreferredSize(new Dimension(200, 800)); //??
        
        TreeSelectionModel treeSelectionModel = treeView.getSelectionModel();
        treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        treeView.setExpandsSelectedPaths(true);
        
        treeSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                Object[] nodes = path.getPath();
                RectImgDescr[] newPath = new RectImgDescr[nodes.length];
                for(int i = 0; i < nodes.length; i++) {
                    Object node = nodes[i];
                    if (node instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) node;
                        NodeRectImgDescrTreeNodeData<?> nodeData = (NodeRectImgDescrTreeNodeData<?>) tnode.getUserObject();
                        RectImgDescr rectDescr = nodeData.getNode();
                        newPath[i] = rectDescr; 
                    }
                }
                setSelectedPath(newPath, false); 
            }
        });
    }

    public static JComponent createView(RectImgDescr model) {
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

    public RectImgDescr getModel() {
        return model;
    }
    
    public void setModel(RectImgDescr model) {
        this.model = model;
        treeRoot.removeAllChildren();
        treeRoot.setValue(new NodeRectImgDescrTreeNodeData<RectImgDescr>("", model));
        
        RectImgDescrTreeNodeExpander expander = new RectImgDescrTreeNodeExpander();
        model.accept(expander, treeRoot);
        
        treeView.expandRow(0);
    }

    public JTree getTree() {
        return treeView;
    }
    
    public RectImgDescr[] getSelectedRectDescrPath() {
        return selectedRectDescrPath;
    }

    public void setSelectedPath(List<RectImgDescr> path) {
        setSelectedPath(path.toArray(new RectImgDescr[path.size()]), true);
    }

    private void setSelectedPath(RectImgDescr[] p, boolean updateUI) {
        RectImgDescr[] prev = selectedRectDescrPath; 
        this.selectedRectDescrPath = p;
        
        if (updateUI) {
            List<MutableTreeNode> treeNodes = modelPathToTreeNodePath(selectedRectDescrPath);
            // set selection in TreeView
            if (treeNodes.size() > 0) {
                Object[] treeNodesArray = treeNodes.toArray(new Object[treeNodes.size()]);
                TreePath treePath  = new TreePath(treeNodesArray);
                
                // treeView.expandPath(treePath);// does not work!! isLeaf(lastComponent) ...
                // => check to expand parentTreePath
                TreePath parentTreePath = treePath; 
                MutableTreeNode lastNode = treeNodes.get(treeNodes.size()-1);
                if (lastNode.isLeaf()) {
                    parentTreePath = treePath.getParentPath();
                }
                LOG.debug("treeView.expandPath " + parentTreePath);
                treeView.expandPath(parentTreePath);
                
                treeView.getSelectionModel().setSelectionPath(treePath);
            } else {
                treeView.getSelectionModel().clearSelection();
            }
        }
        propChangeSupport.firePropertyChange(PROP_selectedRectDescrPath, prev, selectedRectDescrPath);
    }

    public List<MutableTreeNode> modelPathToTreeNodePath(RectImgDescr[] modelPath) {
        // lookup corresponding TreeNode
        List<MutableTreeNode> treeNodes = new ArrayList<MutableTreeNode>();
        treeNodes.add(this.treeRoot);
        MutableTreeNode currNode = treeRoot;
        for(int i = 0; i < modelPath.length; i++) {
            MutableTreeNode childNode = findChildFor(currNode, modelPath[i]);
            if (childNode == null) {
                // should not occur?
                break;
            }
            treeNodes.add(childNode);
            currNode = childNode;
        }
        return treeNodes;
    }

    
    protected static MutableTreeNode findChildFor(MutableTreeNode parent, RectImgDescr search) {
        int len = parent.getChildCount();
        for(int i = 0; i < len; i++) {
            TreeNode childObj = parent.getChildAt(i);
            if (childObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) childObj;
                NodeRectImgDescrTreeNodeData<?> nodeData = (NodeRectImgDescrTreeNodeData<?>) child.getUserObject();
                if (nodeData != null) {
                    if (nodeData.getNode() == search) {
                        return child;
                    }
                }
            }
        }
        return null;
    }
    
    
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChangeSupport.removePropertyChangeListener(propertyName, listener);
    }


}
