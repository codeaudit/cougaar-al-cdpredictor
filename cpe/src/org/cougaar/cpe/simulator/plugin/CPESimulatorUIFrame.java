package org.cougaar.cpe.simulator.plugin;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.cpe.ui.WorldDisplayPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class CPESimulatorUIFrame extends JFrame {
    private CPESimulatorPlugin plugin;
    private String agentName;
    private RefreshThread refreshThread;

    JMenuItem miConfigure;
    JMenuItem miStart;
    private JMenuItem miDumpMeasurementPoints;

    public CPESimulatorUIFrame( String agentName, CPESimulatorPlugin plugin ) {
        super( "World State " + agentName ) ;
        this.agentName = agentName ;

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( displayPanel = new WorldDisplayPanel( ws ) ) ;

        JMenuBar mb = new JMenuBar() ;
        setJMenuBar( mb );

        JMenu fileMenu = new JMenu( "File" ) ;
        mb.add( fileMenu ) ;

        miConfigure = new JMenuItem( "Configure" );
        miStart = new JMenuItem( "Start" );
        miStart.setEnabled( false );
        miDumpMeasurementPoints = new JMenuItem( "Dump data" ) ;

        fileMenu.add( miConfigure ) ;
        fileMenu.add( miStart ) ;
        fileMenu.add( miDumpMeasurementPoints ) ;

        miConfigure.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doConfigureAction() ;
            }
        });
        miStart.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doStartAction();
            }
        });
        miDumpMeasurementPoints.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doDumpMeasurementPoints();
            }
        });

        this.plugin = plugin ;
        setSize( 800, 600 );

        refreshThread = new RefreshThread();
        refreshThread.start();
    }

    private void doDumpMeasurementPoints()
    {
        JFileChooser fc = new JFileChooser() ;
        fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        fc.showOpenDialog( this ) ;

        if ( fc.getSelectedFile() != null ) {
            File f = fc.getSelectedFile() ;
            plugin.setSaveMPOutDir( f.getPath() );
            plugin.doDumpMeasurementPoints();
        }
    }

    private void doConfigureAction() {
        plugin.doConfigureWorld();
        miConfigure.setEnabled( false );
        miStart.setEnabled( true );

    }

    private void doStartAction() {
        plugin.startTimeAdvance();
        miStart.setEnabled( false );
    }

    public WorldState getWorldState() {
        return ws;
    }

    public void setWorldState(WorldState ws) {
        this.ws = ws;
        displayPanel.setWorldState( ws );
    }

    public class RefreshThread extends Thread {

        public void run() {
            while (true) {
                try {
                    Thread.sleep(refreshRate);
                } catch (InterruptedException e) {
                }

                if ( displayPanel != null) {
                    displayPanel.repaint();
                    WorldState ws = displayPanel.getWorldState() ;
                    if ( ws != null ) {
                        setTitle( "WorldState " + agentName + " [Time=" +
                            ( ws.getTime() * VGWorldConstants.SECONDS_PER_MILLISECOND ) +
                            ", Score=" + ws.getScore() + "]" );
                    }
                }
            }
        }
    }

    long refreshRate = 1000 ;
    WorldDisplayPanel displayPanel ;
    WorldState ws ;
}
