package org.cougaar.tools.castellan.server.ui;

import org.cougaar.tools.castellan.server.ui.events.LogReferenceEvent;
import org.cougaar.tools.castellan.server.ui.events.LogReferenceEventListener;
import org.cougaar.tools.castellan.analysis.Loggable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class PropertyPanel extends JPanel
{
    int row = 0 ;
    GridBagConstraints gbc = new GridBagConstraints() ;
    GridBagLayout gbl ;

    protected void dispatchLogEvent( LogReferenceEvent evt ) {
        for (int i=0;i<listeners.size();i++) {
            ( ( LogReferenceEventListener ) listeners.get(i) ).logReferenceOccured( evt );
        }
    }

    protected void addLogReferenceListener( LogReferenceEventListener l ) {
        if ( listeners.indexOf( l ) == -1 ) {
            listeners.add( l ) ;
        }
    }

    protected void removeLogReferenceListener( LogReferenceEventListener l ) {
        listeners.remove( l ) ;
    }

    protected void addProperty( String name, Component viewer, int height ) {
        gbc.gridx = 0; gbc.gridy = row ; gbc.fill = GridBagConstraints.NONE ; gbc.weightx = 0 ;
        gbc.anchor = GridBagConstraints.WEST ;
        Component prop = new JLabel( name ) ;
        gbl.setConstraints( prop, gbc );
        add( prop ) ;
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL ; gbc.weightx = 100 ;
        gbl.setConstraints( viewer, gbc );
        add( viewer ) ;
        row++ ;
        if ( viewer instanceof JComponent ) {
            JComponent panel = ( JComponent ) viewer ;
            panel.setMinimumSize( new Dimension( 32, height ) ) ;
        }
    }

    protected void addProperty( String name, String value ) {
        JTextField valuePanel = new JTextField( value ) ;
        valuePanel.setEditable( false );
        addProperty( name, valuePanel );
    }

    protected void addProperty( String name, Component viewer ) {
        gbc.gridx = 0; gbc.gridy = row ; gbc.fill = GridBagConstraints.NONE ; gbc.weightx = 0 ;
        gbc.anchor = GridBagConstraints.WEST ;
        Component prop = new JLabel( name ) ;
        gbl.setConstraints( prop, gbc );
        add( prop ) ;
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL ; gbc.weightx = 100 ;
        gbl.setConstraints( viewer, gbc );
        add( viewer ) ;
        row++ ;
    }

    class RefComponent extends JPanel {
        public RefComponent ( Loggable value )
        {
            log = value ;
            // Get the default font, etc.
        }

        public Dimension getMinimumSize ()
        {
            Graphics g = getGraphics() ;
            FontMetrics fm = g.getFontMetrics() ;
            int maxHeight = fm.getMaxAscent() + fm.getMaxDescent() ;
            return new Dimension( 0, maxHeight + 10 ) ;
        }

        public void paint ( Graphics g )
        {
            Graphics2D g2 = ( Graphics2D ) g ;
            Rectangle b = getBounds() ;
            g2.setColor( Color.WHITE );
            g2.fill( b );

        }

        Loggable log  ;
    }

    protected void addReference( String name, Loggable value ) {
        JTextField valuePanel = new JTextField() ;

        valuePanel.setEditable( false );
        // Add a mouseClicked listener.
        valuePanel.addMouseListener( new MouseAdapter() {
            public void mouseClicked ( MouseEvent e )
            {
                if ( e.getClickCount() == 1 ) {
                    // Resolve this
                }
            }
        });
        addProperty( name, valuePanel );
    }

    protected void resetPanelLayout() {
        row = 0 ;
    }

    protected void closePanelLayout() {
        JPanel dummyPanel = new JPanel() ;
        gbc.gridy = row ;
        gbc.weightx = 0; gbc.weighty = 1000 ;
        gbc.fill = GridBagConstraints.BOTH ;
        gbl.setConstraints( dummyPanel, gbc );
        add( dummyPanel ) ;
    }

    ArrayList listeners = new ArrayList() ;
}
