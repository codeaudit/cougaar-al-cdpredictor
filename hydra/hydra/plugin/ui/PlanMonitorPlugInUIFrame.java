/*
 * PlanMonitorUIFrame.java
 *
 * Created on July 26, 2001, 3:28 PM
 */

package org.hydra.plugin.ui;
import org.hydra.plugin.* ;
import javax.swing.text.* ;
import javax.swing.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class PlanMonitorPlugInUIFrame extends javax.swing.JFrame {

    /** Creates new PlanMonitorUIFrame */
    public PlanMonitorPlugInUIFrame(PlanMonitorPlugIn plugIn) {
        this.plugIn = plugIn ;
        setSize( 512, 480 ) ;
        build() ;
    }
    
    public void log( String s ) {
       logTextArea.append( s ) ; 
    }
    
    private void build() {
        getContentPane().add( tabbedPane = new JTabbedPane() ) ;    
        getContentPane().add( logTextArea = new JTextArea() ) ;
        logTextArea.setEditable( false ) ;
    }

    JTabbedPane tabbedPane ;
    JTextArea logTextArea ;
    PlanMonitorPlugIn plugIn ;
}