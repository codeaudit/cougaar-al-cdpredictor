/*
 * EditSettingsDialog.java
 *
 * Created on July 17, 2001, 5:22 PM
 */

package org.hydra.server.ui;
import java.awt.* ;
import javax.swing.* ;
import java.awt.event.* ;
import org.hydra.server.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class EditSettingsDialog extends JDialog {

    /** Creates new EditSettingsDialog */
    public EditSettingsDialog( Frame frame, ServerApp app ) {
        super( frame ) ;
        setTitle( "Edit settings" ) ;
        setSize( 512, 250 ) ;
        setModal( true ) ;
        this.app = app ;
        createLayout() ;
        if ( app.getDotPath() != null ) {
        dotPath.setText( app.getDotPath() ) ;
        }
        if ( app.getTempPath() != null ) {
          tempPath.setText( app.getTempPath() ) ;   
        }
        if ( app.getDbPath() != null ) {
            dbPath.setText( app.getDbPath() );
        }
    }
    
    protected void createLayout() {
        GridBagLayout gbl = new GridBagLayout() ;
        getContentPane().setLayout( gbl ) ;
        
        Insets leftInsets = new Insets( 10, 5, 10, 5 ) ;
        Insets rightInsets = new Insets( 10, 5, 5, 10 ) ;
        GridBagConstraints gbc = new GridBagConstraints() ; gbc.anchor = GridBagConstraints.WEST ;
        gbc.weightx = 1 ;
        gbc.insets = leftInsets ;
        JLabel dpLabel = new JLabel( "Dot path" ) ;
        gbl.setConstraints( dpLabel, gbc ) ;
        getContentPane().add( dpLabel ) ;

        gbc.gridx = 1 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ;
        gbc.insets = rightInsets ;
        dotPath = new JTextField() ;
        gbl.setConstraints( dotPath, gbc ) ;
        getContentPane().add( dotPath ) ;

        gbc.gridx = 2 ; gbc.weightx = 1; gbc.fill = GridBagConstraints.NONE ;
        JButton browseDotPath = new JButton( "Browse..." ) ;
        browseDotPath.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               doBrowseDotPath() ;
            }
        } ) ;
        gbl.setConstraints( browseDotPath, gbc ) ;
        getContentPane().add( browseDotPath ) ;

        // Handle the third row
        gbc.gridx = 0 ; gbc.gridy = 2 ; gbc.fill = GridBagConstraints.NONE ;
        gbc.weightx = 1 ;
        gbc.insets = leftInsets ;
        JLabel tempLabel = new JLabel( "Temp path" ) ;
        gbl.setConstraints( tempLabel, gbc ) ;
        getContentPane().add( tempLabel ) ;

        gbc.gridx = 1 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ;
        gbc.insets = rightInsets ;
        tempPath = new JTextField() ;
        gbl.setConstraints( tempPath, gbc ) ;
        getContentPane().add( tempPath ) ;

        gbc.gridx = 2 ; gbc.weightx = 1; gbc.fill = GridBagConstraints.NONE ;
        JButton browseTempPath = new JButton( "Browse..." ) ;
        gbl.setConstraints( browseTempPath, gbc ) ;
        getContentPane().add( browseTempPath ) ;
        browseTempPath.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               doBrowseTempPath() ;
            }
        } ) ;

        // Database path text field
        gbc.gridx = 0 ; gbc.gridy = 3 ; gbc.fill = GridBagConstraints.NONE ;
        gbc.weightx = 1 ;
        gbc.insets = leftInsets ;
        JLabel dbLabel = new JLabel( "Database path" ) ;
        gbl.setConstraints( dbLabel, gbc ) ;
        getContentPane().add( dbLabel ) ;

        gbc.gridx = 1 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ;
        gbc.insets = rightInsets ;
        dbPath = new JTextField() ;
        gbl.setConstraints( dbPath, gbc ) ;
        getContentPane().add( dbPath ) ;

        // Dummy panel to take up extra space.
        JPanel dummy = new JPanel() ;
        gbc.gridx = 0 ; gbc.gridy = 8 ; gbc.gridwidth = 3 ; gbc.fill = GridBagConstraints.BOTH ;
        gbc.weightx = 100; gbc.weighty = 100 ;
        gbl.setConstraints( dummy, gbc ) ;
        getContentPane().add( dummy ) ;
        
        // Add ok and cancel buttons
        JPanel okCancelPanel = new JPanel() ;
        okCancelPanel.setLayout( new FlowLayout() ) ;
        JButton okButton = new JButton( "OK" ) ;
        JButton cancelButton = new JButton( "Cancel" ) ;
        okCancelPanel.add( okButton ) ;
        okCancelPanel.add( cancelButton ) ;
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
        
        gbc.fill = GridBagConstraints.HORIZONTAL ; gbc.gridy = 9;
        gbc.insets = rightInsets ;
        gbc.weighty = 1 ;
        gbl.setConstraints( okCancelPanel, gbc ) ;
        getContentPane().add( okCancelPanel ) ;
    }
    
    protected void doOKAction() {
        app.setDotPath( dotPath.getText() ) ;
        app.setTempPath( tempPath.getText() ) ;
        app.setDbPath( dbPath.getText( ) ) ;
        setVisible( false ) ;
    }
    
    protected void doCancelAction() {
        setVisible( false ) ;   
    }
    
    protected void doBrowseDotPath() {
        JFileChooser fc = new JFileChooser(dotPath.getText()) ;
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY ) ;
        int result = fc.showDialog( this, "Select" ) ;
        
        if ( result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null ) {
             dotPath.setText( fc.getSelectedFile().toString() ) ;
        }
    }
    
    protected void doBrowseTempPath() {
        JFileChooser fc = new JFileChooser( tempPath.getText() ) ;
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
        int result = fc.showDialog( this, "Select" ) ;
        
        if ( result == JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null ) {
             tempPath.setText( fc.getSelectedFile().toString() ) ;
        }                
    }

    JTextField dotPath, gsviewPath, tempPath, dbPath ;
    ServerApp app ;
}
