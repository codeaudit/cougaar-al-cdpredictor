package org.cougaar.cpe.ui;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.cpe.agents.plugin.C2AgentPlugin;
import org.cougaar.cpe.agents.plugin.WorldStateReference;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.DelayMeasurementPoint;
import org.cougaar.tools.techspecs.qos.EventDurationMeasurementPoint;

import javax.swing.*;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ComponentPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: wpeng
 * Date: May 31, 2003
 * Time: 5:09:20 PM
 */
public class AgentDisplayPanel extends JFrame {

    private String agentName;

    private RefreshThread refreshThread;
    private JScrollPane spMeasurementPanel;

    public AgentDisplayPanel( String agentName, AgentDisplayPlugin plugin ) {
        this.plugin = plugin ;
        this.agentName = agentName ;
        sp = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;
        Container c = getContentPane() ;
        c.setLayout( new BorderLayout() );
        c.add( sp, BorderLayout.CENTER ) ;
        sp.setLeftComponent( leftDisplayPanel = new WorldDisplayPanel( null ) );
        setSize( 640, 480 ) ;
        updateTitle();

        sp.setRightComponent( rightTabbedPane = new JTabbedPane() );
        rightTabbedPane.add( "Measurements", spMeasurementPanel = new JScrollPane( measurementPanel = new JPanel() ) ) ;
        if ( spMeasurementPanel != null ) {
            spMeasurementPanel.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
            spMeasurementPanel.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        }
        rightTabbedPane.add( "Controls", controlPanel = new ControlsPanel() ) ;
        sp.setDividerLocation( 400 );

        refreshThread = new RefreshThread() ;
        refreshThread.start();
    }

    protected void updateTitle() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "Display Panel [Agent=" ) ;
        buf.append( agentName ) ;
        buf.append( "]" ) ;
//        if ( ws != null )  {
//            buf.append( ", time=").append( ws.getTime() * VGWorldConstants.MILLISECONDS_PER_SECOND ) ;
//        }
        setTitle( buf.toString() );
    }

    public void updateWorldStates( ArrayList worldStateReferences ) {
        int dividerLocation = sp.getDividerLocation() ;
        if ( worldStateReferences.size() == 0 ) {
            sp.setLeftComponent( new JPanel());
            leftTabbedPane.removeAll();
            leftDisplayPanel = null ;
        }
        else if ( worldStateReferences.size() == 1 ) {
            sp.setLeftComponent( leftDisplayPanel = new WorldRefDisplayPanel( (WorldStateReference) worldStateReferences.get(0) ));
            // Free up these.
            leftTabbedPane.removeAll();
        }
        else {
            leftTabbedPane.removeAll();
            for (int i = 0; i < worldStateReferences.size(); i++) {
                WorldStateReference worldStateReference = (WorldStateReference)worldStateReferences.get(i);
                WorldRefDisplayPanel panel = new WorldRefDisplayPanel( worldStateReference ) ;
                leftTabbedPane.add( worldStateReference.getName(), panel ) ;
            }
            sp.setLeftComponent( leftTabbedPane );
        }
        sp.setDividerLocation( dividerLocation );
    }

    protected Dimension panelSize = new Dimension( 320, 320 ) ;

    protected void updateMeasurements( ArrayList measurementPoints ) {
        // System.out.println("\n\nAgentDisplayPanel:: DEBUG UPDATING MP DISPLAY ");
        measurementPanel.removeAll();
        mpDisplayPanels.clear();

        GridBagLayout gbl = new GridBagLayout() ;
        measurementPanel.setLayout( gbl );
        int gridx =0, gridy =0 ;

        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.fill = GridBagConstraints.BOTH ;
        gbc.weighty = 100 ; gbc.weightx = 100 ;
        gbc.anchor = GridBagConstraints.CENTER ;

        for (int i = 0; i < measurementPoints.size(); i++) {
            MeasurementPoint measurementPoint = (MeasurementPoint)measurementPoints.get(i);
            if ( measurementPoint instanceof EventDurationMeasurementPoint ) {
                gbc.gridx = gridx; gbc.gridy = gridy ;
                EventDurationMeasurementPoint emp = (EventDurationMeasurementPoint) measurementPoint ;
                EventDurationPlotPanel panel = new EventDurationPlotPanel( plugin, emp ) ;
                panel.setMinimumSize( panelSize ) ;
                panel.setPreferredSize( panelSize ) ;
                gbl.setConstraints( panel, gbc );
                measurementPanel.add( panel ) ;
                mpDisplayPanels.add( panel ) ;
                gridy ++ ;
            }
            else if ( measurementPoint instanceof DelayMeasurementPoint ) {
                gbc.gridx = gridx; gbc.gridy = gridy ;
                // System.out.println("\n\nDEBUG: AgentDisplayPanel:: ADDING DELAY MEASUREMENT POINT FOR " + measurementPoint );
                DelayMeasurementPoint dmp = (DelayMeasurementPoint) measurementPoint ;
                DelayPlotPanel2 panel = new DelayPlotPanel2( plugin, dmp ) ;
                panel.setMinimumSize( panelSize ) ;
                panel.setPreferredSize( panelSize ) ;
                gbl.setConstraints( panel, gbc );
                measurementPanel.add( panel ) ;
                mpDisplayPanels.add( panel ) ;
                gridy ++ ;
            }

            // Move to the next display element.
        }

        // Add an empty panel to take up any leftover space
//        JPanel dp = new JPanel() ;
//        gbl.setConstraints( dp, gbc ) ;
//        mpDisplayPanels.add( dp ) ;

        if ( spMeasurementPanel != null ) {
            spMeasurementPanel.invalidate(); spMeasurementPanel.validate(); spMeasurementPanel.repaint();
        }
        measurementPanel.invalidate();
        measurementPanel.repaint();
    }

    protected void updateControls( ArrayList operatingModes ) {
        controlPanel.setOperatingModes( operatingModes );
    }

    private void updateMeasurementPanels() {
        for (int i = 0; i < mpDisplayPanels.size(); i++) {
            Object o = mpDisplayPanels.get(i) ;
            if ( o instanceof DelayPlotPanel ) {
                DelayPlotPanel delayPlotPanel = (DelayPlotPanel) o ;
                delayPlotPanel.updateData();
            }
            else if ( o instanceof DelayPlotPanel2 ) {
                DelayPlotPanel2 p = (DelayPlotPanel2) o ;
                p.updateData();
            }
            else if ( o instanceof EventDurationPlotPanel ) {
                EventDurationPlotPanel ep = (EventDurationPlotPanel) o ;
                ep.updateData();
            }
        }
    }


    public void execute() {
    }

    ArrayList worldStatePanels = new ArrayList() ;
    ArrayList opModeControlPanels = new ArrayList() ;
    ArrayList mpDisplayPanels = new ArrayList() ;

    JTabbedPane rightTabbedPane = new JTabbedPane();

    /**
     * If this non-null, this is the left panel.
     */
    JTabbedPane leftTabbedPane = new JTabbedPane();

    JPanel measurementPanel;
    ControlsPanel controlPanel ;
    WorldDisplayPanel leftDisplayPanel ;
    AgentDisplayPlugin plugin ;
    JSplitPane sp ;

    private class RefreshThread extends Thread {
        public void run() {
            while ( true ) {
                try {
                    Thread.sleep( 5000 );
                } catch (InterruptedException e) {

                }

                controlPanel.updateValues();
                updateMeasurementPanels() ;
                if ( leftDisplayPanel != null ) {
                    leftDisplayPanel.repaint();
                }

                for (int i = 0; i < worldStatePanels.size(); i++) {
                    WorldRefDisplayPanel worldRefDisplayPanel = (WorldRefDisplayPanel)worldStatePanels.get(i);
                    worldRefDisplayPanel.repaint();
                }
            }
        }

    }
}

//        ArrayList newWorldStateRefs = new ArrayList() ;
//        ArrayList removedPanels = new ArrayList() ;
//        for (int i = 0; i < worldStateReferences.size(); i++) {
//            WorldStateReference worldStateReference = (WorldStateReference)worldStateReferences.get(i);
//
//            boolean found = false ;
//            for (int j = 0; j < worldStatePanels.size(); j++) {
//                WorldRefDisplayPanel worldRefDisplayPanel = (WorldRefDisplayPanel)worldStatePanels.get(j);
//                if ( worldRefDisplayPanel.getWorldStateRefence() == worldStateReference ) {
//                    found = true ;
//                    break ;
//                }
//            }
//            if ( !found ) {
//                newWorldStateRefs.add( worldStateReference ) ;
//            }
//        }
//
//        for (int i = 0; i < worldStatePanels.size(); i++) {
//            WorldRefDisplayPanel worldRefDisplayPanel = (WorldRefDisplayPanel)worldStatePanels.get(i);
//
//            boolean found = false ;
//            for (int j = 0; j < worldStateReferences.size(); j++) {
//                WorldStateReference worldStateReference = (WorldStateReference)worldStateReferences.get(j);
//                if ( worldStateReference == worldRefDisplayPanel.getWorldStateRefence() ) {
//                    found = true ;
//                    break ;
//                }
//            }
//            if ( !found ) {
//                removedPanels.add( worldRefDisplayPanel ) ;
//            }
//        }
//
//        if ( newWorldStateRefs.size() > 0 || removedPanels.size() > 0 ) {
//            // Remove the old panels.
//            worldStatePanels.removeAll( removedPanels ) ;
//
//            // Add the new pan
//            for (int i = 0; i < newWorldStateRefs.size(); i++) {
//                WorldStateReference worldStateReference = (WorldStateReference)newWorldStateRefs.get(i);
//                WorldRefDisplayPanel newPanel = new WorldRefDisplayPanel( worldStateReference ) ;
//                worldStatePanels.add( newPanel ) ;
//            }
//
//            if ( worldStatePanels.size() == 0 ) {
//               sp.setLeftComponent( new JPanel() );
//            }
//
//            if ( worldStatePanels.size() == 1 ) {
//               sp.setLeftComponent( ( JPanel ) worldStatePanels.get(0) ) ;
//            }
//        }

