
/*
 * GraphFrame.java
 *
 * Created on September 24, 2001, 11:04 AM
 */

package org.hydra.server.ui;
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
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                Window w = wev.getWindow();
                w.setVisible(false);
                w.dispose();
            }
        });
                
        gp = new GrappaPanel(graph);
        gp.addGrappaListener(new GrappaAdapter());
        gp.setScaleToFit(false);
        
        JScrollPane jsp = new JScrollPane(gp);
        // jsp.getViewport().setDoubleBuffered(false);
        getContentPane().add( jsp, BorderLayout.CENTER ) ;
    }
    
    GrappaPanel gp ;
    Graph graph ;
}
