package com.axiom.lib.awt;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import com.axiom.lib.awt.* ;


/**
 *  Standard dialog for selecting from a group of items.
 */
public class ListSelectionDialog extends JDialog {
    int state = JOptionPane.CANCEL_OPTION ;
    JPanel panel1 = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JList list = new JList();
    DefaultListModel listModel = new DefaultListModel() ;
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JLabel messageLabel = new JLabel();

    public ListSelectionDialog(Dialog dialog, String title, String message, boolean modal) {
        super(dialog, title, modal);
        init( message ) ;
    }

    public ListSelectionDialog(Frame frame, String title, String message, boolean modal) {
        super(frame, title, modal);
        init( message ) ;
    }

    public void setSelectionMode( int mode ) {
        list.setSelectionMode( mode );
    }

    protected void init( String message ) {
        try  {
            jbInit();
            pack();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        if ( message != null )
            messageLabel.setText( message );
        else {
            messageLabel.setText( "" ) ;
        }
        list.setModel( listModel );
        pack() ;
    }

    public ListSelectionDialog() {
        this( ( Frame ) null, "", "", false);
    }

    public int getState() {
        return state ;
    }

    public Object getSelectedValue() {
        return list.getSelectedValue() ;
    }

    public Object[] getSelectedValues() {
        return list.getSelectedValues() ;
    }

    public ListModel getListModel() {
        return listModel ;
    }

    public void setValues( Object[] values ) {
        listModel.removeAllElements();
        for (int i=0;i<values.length;i++) {
            if ( values[i] != null ) {
                listModel.addElement( values[i] );
            }
        }
    }

    void jbInit() throws Exception {
        panel1.setLayout(gridBagLayout1);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doOKAction(e);
            }
        });
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                doCancelAction(e);
            }
        });
        messageLabel.setText("None");
        getContentPane().add(panel1);
        panel1.add(jScrollPane1, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 15, 0, 15), 396, 296));
        panel1.add(okButton, new GridBagConstraints(0, 2, 1, 1, 100.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel1.add(cancelButton, new GridBagConstraints(1, 2, 1, 1, 100.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
        panel1.add(messageLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(15, 15, 5, 15), 0, 0));
        jScrollPane1.getViewport().add(list, null);
    }

    void doOKAction(ActionEvent e) {
        state = JOptionPane.OK_OPTION ;
        setVisible( false ) ;
    }

    void doCancelAction(ActionEvent e) {
        state = JOptionPane.CANCEL_OPTION ;
        setVisible( false ) ;
    }
}

