package org.cougaar.cpe.agents.plugin;

import org.cougaar.cpe.ui.WorldDisplayPanel;
import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.util.UnaryPredicate;

public class C2AgentDisplayUI extends JFrame {
    private WorldState ws;
    private ServiceBroker broker;

    public C2AgentDisplayUI( ServiceBroker broker, C2AgentPlugin plugin ) {
        this.plugin = plugin ;
        this.broker = broker ;
        JSplitPane sp = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;
        Container c = getContentPane() ;
        c.setLayout( new BorderLayout() );
        c.add( sp, BorderLayout.CENTER ) ;
        sp.setLeftComponent( panel = new WorldDisplayPanel( null ) );
        setSize( 640, 480 ) ;
        updateTitle();

        sp.setRightComponent( tabbedPane = new JTabbedPane() );
        tabbedPane.add( "Measurements", measurementPanel = new JPanel() ) ;
        tabbedPane.add( "Controls", controlPanel = new JPanel() ) ;
        sp.setDividerLocation( 500 );
    }

    protected void updateTitle() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "World State " ) ;
        buf.append( plugin.getAgentIdentifier().getAddress() ) ;
        if ( ws != null )  {
            buf.append( ", time=").append( ws.getTime() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
        }
        setTitle( buf.toString() );
    }

    protected void updateMeasurements( ArrayList measurementPoints ) {

    }

    protected void updateControls( ArrayList operatingModes ) {
    }

    public void setWorldState( WorldState ws ) {
        this.ws = ws ;
        panel.setWorldState( ws );
        updateTitle();
        repaint();
    }

//    public void setupSubscriptions() {
//        BlackboardService bs = (BlackboardService) broker.getService( plugin, BlackboardService.class, null ) ;
//        bs.subscribe( new UnaryPredicate() {
//            public boolean execute(Object o) {
//                return o instanceof MeasurementPoint ;
//            }
//        }) ;
//    }

    public void execute() {
    }

    JTabbedPane tabbedPane ;
    JPanel measurementPanel, controlPanel ;
    WorldDisplayPanel panel ;
    C2AgentPlugin plugin ;
}
