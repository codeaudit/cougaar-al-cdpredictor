package org.cougaar.tools.castellan.server.ui;

import org.cougaar.tools.castellan.analysis.*;
import org.cougaar.tools.castellan.server.ui.events.LogReferenceEvent;
import org.cougaar.tools.castellan.server.ui.events.LogReferenceEventListener;
import org.cougaar.tools.alf.*;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;

import sun.rmi.runtime.Log;

/**
 * Display an aggregate log.
 */

public class AggregateLogPanel
        extends PropertyPanel
{
    public static class LogReference {
        public LogReference ( Loggable lb )
        {
            this.lb = lb;
        }

        public Loggable getLog ()
        {
            return lb;
        }

        Loggable lb ;
    }

    public AggregateLogPanel( AggregateLog log  ) {
        this.log = log ;
        updateLayout();
    }

    public String getTypeStringForAggregateLog( AggregateLog log ) {
        if ( log instanceof BoundaryVerbTaskAggregate ) {
            return "Boundary task aggregate" ;
        }
        else if ( log instanceof AggregateVerbTaskLog ) {
            return "Verb task aggregate" ;
        }
        else {
            return log.getClass().toString() ;
        }
    }

    public void updateLayout() {
        removeAll();
        setLayout( gbl = new GridBagLayout() );
        // R
        resetPanelLayout() ;

        JTextField tfType = new JTextField( getTypeStringForAggregateLog( log ) ) ;
        tfType.setEditable( false );
        addProperty( "Type", tfType ) ;

        if ( log instanceof BoundaryVerbTaskAggregate ) {
            BoundaryVerbTaskAggregate bvta = ( BoundaryVerbTaskAggregate ) log ;
            JTextField tfBoundaryType = new JTextField( BoundaryConstants.toParamString( bvta.getBoundaryType() ) ) ;
            tfBoundaryType.setEditable( false );
            addProperty( "Boundary type", tfBoundaryType );
        }

        if ( log instanceof AggregateVerbTaskLog ) {
            AggregateVerbTaskLog avtl = ( AggregateVerbTaskLog ) log ;
            JTextField tfVerb = new JTextField( avtl.getVerb() ) ;
            tfVerb.setEditable( false );
            addProperty( "Verb", tfVerb );

            JTextField tfCluster = new JTextField( avtl.getCluster() ) ;
            tfCluster.setEditable(false);
            addProperty( "Agent", tfCluster );
        }

        // Now, make a list of child logs.  Make this a JList of LogRef items.

        final JList childList = new JList( ) ;
        childList.addMouseListener( new MouseAdapter() {
            public void mouseClicked ( MouseEvent e )
            {
                if ( e.getClickCount() == 2 ) {
                    UniqueObjectLog ul = ( UniqueObjectLog ) childList.getSelectedValue() ;
                    dispatchLogEvent( new LogReferenceEvent( AggregateLogPanel.this, ul ) );
                }
            }
        });
        DefaultListModel model = new DefaultListModel() ;
        childList.setModel( model );
        for ( Iterator iter = log.getLogs().iterator(); iter.hasNext(); ) {
            Loggable l = ( Loggable ) iter.next() ;
            model.addElement( l );
        }
        addProperty( "Instances", new JScrollPane(childList), 400 );

        closePanelLayout() ;
    }

//    protected MouseAdapter refLinkClickedAdapter = new MouseAdapter() {
//        public void mouseClicked ( MouseEvent e )
//        {
//
//        }
//    }

    AggregateLog log ;
}
