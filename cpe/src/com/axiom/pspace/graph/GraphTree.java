package com.axiom.pspace.graph ;
import java.awt.* ;
import javax.swing.* ;
import java.util.Comparator ;
import com.axiom.lib.util.Pair ;
import java.util.* ;
import com.axiom.lib.util.* ;

/**
 *  Prototype for displaying graphs as trees.
 */
public class GraphTree extends JComponent {

    public GraphTree() {
        rendererPane = new CellRendererPane();
        add( rendererPane ) ;
    }

    public void setXSpacing( int spacing ) {
        this.xSpacing = spacing ;
    }

    public int getXSpacing() { return xSpacing ; }

    public void setCellRenderer( GraphTreeCellRenderer g ) {
        this.renderer = g ;
        if ( model != null && renderer != null )
            updateLayout();
    }

    public void setEdgeRenderer( GraphTreeEdgeRenderer er ) {
        this.edgeRenderer = er ;
        repaint() ;
    }

    public void setModel( GraphTreeModel model ) {
        this.model = model ;
        if ( model != null && renderer != null )
            updateLayout(); 
    }

    public void setMaxDepth( int maxDepth ) {
        this.maxDepth = maxDepth ;
        if ( model != null && renderer != null ) {
            updateLayout() ;
        }
    }

    public int getMaxDepth() { return maxDepth ; }

    public Dimension getPreferredSize() {
        return preferredSize ;
    }

    public void setMaxNodesPerLevel( int maxNodes ) {
        this.maxNodes = maxNodes ;
    }

    public int getMaxNodesPerLevel() {
        return maxNodes ;
    }

    public void setNodeComparator( Comparator c ) {
        this.treeNodeComparator = c ;
    }

    protected void updateLayout() {
        // A identity hashtable of nodes
        Hashtable set = new Hashtable() ;
        nodeLists = new ArrayList[ maxDepth ] ;

        for (int i=0;i<maxDepth;i++) {
            ArrayList nlist = nodeLists[i] = new ArrayList() ;
            if ( i == 0 ) {
                Object root = model.getRoot() ;
                set.put( new ObjectRef( root ), root ) ;
                nodeLists[i].add( root ) ;
            }
            else {
                // Get children for every element of list[i-1].
                for ( int j=0;j<nodeLists[i-1].size();j++) {
                    Object par = nodeLists[i-1].get(j) ;
                    int numChildren = model.getChildCount( par ) ;
                    for (int k=0;k<numChildren;k++) {
                        Object child = model.getChild( par, k ) ;
                        Object tmp = set.get( new ObjectRef( child ) ) ;
                        if ( tmp != null ) {
                            continue ;
                        }
                        set.put( new ObjectRef(child), child );
                        nodeLists[i].add( child ) ;
                    }
                }
            }
            // Sort each of the lists and remove those which exceed maxNodes
            if ( treeNodeComparator != null ) {
                Collections.sort( nodeLists[i], treeNodeComparator );
            }

            // Eliminate excess nodes
            if ( maxNodes != -1 && nodeLists[i].size() > maxNodes ) {
                ArrayList temp = new ArrayList() ;
                for (int j=0;j<maxNodes;j++) {
                    temp.add( nodeLists[i].get(j) ) ;
                }
                for (int j=maxNodes;j<nodeLists[i].size();j++) {
                    set.remove( new ObjectRef( nodeLists[i].get(j) ) );
                }
                nodeLists[i] = temp ;
            }

        }


        // Cache width and heigh for each row and compute the preferred size
        cw = new int[ nodeLists.length ] ;
        ch = new int[ nodeLists.length ] ;
        int maxNumVElements = 0 ;

        for (int i=0;i<nodeLists.length;i++) {
            cw[i] = 0 ; ch[i] = 0 ;
            for (int j=0;j<nodeLists[i].size();j++) {
                Component c = renderer.getRendererComponent( this, nodeLists[i].get(j) ) ;
                rendererPane.add( c ) ;
                Dimension d = c.getPreferredSize() ;
                cw[i] = (int) ( ( d.getWidth() > cw[i] ) ? d.getWidth() : cw[i] );
                ch[i] += d.getHeight() ;
            }
            maxNumVElements =
                ( nodeLists[i].size() > maxNumVElements ) ? nodeLists[i].size() : maxNumVElements ;
        }

        int maxWidth = cw.length * xSpacing + com.axiom.lib.util.ArrayMath.sum( cw ) ;
        int maxHeight = com.axiom.lib.util.ArrayMath.max( ch ) +
                          ( maxNumVElements + 1 ) * ySpacing ;

        preferredSize = new Dimension( maxWidth, maxHeight ) ;

    }

    public void paint( Graphics g ) {
        Rectangle r = getBounds() ;
        g.setColor( Color.white );
        g.fillRect( 0, 0, r.width, r.height );
        Hashtable set = new Hashtable() ;

        int mw = xSpacing ;
        
        //System.out.println( "\n\nPreferred Size : " + preferredSize ) ;
        //for (int i=0;i<cw.length;i++) {
        //    System.out.println( "Tree row " + i + " is (" + cw[i] + "," + ch[i] + ")" ) ;
        //}

        // Render nodes
        double maxHeight = preferredSize.getHeight(), maxWidth = preferredSize.getWidth() ;
        for (int i=0;i<nodeLists.length;i++) {
            float lh = 0 ;
            Integer ii = new Integer( i ) ;
            for (int j=0;j<nodeLists[i].size();j++) {
                Object o = nodeLists[i].get(j) ;
                Component rc = renderer.getRendererComponent( this, o ) ;
                Dimension d = rc.getPreferredSize() ;
                float frac = ( float ) ( d.getHeight() ) / ( float ) ch[i] ;
                int cy = ( int ) ( lh * maxHeight + frac * maxHeight / 2.0f ) ;
                Rectangle bounds = new Rectangle( mw, cy, ( int ) d.getWidth(), ( int ) d.getHeight() ) ;
                // System.out.println( "Adding bounds for (" + i + "," + j + ") at " + bounds ) ;
                set.put( new ObjectRef(o) , new Pair( o, new Pair( bounds, ii ) ) );
                // Associate bounds with object o
                rendererPane.paintComponent( g, rc, this,
                                             mw, cy,
                        ( int ) d.getWidth(), ( int ) d.getHeight(), true ) ;
                lh += frac ;
            }
            mw += cw[i] + xSpacing ;
        }

        //System.out.println( "\n\nPainting tree..." ) ;
        // System.out.println( "Bounds table after first pass: " + set ) ;

        // Render edges
        for (int i=0;i<nodeLists.length-1;i++) {
            //System.out.println( "\nPainting level " + i ) ;
            for (int j=0;j<nodeLists[i].size();j++) {
                Object o = nodeLists[i].get(j) ;

                // DEBUG
                //System.out.println( "Rendering edges for parent node " + o ) ;

                Pair p = ( Pair ) set.get( new ObjectRef(o) );
                if ( p == null )
                    continue ;
                Rectangle rp = ( Rectangle ) ( ( Rectangle ) ( ( Pair ) p.second()).first() ).clone() ;

                int numChildren = model.getChildCount( o ) ;
                if ( numChildren > maxEdgesRendered ) {
                    numChildren = maxEdgesRendered ;
                }
                //System.out.println( "There are a total of " + numChildren + " chidren." ) ;
                for (int k=0;k<numChildren;k++) {
                    Object child = model.getChild( o, k ) ;
                    Object edge = model.getEdge( o, k ) ;
                    p = ( Pair ) set.get( new ObjectRef( child ) ) ;
                    if ( p == null ) continue ;
                    int depth =  ( ( Integer ) ( ( Pair ) p.second()).second() ).intValue() ; 
                    if ( depth != i + 1 ) {
                        continue ;
                    }

                    Rectangle rc = ( Rectangle ) ( ( Rectangle ) ( ( Pair ) p.second()).first() ).clone() ;

                    //System.out.println( "Drawing edge between components at " + rp + " and " + rc );
                    Rectangle ru = new Rectangle() ;
                    Rectangle rpt = new Rectangle( rp ) ;
                    Rectangle.union( rp, rc, ru );
                    //System.out.println( "Rendering edge in bounds " + ru + " from " + rp + " to " + rc  ) ;

                    rpt.translate( -ru.x, -ru.y );
                    rc.translate( -ru.x, - ru.y );

                    Component ec = edgeRenderer.getEdgeRenderComponent( rpt, rc, o, child, edge ) ;
                    rendererPane.paintComponent( g, ec, this, ru );
                }
            }
        }
    }

    public void setMaxEdgesRendered(int newMaxEdgesRendered) {
        maxEdgesRendered = newMaxEdgesRendered;
    }

    public int getMaxEdgesRendered() {
        return maxEdgesRendered;
    }


    int maxDepth = 2 ;

    int xSpacing = 100, ySpacing = 2 ;

    Dimension preferredSize = new Dimension( 0, 0 ) ;
    ArrayList[] nodeLists ;
    int[] cw, ch ;

    GraphTreeModel model ;
    Comparator treeNodeComparator ;

    CellRendererPane rendererPane ;

    GraphTreeCellRenderer renderer ;

    GraphTreeEdgeRenderer edgeRenderer ;
    private int maxEdgesRendered = 20 ;
    private int maxNodes = 25 ;

}
