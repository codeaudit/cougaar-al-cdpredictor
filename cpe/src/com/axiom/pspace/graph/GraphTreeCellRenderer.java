package com.axiom.pspace.graph ;
import java.awt.* ;
import javax.swing.* ;

public interface GraphTreeCellRenderer {

    /** Render an object into a cell.
     */
    public Component getRendererComponent( GraphTree tree, Object o ) ;
}
