package com.axiom.lib.awt ;

import javax.swing.* ;
import java.awt.* ;

/**
 *  Implements a message component using JList and MessageListModel.
 *
 */
public class MessageComponent extends JList {
    
    public MessageComponent() {
        setModel( model ) ;
    }
    
    public void clear() {
        model.clear() ;
    }

    public void addMessage( String message ) {
        addMessage( message, null ) ;
    }
    
    public void addMessage( String message, Object tag ) {
        model.addMessage( message, tag ) ;
    }
    
    public void addMessages( String message, Object tag ) {
        model.addMessages( message, tag ) ;
    }
    
    MessageListModel model = new MessageListModel() ;
}
