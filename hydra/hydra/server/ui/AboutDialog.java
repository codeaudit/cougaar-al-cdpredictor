
//Title:        Hydra
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       
//Company:      
//Description:  Your description

package org.hydra.server.ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.* ;

public class AboutDialog extends JDialog {
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();

    public AboutDialog( Frame frame ) {
        super(frame, "About Castellan", true );
        setSize( 320, 240 ) ;
        setResizable( false );
        try  {
            buildWindows();
            //pack();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    void buildWindows() throws Exception {
        panel1.setLayout(borderLayout1);
        getContentPane().add(panel1);


        // Main about panel
        GridBagLayout gbl = new GridBagLayout() ;
        JPanel aboutPanel = new JPanel( gbl ) ;
        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.anchor = GridBagConstraints.CENTER ;


        JPanel spanel1 = new JPanel() ;
        gbc.fill = GridBagConstraints.BOTH ; gbc.weightx = 100 ; gbc.weighty = 100 ;
        gbl.setConstraints( spanel1, gbc );
        aboutPanel.add( spanel1 ) ;

        gbc.gridy = 1 ; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0 ; gbc.weighty = 0 ;
        panel1.add( aboutPanel, BorderLayout.CENTER ) ;
        JLabel aboutLabel = new JLabel( "Castellan" ) ;
        gbl.setConstraints( aboutLabel, gbc ) ;
        aboutPanel.add( aboutLabel ) ;


        JLabel versionLabel = new JLabel( "Version 1.0" ) ;
        gbc.gridy = 2 ;
        gbl.setConstraints( versionLabel, gbc );
        aboutPanel.add( versionLabel ) ;

        gbc.gridy = 3 ; gbc.fill = GridBagConstraints.BOTH ; gbc.weightx = 100 ;
        gbc.weighty = 100 ;
        JPanel spanel2 = new JPanel() ;
        gbl.setConstraints( spanel2, gbc );
        aboutPanel.add( spanel2 ) ;

        okButton = new JButton( "OK" ) ;
        gbc.gridy = 4 ; gbc.anchor = gbc.EAST ; gbc.fill = GridBagConstraints.NONE ;
        gbc.insets = new Insets( 0, 10, 10, 10 ) ;
        gbc.weightx = 0 ; gbc.weighty = 0 ;
        gbl.setConstraints( okButton, gbc );
        aboutPanel.add( okButton ) ;
        okButton.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e ) {
              setVisible( false ) ;
          }
        } );
    }

    JButton okButton ;
}
