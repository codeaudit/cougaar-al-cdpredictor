package com.axiom.lib.awt ;

import javax.swing.* ;
import java.awt.* ;
import java.util.ArrayList ;
import java.util.List ;

/**
 *  A Window for displaying single/multiline messages using a list box.
 *  Should be augmented with a message text window, allowing messages to
 *  be associated with one or more lines.
 */
public class MessageWindow extends JFrame {

    public static class MessagePair {

        MessagePair( String m, Object t ) {
           this.message = m ;
           this.tag = t ;
        }
        public String getString() {
            return message ;
        }

        public Object getTag() {
            return tag ;
        }

        protected String message ;
        protected Object tag ;
    }

    public static class MessageListModel extends AbstractListModel {
        MessageListModel( ArrayList messageList ) {
            this.messageList = messageList ;
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

            if ( messageList.size() > limit ) {
                messageList.remove( 0 ) ;
                fireIntervalRemoved(this, 0, 0 ) ;
            }
        }

       /**
        *  Add a single or multiline message associated with tag.  If the
        *  message string has embedded new line characters, it will be
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
                    int ch = message.charAt(i) ;
                    if ( ch == '\n' || ch == Character.LINE_SEPARATOR ) {
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

        protected ArrayList messageList ;

        protected int limit = Integer.MAX_VALUE;
        
        protected int tabwidth = 4 ;

        protected boolean convertTabsToSpaces = true ;

        protected char[] buffer = new char[1024];
    }

    public MessageWindow( String name ) {
        super( name ) ;
        model = new MessageListModel( messagesList ) ;
        getContentPane().setLayout( new BorderLayout() ) ;
        list = new JList( model );
        JScrollPane scrollPane = new JScrollPane( list ) ;
        scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS ) ;
        getContentPane().add( scrollPane, BorderLayout.CENTER ) ;
    }

    /**
     *  Add an element to be notified when a message is clicked upon.
     */
    public void addMessageSelectedListener() {

    }

    public MessageListModel getModel() {
        return model ;
    }

    /**
     *  Remove listener to messages.
     */
    public void removeMessageSelectedListener() {

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

    public void addMessages( String message ) {
        model.addMessages( message, null ) ;
    }

    /**
     * Return an array of Strings which are currently selected.
     */
    public Object[] getSelected() {
        return list.getSelectedValues() ;
    }

    public JList getListComponent() {
        return list ;
    }

    public void setMessageFont( Font f ) {
        list.setFont( f ) ;
    }

    /**
     *  A list of messages and associated tags, if any.
     */
    protected ArrayList messagesList = new ArrayList() ;

    /**
     *  A ListModel wrapping the <code>messagesList</code> and used with the
     *  JList component.
     */
    protected MessageListModel model ;
    
    protected JList list ;

    protected int limit = 1000 ;

    protected boolean scrollDown = true ;
        
    public static void main( String[] argv ) {

        MessageWindow window = new MessageWindow( "Test Window" ) ;
        window.setSize( 640, 480 ) ;
        
        window.setVisible( true ) ;

        String[] s = Toolkit.getDefaultToolkit().getFontList() ;
        window.list.setFont( new Font( "Monospaced", Font.PLAIN, 11 ) ) ;
        
        
        try{
        Thread.sleep(1000) ;
        }
        catch ( InterruptedException e ) {
        }
        
        for (int i=0;i<20;i++) {        
        window.addMessage( "The" );
        window.getModel().addMessage( "quick" ) ;
        window.getModel().addMessage( "brown" ) ;
        window.getModel().addMessage( "fox" );
        window.addMessages( "jumps" + ( char ) Character.LINE_SEPARATOR + "over" ) ;
        window.getModel().addMessage( "the \tlazy \tdog." ) ;
        window.getModel().addMessage( "This is a very long long long message." +
                                      " Antidisestablishmentarianism " + ( char ) Character.LINE_SEPARATOR +
                                      " pneumoultramicroscopicsilicovolcaniosis " + 
                                      " supercalafragilisticexpialadocious" );
        }
    }
}