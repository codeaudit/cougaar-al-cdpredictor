package com.axiom.lib.beans ;
import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;

/**
 *  Made specifically to handle String properties that can be <code>null</code>
 *  It displays string inside quotes except when editing.
 *
 *  <p> Use this property editor if the property type is expicitly a <code>String</code>.
 *  Currently, the textfield looks disabled due to limitations with JTextField.
 */

public class PropertyStringEditor extends JTextField {

    public PropertyStringEditor( PropertyEditor pe ) {
        String s = pe.getAsText() ;
        
        if ( pe.getValue() == null ) {
            isNull = true ;
            setText( "null" ) ;
        }
        else
            setText( "\"" + s + "\"" ) ;
        
        editor = pe ;
        addKeyListener (
            new KeyAdapter() {
               public void keyPressed( KeyEvent e ) {
                  doKeyTyped( e ) ;
               }
            } ) ;
            
        addMouseListener( 
            new MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                   doMouseClicked( e ) ;
                }
            } );
   
        addFocusListener(
            new FocusAdapter() {
                public void focusGained( FocusEvent e ) {
                    
                }
                
                public void focusLost( FocusEvent e ) {
                    doFocusLost( e ) ;
                }
            } ) ;

        setEditable( false ) ;
    }
    
    public void setNoEnable( boolean value ) {
        noEnable = value ;
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
	        isNull = false ;
	        javax.swing.FocusManager.getCurrentManager().focusNextComponent( this ) ;
            setText( "\"" + editor.getAsText() + "\"" ) ;
	        setEditable( false ) ;
        }
        else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            javax.swing.FocusManager.getCurrentManager().focusNextComponent( this ) ;
            if ( isNull )
                setText( "null" ) ;
            else
                setText( "\"" + editor.getAsText() + "\"" ) ;
            setEditable( false ) ;
        }
    }
    
    void doMouseClicked( MouseEvent e ) {
        if ( !noEnable && isEditable() == false ) {
            if ( isNull ) {
               setText("") ;   
            }
            else
                setText( editor.getAsText() ) ;
            setEditable( true ) ;
            requestFocus() ;
        }
    }
    
    void doFocusLost( FocusEvent e ) {
        setEditable( false ) ;
        setText( "\"" + editor.getAsText() + "\"" ) ;
    }
    
    PropertyEditor editor ;
    
    boolean isNull = false ;
    
    boolean noEnable = false ;
}