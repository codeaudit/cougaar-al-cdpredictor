package com.axiom.lib.beans ;
import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;

public class PropertyTextEditor extends JTextField {
    
    public PropertyTextEditor( PropertyEditor pe ) {
        super( pe.getAsText() ) ;
        editor = pe ;
        addKeyListener ( 
            new KeyAdapter() {
               public void keyPressed( KeyEvent e ) {
                  doKeyTyped( e ) ;
               }
            } ) ;
    }

    /**
    public void repaint() {
        if ( editor != null ) 
	        setText(editor.getAsText());
	    //else
	    //    super.repaint() ;
    }
    */
    
    /**
     *  Update the property only if RETURN is pressed.
     */
    void doKeyTyped( KeyEvent e ) {
        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            try {
	            editor.setAsText(getText());
	        } catch (IllegalArgumentException ex) {
	            // Quietly ignore.
	        }
	        javax.swing.FocusManager.getCurrentManager().focusNextComponent( this ) ;
        }
        else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            javax.swing.FocusManager.getCurrentManager().focusNextComponent( this ) ;
            setText( editor.getAsText() ) ;
        }
    }
    
    PropertyEditor editor ;
}