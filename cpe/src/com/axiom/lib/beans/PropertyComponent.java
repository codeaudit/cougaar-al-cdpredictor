package com.axiom.lib.beans ;

import java.awt.*;
import java.awt.event.*;
import java.beans.* ;
import javax.swing.* ;

public class PropertyComponent extends JComponent {

    public PropertyComponent( PropertyEditor pe ) {
	    // this.frame = frame;
	    editor = pe;
	    
	    MouseListener l = new MouseAdapter() {
	        
	        public void mouseClicked( MouseEvent e ) {
	            doMouseClicked( e ) ;   
	        }
	    } ;
	    addMouseListener( l ) ;
    }
        
    public void paint(Graphics g) {
        
        Dimension d = getSize() ;
	    Rectangle box = new Rectangle(2, 2, d.width - 4, d.height - 4);
	    editor.paintValue(g, box);
    }

    public void doMouseClicked( MouseEvent e ) {
	    if (! ignoreClick) {
	        try {
		    ignoreClick = true;
            int x = 0, y = 0 ;
            //int x = frame.getLocation().x - 30;
            //int y = frame.getLocation().y + 50;
            if ( e.getClickCount() == 1 && e.getSource() instanceof Component ) {
                Component c = ( Component )  e.getSource() ;
                Point p =  c.getLocationOnScreen() ;
                x = p.x ; y = p.y ;
            }

            new PropertyDialog( JOptionPane.getFrameForComponent(this), editor, x, y);
            } finally {
            ignoreClick = false;
		    }
		}
    }

    public static void main( String[] vars ) {
        JFrame frame = new JFrame( "Mice" ) ;
        frame.setSize( 640, 480 );
        frame.setVisible( true ) ;

        frame = new JFrame( "Moron" ) ;
        frame.setSize( 640, 480 );
        frame.setVisible( true ) ;

        JDialog moose = new JDialog( frame, "Moose", true ) ;
        moose.setSize( 300, 400 ) ;
        moose.getContentPane().setLayout( new BorderLayout() ) ;
        moose.getContentPane().add( new JButton( "Meese" ) , BorderLayout.SOUTH ) ;
        moose.setVisible( true ) ;
    }

    private Frame frame;
    private PropertyEditor editor;

    private boolean ignoreClick = false;
}