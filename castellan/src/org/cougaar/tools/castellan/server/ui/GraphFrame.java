
/*
 * GraphFrame.java
 *
 * Created on September 24, 2001, 11:04 AM
 */

package org.cougaar.tools.castellan.server.ui;
import att.grappa.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;

/**
 *
 * @author  wpeng
 * @version
 */
public class GraphFrame extends JFrame {
    
    /** Creates new GraphFrame */
    public GraphFrame( String name, Graph graph ) {
        super(name);
        this.graph = graph;
        
        setSize(600,400);
        setLocation(100,100);
        getContentPane().setLayout( new BorderLayout() ) ;
        
        splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                Window w = wev.getWindow();
                w.setVisible(false);
                w.dispose();
            }
        });
                
        gp = new GrappaPanel(graph);
        gp.addGrappaListener(new GrappaAdapter() {
            public void grappaClicked( Subgraph subg, Element elem, GrappaPoint pt, int modifiers, int clickCount, GrappaPanel panel ) {
                if ( elem != null ) {
                    doClicked( elem.getName() ) ;
                }
            }
        });
        gp.setScaleToFit(false);
        
        JScrollPane jsp = new JScrollPane(gp);
        splitPane.setLeftComponent( jsp ) ;
        splitPane.setDividerLocation( 500 ) ;
        splitPane.setOneTouchExpandable( true ) ;
        splitPane.setRightComponent( emptyPanel ) ;
        splitPane.setResizeWeight( 1.0 );
        // jsp.getViewport().setDoubleBuffered(false);
        getContentPane().add( splitPane, BorderLayout.CENTER ) ;
    }
    
    protected void doClicked( String s ) {
        
    }
 
    JSplitPane splitPane ;
    JPanel emptyPanel = new JPanel() ;
    GrappaPanel gp ;
    Graph graph ;
}
