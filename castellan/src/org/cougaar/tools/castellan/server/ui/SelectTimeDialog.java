/*
 * SelectTimeDialog.java
 *
 * Created on August 29, 2001, 3:35 PM
 */

package org.cougaar.tools.castellan.server.ui;
import org.cougaar.tools.castellan.util.libui.* ;
import javax.swing.* ;
import java.awt.event.* ;
import java.awt.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class SelectTimeDialog extends javax.swing.JDialog {

    /** Creates new SelectTimeDialog */
    public SelectTimeDialog(long start, long end ) {
        setModal( true ) ;
        setSize( 600, 400 ) ;
        getContentPane().setLayout( new BorderLayout() ) ;
         trs = new TimeRangeSelector( start, end ) ;
        getContentPane().add( trs, BorderLayout.CENTER ) ;
        
        JPanel okPanel = new JPanel( new FlowLayout() ) ;
        JButton okButton = new JButton( "OK" ) ;
        JButton cancelButton = new JButton( "Cancel" ) ;
        okPanel.add( okButton ) ;
        okPanel.add( cancelButton ) ;
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doOKAction() ;
            }
        } ) ;
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doCancelAction() ;   
            }
        } ) ;
        getContentPane().add( okPanel, BorderLayout.SOUTH ) ;
    }
    
    public long getSelectedStartTime() {
        return trs.getSelectedStartTime() ;
    }
    
    public long getSelectedEndTime() {
        return trs.getSelectedEndTime() ;
    }
    
    public int getResult() {
        return result ;   
    }

    public void doOKAction() {
        result = JOptionPane.OK_OPTION ;
        setVisible( false ) ;
    }
    
    public void doCancelAction() {
        result = JOptionPane.CANCEL_OPTION ;
        setVisible( false ) ;
    }
    
    TimeRangeSelector trs ;
    int result = JOptionPane.CANCEL_OPTION ;
}
