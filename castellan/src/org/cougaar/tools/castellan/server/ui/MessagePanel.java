/*
 * MessagePanel.java
 *
 * Created on October 1, 2001, 10:41 AM
 */

package org.cougaar.tools.castellan.server.ui;
import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class MessagePanel extends javax.swing.JPanel {

    /** Creates new MessagePanel */
    public MessagePanel() {
        super( false ) ;
        buildLayout() ;
        buildMenus() ;
    }

    private void buildLayout() {
        setLayout( new BorderLayout() ) ;
        textArea = new JTextArea() ;
        textArea.setFont( new Font( "Courier", Font.PLAIN, 12 ) ) ;
        textArea.setEditable( false ) ;
        JScrollPane sp = new JScrollPane( textArea ) ;
        sp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ) ;
        add( sp, BorderLayout.CENTER ) ;
        textArea.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                if ( ( e.getModifiers() & InputEvent.BUTTON3_MASK ) == 0 ) {
                    return ;   
                }
                
                menu.setInvoker( textArea ) ;
                Point p = textArea.getLocationOnScreen() ;
                menu.setLocation( p.x + e.getX(), p.y + e.getY() ) ;   
                menu.setVisible( true ) ;
            }
        } ) ;
    }
    
    public void append( String s ) {
        textArea.append( s ) ;   
    }
    
    private void buildMenus() {
        menu = new JPopupMenu() ;
        JMenuItem miClear = new JMenuItem( "Clear" ) ;
        menu.add( miClear ) ;
        miClear.setMnemonic( 'L' ) ;
        JMenuItem miCopy = new JMenuItem( "Copy" ) ;
        menu.add( miCopy ) ;
        miCopy.setMnemonic( 'C' ) ;
        JMenuItem miSelectAll = new JMenuItem( "Select all" ) ;
        menu.add( miSelectAll ) ;
        miSelectAll.setMnemonic( 'S' ) ;
        
        miClear.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                textArea.setText( "" ) ;   
            }
        } ) ;
        
        miCopy.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                textArea.copy() ;
            }
        } ) ;
        
        miSelectAll.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                textArea.selectAll() ;
            }
        } ) ;
    }
    
    JPopupMenu menu ;
    JTextArea textArea ;
}
