package com.axiom.lib.awt ;
import java.awt.* ;
import javax.swing.* ;
import javax.swing.tree.* ;
import javax.swing.event.* ;
import java.util.* ;

public class CellTree extends JComponent {

    public void CellTree() {
    }

    public void setTreeModel( TreeModel model ) {
        if ( treeModel != null ) {
            treeModel.removeTreeModelListener( treeModelRedirector ) ;
        }

        this.treeModel = model ;
        model.addTreeModelListener( treeModelRedirector ) ;
    }

    public TreeModel getTreeModel() {
        return treeModel ;
    }

    public void setTreeCellFactory( TreeCellFactory factory ) {
        this.cellFactory = factory ;
    }

    public TreeCellFactory getTreeCellFactory() {
        return cellFactory ;
    }

    /**
     *  Rebuild all tree components for objects below the root.
     */
    protected void buildTreeComponents() {

        Object root = treeModel.getRoot() ;

        //TreeCell tc = cellFactory.makeComponentForObject( this, root ) ;

        // populateComponents() ;
    }

    protected void addCellForObject( Object o, Component c ) {
        table.put( o, c ) ;
    }

    protected Component getCellForObject( Object o ) {
        return ( Component ) table.get( o ) ;
    }

    // Map objects via hashcode to component
    protected Hashtable table = new Hashtable() ;

    protected TreeCellFactory cellFactory ;

    protected TreeModel treeModel ;

    protected TreeModelRedirector treeModelRedirector = new TreeModelRedirector() ;

    /**
     *  Listens to tree model events and makes changes to the current visible
     *  tree structure accordingly.
     */
    class TreeModelRedirector implements TreeModelListener {
        public void treeNodesChanged( TreeModelEvent e ) {
            invalidate() ;
            repaint() ;
        }

        public void treeNodesInserted( TreeModelEvent e ) {
            Object[] o = e.getPath() ;
            int[] indices = e.getChildIndices() ;
            Object c = getCellForObject( o[0] ) ;
            if ( c instanceof TreeCell ) {
                TreeCell tc = ( TreeCell ) c ;
                for (int i=0;i<indices.length;i++ ) {
                   // tc.insertCell() ;
                   
                }
            }

        }

        public void treeNodesRemoved( TreeModelEvent e ) {
        }

        public void treeStructureChanged( TreeModelEvent e ) {
        }
    }

}
