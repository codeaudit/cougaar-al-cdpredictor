package org.cougaar.cpe.unittests;

import org.cougaar.cpe.ui.WorldDisplayPanel;
import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.cpe.model.ReferenceWorldState;
import org.cougaar.cpe.mplan.ManueverPlanner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

public class VGFrame extends JFrame {
    private RefreshThread refreshThread;
    private JTabbedPane mainTabbedPane = new JTabbedPane() ;
    private SearchInspectorPanel searchPanel;
    private CPESimulator simulator;
    private WorldDisplayPanel zoneDisplayPanel;
    private boolean isStarted;
    private MetricsPanel metricsPanel;
    private JCheckBoxMenuItem miDebugPlanningNodes;

    public VGFrame( ReferenceWorldState ws, CPESimulator simulator ) {
        super( "CPE Society Simulator" ) ;
        this.simulator = simulator ;
        JMenuBar menuBar = new JMenuBar( ) ;
        setJMenuBar( menuBar );

        JMenu fileMenu = new JMenu( "File" ) ;
        menuBar.add( fileMenu );
        JMenuItem miConfigureScenario = new JMenuItem( "Configure scenario" ) ;
        fileMenu.add( miConfigureScenario ) ;
        miConfigureScenario.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doConfigureScenario() ;
            }
        });

        JMenuItem miConfigureTargets = new JMenuItem( "Configure targets" ) ;
        fileMenu.add( miConfigureTargets ) ;

        JMenuItem miLoadModelParameters = new JMenuItem( "Load model parameters..." ) ;
        fileMenu.add( miLoadModelParameters ) ;
        miLoadModelParameters.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doLoadModelParameters();
            }
        });

        JMenuItem miSaveModelParameters = new JMenuItem( "Save model parameters..." ) ;
        fileMenu.add( miSaveModelParameters ) ;
        miSaveModelParameters.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doSaveModelParameters( e ) ;
            }
        });

        JMenu runMenu = new JMenu( "Run" ) ;
        menuBar.add( runMenu ) ;

        JMenuItem miStep = new JMenuItem( "Step" ) ;
        runMenu.add( miStep ) ;
        JMenuItem miRun = new JMenuItem( "Run" ) ;
        runMenu.add( miRun ) ;
        JMenuItem miStop = new JMenuItem( "Stop" ) ;
        runMenu.add( miStop ) ;

        JMenu debugMenu = new JMenu( "Debug" ) ;
        menuBar.add( debugMenu ) ;
        JMenuItem miConfigureZoneTest;
        miConfigureZoneTest = new JMenuItem( "Zone test" ) ;
        debugMenu.add( miConfigureZoneTest ) ;
        miDebugPlanningNodes = new JCheckBoxMenuItem( "Print BN planning nodes" ) ;
        debugMenu.add( miDebugPlanningNodes ) ;

        miConfigureTargets.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                 doConfigure() ;
            }
        });

        miStep.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doStep() ;
            }
        });

        miRun.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doRun() ;
            }
        });

        miStop.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doStop() ;
            }
        });

        miConfigureZoneTest.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doConfigureZoneTest() ;
            }
        });

        miDebugPlanningNodes.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doPrintPlanningNodes() ;
            }
        });

        setSize( 800, 600 ) ;
        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( mainTabbedPane, BorderLayout.CENTER ) ;
        mainTabbedPane.addTab( "Display", displayPanel = new WorldDisplayPanel( ws ) );
        mainTabbedPane.addTab( "Zones", zoneDisplayPanel = new WorldDisplayPanel( simulator.getZoneWorld() ) ) ;
        mainTabbedPane.addTab( "Inspector", searchPanel = new SearchInspectorPanel( this ) );
        mainTabbedPane.addTab( "Metrics", metricsPanel = new MetricsPanel( ws ) );

        Timer timer = new Timer( 2000, refreshTitleBar);
        timer.setRepeats( true );
        timer.start(); ;
    }

    ActionListener refreshTitleBar = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            if ( displayPanel != null) {
                displayPanel.repaint();
                WorldState ws = simulator.getWorldState() ;
                if ( ws != null ) {
                    setTitle( "CPE Society Display [time=" +
                        ( displayPanel.getWorldState().getTime() *
                        VGWorldConstants.SECONDS_PER_MILLISECOND ) +
                        ", score=" + ws.getScore() + "]" );
                }
            }

        }
    } ;


    private void doConfigureScenario()
    {
        simulator.configureDefaultScenario();
        displayPanel.setWorldState( simulator.getWorldState() );
        metricsPanel.setWorldState( simulator.getWorldState() );
        zoneDisplayPanel.setWorldState( simulator.getZoneWorld() );
    }

    private void doSaveModelParameters(ActionEvent e)
    {
        JFileChooser fc = new JFileChooser() ;
        fc.showSaveDialog( this ) ;
        File f = fc.getSelectedFile() ;
        if ( f != null ) {
            FileOutputStream fos = null ;
            try
            {
                fos = new FileOutputStream(f);
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            VGWorldConstants.saveParameterValues( fos );
        }
    }

    private void doLoadModelParameters() {
        JFileChooser fc = new JFileChooser() ;
        fc.showOpenDialog( this ) ;
        File f = fc.getSelectedFile() ;
        if ( f != null ) {
            FileInputStream fis = null ;
            try
            {
                fis = new FileInputStream(f);
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;
            try
            {
                DocumentBuilder builder = factory.newDocumentBuilder() ;
                Document doc =builder.parse( fis ) ;
                VGWorldConstants.setParameterValues( doc );
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        VGWorldConstants.printParameters( new PrintWriter( System.out ) );
    }

    private void doConfigureZoneTest()
    {
        simulator.configureZoneTestScenario();
        displayPanel.setWorldState( simulator.getWorldState() );
        zoneDisplayPanel.setWorldState( simulator.getZoneWorld() );
        metricsPanel.setWorldState( simulator.getWorldState() );
    }

    private void doPrintPlanningNodes()
    {
        if ( miDebugPlanningNodes.isSelected() ) {

            JFileChooser fc = new JFileChooser( );
            int value = fc.showSaveDialog( this ) ;
            if ( value != JFileChooser.APPROVE_OPTION ) {
                return ;
            }

            File f = fc.getSelectedFile() ;
            if ( f == null ) {
                return ;
            }

            simulator.setDebugDumpPlanNodes( true );
            simulator.setDebugPlanFile( f );
        }
        else {
            simulator.setDebugDumpPlanNodes( false );
        }
    }

    private void doConfigure()
    {
        if ( isStarted ) {
            return ;
        }
        if ( !simulator.isRunning() ) {
            simulator.doPopulateTargets();
        }
    }

    private void doStop()
    {
        if ( simulator.isRunning() ) {
            simulator.stop() ;
        }
    }

    public void doRun()
    {
        isStarted = true ;
        if ( !simulator.isRunning() ) {
            simulator.run();
        }
    }

    private void doStep()
    {
        isStarted = true ;
        if ( !isRunning ) {
            simulator.step();
        }
    }

    public class RefreshThread extends Thread {

        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                if ( displayPanel != null) {
                    displayPanel.repaint();
                    WorldState ws = displayPanel.getWorldState() ;
                    setTitle( "CPE Society Display [time=" +
                            ( displayPanel.getWorldState().getTime() *
                            VGWorldConstants.SECONDS_PER_MILLISECOND ) +
                            ", score=" + ws.getScore() + "]" );
                }
            }
        }
    }

    class WorldRunThread extends Thread {
        public void run()
        {
            while ( true ) {
                if ( isRunning | isStepping ) {
                    simulator.step();
                    isStepping = false ;
                }
                else {
                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    boolean isRunning = false, isStepping = false;

    WorldRunThread thread = new WorldRunThread() ;

    protected WorldDisplayPanel displayPanel ;
}
