/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  * CHANGE RECORD
  */
package org.cougaar.tools.castellan.server.ui;

import org.cougaar.tools.castellan.planlog.EventLog;
import org.cougaar.tools.castellan.pdu.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EventPDUTableComponent extends JPanel {

    static class EventTableModel implements TableModel {
        public static final String[] COLUMNS = { "Type", "UID", "Value" } ;

        public EventTableModel(EventLog log) {
            this.log = log;
            update() ;
        }

        public void update() {
            list.ensureCapacity( log.getNumEvents() );
            for ( Iterator iter = log.getEvents(); iter.hasNext(); ) {
                list.add( iter.next() ) ;
            }

            // Fire all the events.
            TableModelEvent tme = new TableModelEvent( this ) ;
            for (int i=0;i<listeners.size();i++) {
                TableModelListener tml = ( TableModelListener ) listeners.get(i) ;
                tml.tableChanged( tme );
            }
        }

        public int getRowCount() {
            return list.size();
        }

        public int getColumnCount() {
            return COLUMNS.length;
        }

        public String getColumnName(int columnIndex) {
            return COLUMNS[columnIndex];
        }

        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        protected Object getObject( int rowIndex ) {
            return list.get( rowIndex ) ;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            EventPDU epdu = ( EventPDU ) list.get( rowIndex ) ;
            switch ( columnIndex  ) {
                case 0 :
                   return EventPDU.typeToString( epdu.getType() ) ;
                case 1 :
                   if ( epdu instanceof UniqueObjectPDU ) {
                        UniqueObjectPDU updu = ( UniqueObjectPDU ) epdu ;
                        return updu.getUID() ;
                   }
                   else return null ;
                case 2 :
                    return epdu.toString() ;
            }
            return null ;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        public void addTableModelListener(TableModelListener l) {
            listeners.add(  l ) ;
        }

        public void removeTableModelListener(TableModelListener l) {
            listeners.add(  l ) ;
        }

        EventLog log ;
        ArrayList listeners = new ArrayList() ;
        ArrayList list = new ArrayList() ;
    }

    public EventPDUTableComponent( EventLog log ) {
        this.log = log ;
        buildLayout() ;
    }

    protected void findUID() {
        String value = uidTextField.getText() ;
        if ( value.length() == 0 || value == null ) {
            return ;
        }

        int selection = pduTable.getSelectedRow() ;
        int index = value.indexOf('/') ;
        if ( index == -1 ) {
            return ;
        }

        String owner = value.substring(0,index) ;
        String num = value.substring(index+1) ;
        long id = -1;
        try {
            id = Long.parseLong( num ) ;
        }
        catch ( Exception e ) {
            return ;
        }

        for ( int i=selection=1;i<model.getRowCount();i++) {
            UniqueObjectPDU updu = ( UniqueObjectPDU ) model.getObject(i) ;
            UIDStringPDU uid = ( UIDStringPDU ) updu.getUID() ;
            if ( uid.getOwner().equals( owner ) && uid.getId() == id ) {
                pduTable.getSelectionModel().setSelectionInterval( i, i );
                return ;
            }
        }
    }

    protected void buildLayout() {
        setLayout( new BorderLayout() ) ;
        JPanel topPanel = new JPanel( ) ;
        GridBagLayout gbl = new GridBagLayout() ;
        topPanel.setLayout( gbl );
        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.anchor = GridBagConstraints.WEST ; gbc.fill = GridBagConstraints.NONE ;
        gbc.insets = new Insets( 10, 10, 10, 10 ) ;

        findUIDButton = new JButton( "Find" ) ;
        gbl.setConstraints( findUIDButton, gbc );
        topPanel.add( findUIDButton ) ;

        uidTextField = new JTextField() ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.anchor = GridBagConstraints.CENTER ;
        gbc.weightx = 100 ;
        gbl.setConstraints( uidTextField, gbc );
        topPanel.add( uidTextField ) ;

        findUIDButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findUID();
            }
        });
        add( topPanel, BorderLayout.NORTH ) ;

        model = new EventTableModel( log ) ;
        pduTable = new JTable( model ) ;
        pduTable.setAutoscrolls( true );
        add( new JScrollPane( pduTable), BorderLayout.CENTER ) ;
    }

    protected EventTableModel model ;
    protected EventLog log ;
    protected JTable pduTable ;
    protected JTextField uidTextField ;
    protected JButton findUIDButton ;
}
