package org.cougaar.cpe.simulator.plugin;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.cpe.model.WorldMetrics;
import org.cougaar.cpe.model.MeasuredWorldMetrics;
import org.cougaar.cpe.ui.WorldDisplayPanel;
import org.cougaar.cpe.unittests.MetricsPanel;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Iterator;
import java.util.Collection;

public class CPESimulatorUIFrame extends JFrame {
    private CPESimulatorPlugin plugin;
    private String agentName;
    private RefreshThread refreshThread;

    JMenuItem miConfigure;
    JMenuItem miStart;
    private JMenuItem miDumpMeasurementPoints;
    private MetricsPanel metricsGraphPanel;

    public CPESimulatorUIFrame( String agentName, CPESimulatorPlugin plugin ) {
        super( "World State " + agentName ) ;
        this.agentName = agentName ;

        JTabbedPane mainPanel = new JTabbedPane() ;
        mainPanel.addTab( "World", displayPanel = new WorldDisplayPanel( ws ) ); ;

        metricsGraphPanel = new MetricsPanel( ws ) ;
        mainPanel.addTab( "Metrics", metricsGraphPanel ); ;

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( mainPanel  ) ;


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

            dumpLocalMetrics( f ) ;
        }
    }

    private void dumpLocalMetrics(File f)
    {
        WorldMetrics wm = ws.getDefaultMetric() ;
        if ( wm instanceof MeasuredWorldMetrics ) {
            // Now, dump each of the measurement points.
            MeasuredWorldMetrics mwm = (MeasuredWorldMetrics) wm ;
            synchronized ( mwm ) {
                dumpTimePeriodMeasurements( f, (TimePeriodMeasurementPoint) mwm.getEntryRate() ) ;
                dumpTimePeriodMeasurements( f, (TimePeriodMeasurementPoint) mwm.getKills() ) ;
                dumpTimePeriodMeasurements( f, (TimePeriodMeasurementPoint) mwm.getPenalties() ) ;
                dumpTimePeriodMeasurements( f, (TimePeriodMeasurementPoint) mwm.getViolations() ) ;
                dumpTimePeriodMeasurements( f, (TimePeriodMeasurementPoint) mwm.getAttrition() ) ;
                dumpTimePeriodMeasurements( f, mwm.getFuelConsumption() );
                Collection c = mwm.getFuelConsumptionMeasurementPoints() ;
                for (Iterator iterator = c.iterator(); iterator.hasNext();)
                {
                    TimePeriodMeasurementPoint measurementPoint = (TimePeriodMeasurementPoint) iterator.next();
                    dumpTimePeriodMeasurements( f, measurementPoint );
                }
            }
        }
    }

    private void dumpTimePeriodMeasurements(File f, TimePeriodMeasurementPoint mp)
    {
        File newFile = new File( f, mp.getName() + ".csv" ) ;
        try
        {
            FileOutputStream fos = new FileOutputStream( newFile ) ;
            PrintWriter pw = new PrintWriter( fos ) ;
            Iterator iter = mp.getMeasurements() ;
            while (iter.hasNext())
            {
                TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
                pw.print( measurement.getStartTime() );
                if ( iter.hasNext() ) {
                    pw.print( ",") ;
                }
            }
            pw.println();
            iter = mp.getMeasurements() ;
            while (iter.hasNext())
            {
                TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
                pw.print( measurement.getEndTime() );
                if ( iter.hasNext() ) {
                    pw.print( ",") ;
                }
            }
            pw.println();
            iter = mp.getMeasurements() ;
            while (iter.hasNext())
            {
                TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
                pw.print( measurement.getValue() );
                if ( iter.hasNext() ) {
                    pw.print( ",") ;
                }
            }
            pw.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
        metricsGraphPanel.setWorldState( ws );
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
