package com.axiom.lib.awt ;

import javax.swing.* ;
import java.awt.* ;
import java.util.ArrayList ;
import java.util.List ;

public class MessageListModel extends AbstractListModel {
    static class MessagePair {

        MessagePair( String m, Object t ) {
           this.message = m ;
           this.tag = t ;
        }
        String message ;
        Object tag ;
    }    
    
    MessageListModel() {
        this.messageList = new ArrayList() ;
    }

    public Object getElementAt(int i) {
        return ( ( MessagePair ) messageList.get( i )).message ;
    }

    public int getSize() {
        return messageList.size() ;
    }

    public void setMaxMessages( int limit ) {
        this.limit = limit ;
    }

    public void clear() {
        int length = messageList.size() ;

        messageList.clear() ;
        fireIntervalRemoved( this, 0, length - 1 ) ;
    }

    public void addMessage( String message ) {
        addMessage( message, null ) ;
    }

    /**
     *  Add a single line message associated with tag.
     */
    public void addMessage( String message, Object tag ) {
        if ( message == null ) {
            return ;
        }
                        
        // Count the number of tabs if we want to convert tabs to spaces
        if ( convertTabsToSpaces ) {
            int tabcount = 0; 
                
            for (int i=0;i<message.length();i++) {
                if ( message.charAt( i ) == '\t' ) {
                    tabcount++;
                }
            }
                
            if ( tabcount > 0 ) {           
                if ( message.length() > buffer.length ) {
                    buffer = new char[ message.length() + tabcount * 4 ];
                }
            }
                
            int j = 0;
                
            for (int i=0;i<message.length();i++) {
                char c = message.charAt(i);
                    
                if ( c != '\t') {
                    buffer[j++] = c ;
                }
                else {
                    int mod = tabwidth - ( j % tabwidth );
                    for (int k=0;k<mod;k++) {
                        buffer[j++] = ' ' ;
                    }
                }
            }
            message = new String( buffer, 0, j ) ; // Create a new message with spaces added
        }
            
        int length = messageList.size() ;

        MessagePair pair = new MessagePair( message, tag ) ;
        messageList.add( pair ) ;
        fireContentsChanged( this, 0, length ) ;
        // fireIntervalAdded( this, length, length ) ;

        if ( messageList.size() > limit ) {
            messageList.remove( 0 ) ;
            fireIntervalRemoved(this, 0, 0 ) ;
        }
    }

    /**
     *  Add a single or multiline message associated with tag.  If the
     *  message string has embedded new-line characters, it will be
     *  broken up into separate lines, each associated with the same tag.
     */
    public void addMessages( String message, Object tag ) {
        int start = 0 ;
        int end = start ;
        ArrayList messages = new ArrayList() ;
        boolean c = true ;

        while ( c ) {
            boolean found = false ;

            for (int i=start+1;i<message.length();i++) {
                if ( message.charAt(i) == '\n' ) {
                    end = i ;
                    found = true ;
                    break ;
                }
            }

            if ( !found ) {
                end = message.length() ;
                c = false ;
            }

            String s = message.substring( start, end ) ;
            // Update state to not include newline
            start = end + 1;
             messages.add( s ) ;
        }

        // Just add one at a time, rather than blocking the whole thing
        for (int i=0;i<messages.size();i++) {
            addMessage( ( String) messages.get(i), tag ) ;
        }
    }
        
    public void addMessages( String message ) {
        addMessages( message, null ) ;
    }

    protected ArrayList messageList = new ArrayList();

    protected int limit = Integer.MAX_VALUE;
        
    protected int tabwidth = 4 ;

    protected boolean convertTabsToSpaces = true ;

    protected char[] buffer = new char[1024];
}
