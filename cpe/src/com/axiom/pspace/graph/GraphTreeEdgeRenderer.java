package com.axiom.pspace.graph ;
import java.awt.* ;

public interface GraphTreeEdgeRenderer {

    /**
     *  Render an edge connecting two nodes in the tree.
     *  Rp and Rc are the bounding rectangles for the parent and child cell
     *  respectively in the coordinate system of the GraphTreeEdgeRenderer
     */
    public Component getEdgeRenderComponent( Rectangle rp, Rectangle rc,
                                         Object parent, Object child, Object edge ) ;
    
}
