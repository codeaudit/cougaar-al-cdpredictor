/**
 * User: wpeng
 * Date: Mar 26, 2003
 * Time: 5:44:03 PM
 */
package org.cougaar.cpe.unittests;

import org.cougaar.cpe.ui.WorldDisplayPanel;
import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class VGFrame extends JFrame {
    private RefreshThread refreshThread;
    private JTabbedPane mainTabbedPane = new JTabbedPane() ;
    private SearchInspectorPanel searchPanel;
    private CPESimulator simulator;
    private WorldDisplayPanel zoneDisplayPanel;
    private boolean isStarted;

    public VGFrame( WorldState ws, CPESimulator simulator ) {
        super( "CPE Society Simulator" ) ;
        this.simulator = simulator ;
        JMenuBar menuBar = new JMenuBar( ) ;
        setJMenuBar( menuBar );

        JMenu fileMenu = new JMenu( "File" ) ;
        menuBar.add( fileMenu );
        JMenuItem miConfigureTargets = new JMenuItem( "Configure" ) ;
        fileMenu.add( miConfigureTargets ) ;

        JMenu runMenu = new JMenu( "Run" ) ;
        menuBar.add( runMenu ) ;

        JMenuItem miStep = new JMenuItem( "Step" ) ;
        runMenu.add( miStep ) ;
        JMenuItem miRun = new JMenuItem( "Run" ) ;
        runMenu.add( miRun ) ;
        JMenuItem miStop = new JMenuItem( "Stop" ) ;
        runMenu.add( miStop ) ;

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

        setSize( 800, 600 ) ;
        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( mainTabbedPane, BorderLayout.CENTER ) ;
        mainTabbedPane.addTab( "Display", displayPanel = new WorldDisplayPanel( ws ) );
        mainTabbedPane.addTab( "Zones", zoneDisplayPanel = new WorldDisplayPanel( simulator.getZoneWorld() ) ) ;
        mainTabbedPane.addTab( "Inspector", searchPanel = new SearchInspectorPanel( this ) );

        refreshThread = new RefreshThread();
        refreshThread.start();
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
