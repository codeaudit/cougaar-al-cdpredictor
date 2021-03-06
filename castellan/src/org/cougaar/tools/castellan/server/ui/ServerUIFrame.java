package org.cougaar.tools.castellan.server.ui;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.server.* ;
import org.cougaar.tools.castellan.planlog.* ;
import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.alf.AgentLoadObserver;
import org.cougaar.tools.alf.AgentWorkflowLogPlanBuilder;
import org.cougaar.tools.alf.SocietyDesc;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.*;
import java.util.* ;
import java.io.* ;
import att.grappa.*;

/**
 *
 * @author  wpeng
 * @version
 */
public class ServerUIFrame extends javax.swing.JFrame {

    /** Creates new ServerUIFrame */
    public ServerUIFrame( ServerApp app ) {
        this.app = app ;
        setSize( 640, 480 ) ;
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                doQuit() ;
            }
        } ) ;

        makeMenus() ;
        setTitle( "Castellan Server" ) ;

        makeWindows() ;
    }

    public void printMessage( String message ) {
        messagePanel.append( message ) ;
    }

    protected void doEditSettings() {
        EditSettingsDialog esd = new EditSettingsDialog( this, app ) ;
        esd.setVisible( true ) ;
    }

    protected void makeWindows() {
        mainSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true ) ;
        mainSplitPane.setDividerLocation( 300 );
        getContentPane().setLayout( new BorderLayout() ) ;
        getContentPane().add( mainSplitPane, BorderLayout.CENTER ) ;
        mainSplitPane.setTopComponent( mainPanel = new MainPanel( app ) ) ;
        messagePanel = new MessagePanel() ;
        mainSplitPane.setBottomComponent( messagePanel ) ;
        mainSplitPane.setResizeWeight( 1.0 ) ;
    }

    protected void makeMenus() {
        JMenuBar menuBar = new JMenuBar() ;
        setJMenuBar( menuBar ) ;
        menuBar.add( fileMenu = new JMenu( "File" ) ) ;
        fileMenu.setMnemonic( 'F' ) ;
        fileMenu.add( miConnect = new JMenuItem( "Connect" ) ) ;
        fileMenu.add( miDisconnect = new JMenuItem( "Disconnect" ) ) ;
        fileMenu.add( new JSeparator() ) ;

        fileMenu.add( miOpenLog = new JMenuItem( "Open log..." ) ) ;
        fileMenu.add( miCreateNewLog = new JMenuItem( "New log..." ) ) ;
        fileMenu.add( miCloseLog = new JMenuItem( "Close log..." ) ) ;
        fileMenu.add( new JSeparator() ) ;
        fileMenu.add( miSettings = new JMenuItem( "Settings..." ) ) ;
        fileMenu.add( miSaveSettings = new JMenuItem( "Save settings" ) ) ;
        fileMenu.add( new JSeparator() ) ;
        fileMenu.add( miQuit = new JMenuItem( "Quit" ) ) ;

        analyzeMenu = new JMenu( "Analyze" ) ;
        menuBar.add( analyzeMenu ) ;
        analyzeMenu.setMnemonic( 'A' ) ;
        analyzeMenu.add( miGraphAnalysis = new JMenuItem( "Plan analysis..." ) ) ;
        analyzeMenu.add( miAggregateGraphAnalysis = new JMenuItem( "Aggregate analysis..." ) ) ;
        analyzeMenu.add( miAssetDependencyAnalysis = new JMenuItem( "Asset dependency analysis..." ) ) ;
        analyzeMenu.add( miWorkflowAnalysis = new JMenuItem( "Workflow analysis..." ) ) ;
        analyzeMenu.add( miShowEventLogTable = new JMenuItem( "Show event log table" ) ) ;

        helpMenu = new JMenu( "Help" ) ;
        menuBar.add( helpMenu ) ;
        helpMenu.setMnemonic( 'H' ) ;
        helpMenu.add( miAbout = new JMenuItem( "About" ) ) ;

        miQuit.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
               doQuit() ;
            }
        } ) ;

        miSettings.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doEditSettings() ;
            }
        } ) ;

        miSaveSettings.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doSaveSettings() ;
            }
        } ) ;

        miOpenLog.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doOpenLog() ;
            }
        } );

        miCreateNewLog.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doCreateNewLog() ;
            }
        } );

        miCloseLog.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doCloseLog() ;
            }
        } ) ;

        miConnect.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doConnect() ;
            }
        } ) ;

        miDisconnect.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e )  {
                doDisconnect() ;
            }
        } ) ;

        miGraphAnalysis.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doGraphAnalysis() ;
            }
        } ) ;

        miAggregateGraphAnalysis.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doAggregateGraphAnalysis() ;
            }
        } ) ;

        miAssetDependencyAnalysis.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doAssetAnalysis() ;
            }
        } ) ;

        miWorkflowAnalysis.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doWorkflowAnalysis();
            }
        } );

        miShowEventLogTable.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doShowEventLogTable();
            }
        });

        miAbout.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doShowAbout() ;
            }
        } ) ;

    }

    protected void doSaveSettings() {
        printMessage( "Saving settings..." ) ;

        String chome = System.getProperty( "castellanhome" ) ;
        if ( chome == null ) {
            JOptionPane.showMessageDialog( this,  "No castellanhome property set, cannot save settings." ) ;
            return ;
        }
        app.saveSettings() ;
        printMessage( "Done." ) ;
    }

    protected void doClear() {
        app.println( "Clearing event log." ) ;
        app.getEventLog().clear() ;
    }

    public static final String OK = "OK" ;
    public static final String CANCEL = "Cancel" ;

    protected void doCreateNewLog() {
        if ( app.getEventLog() != null ) {
            int confirm = JOptionPane.showConfirmDialog( this, "Close existing log?", "Open Log",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE ) ;
            if ( confirm == JOptionPane.NO_OPTION ) {
                return ;
            }
            app.getEventLog().close();
            app.setEventLog( null );
            mainPanel.refreshVisible();
        }

        ButtonGroup group = new ButtonGroup() ;
        JRadioButton rbLogToMemory = new JRadioButton( "Log to memory" ) ;
        JRadioButton rbLogToDatabase = new JRadioButton( "Log to database" ) ;
        group.add( rbLogToMemory ) ;
        group.add( rbLogToDatabase ) ;
        rbLogToMemory.setSelected( true );
        int result = JOptionPane.showOptionDialog( this, new Object[] { rbLogToMemory, rbLogToDatabase },
           "New log", JOptionPane.OK_CANCEL_OPTION,
           JOptionPane.QUESTION_MESSAGE, null,
           new String[] { OK, CANCEL },
           OK
           ) ;

        if ( result == 1 ) {
            return ;
        }

        // Make a new log.
        if ( rbLogToMemory.isSelected() ) {
            app.println( "Creating new event log in memory." );
            EventLog eventLog = new InMemoryEventLog() ;
            app.setEventLog( eventLog ) ;
        }
        else if ( rbLogToDatabase.isSelected() ) {
            String s = JOptionPane.showInputDialog( this, "Enter database name", "Create new log",
                JOptionPane.QUESTION_MESSAGE ) ;
            if ( s == null || s.length() == 0 ) {
                return ;
            }

            try {
                app.println( "Creating new database log \"" + s + '"'  );
                Properties props = new Properties() ;
                props.put( "dbpath", ServerApp.instance().getDbPath() ) ;

                PersistentEventLog dbEventLog = new PersistentEventLog(s, props) ;
                app.setEventLog( dbEventLog ) ;
                app.println( "Done." ) ;
            }
            catch ( Exception e ) {
                app.println( e.toString()  ) ;
                JOptionPane.showMessageDialog( this, "Error creating database","Create new log", JOptionPane.ERROR_MESSAGE ) ;
            }
            finally {

            }
        }

        mainPanel.refreshVisible() ;
    }

    protected void doCloseLog() {
        if ( app.getEventLog() != null ) {
            app.println( "Closing log " + app.getEventLog() );
            EventLog log = app.getEventLog() ;
            log.close();
            app.setEventLog( null ) ;
            mainPanel.refreshVisible();
        }
    }

    protected void doOpenLog() {
        ArrayList list = null ;

        try {
            list = PersistentEventLog.getValidDatabases( ServerApp.instance().getDbPath(),
                ServerApp.instance().getUser(), ServerApp.instance().getPassword() ) ;
        }
        catch ( Exception e ) {
            JOptionPane.showMessageDialog( this, e.toString(), "Open Log", JOptionPane.ERROR_MESSAGE );
            return ;
        }

        JComboBox cb = new JComboBox();
        for (int i=0;i<list.size();i++) {
            cb.addItem( list.get(i));
        }

        Object[] messages = new Object[3];
        messages[0] = cb ;

        Object[] options = new Object[2] ;
        options[0] = "OK" ;
        options[1] = "Cancel" ;

        int result = JOptionPane.showOptionDialog( this, messages, "Select database",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null ) ;
        if ( result == 1 ) {
            return ;
        }

        String dbname = ( String ) cb.getSelectedItem() ;

        try {
            Properties props = new Properties() ;
            props.put( "dbpath", ServerApp.instance().getDbPath() ) ;

            PersistentEventLog dbEventLog = new PersistentEventLog(dbname,props,true) ;
            app.setEventLog( dbEventLog ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace();
            app.println( e.toString()  ) ;
            JOptionPane.showMessageDialog( this, "Error opening database","Open new log", JOptionPane.ERROR_MESSAGE ) ;
        }
    }

    protected void doQuit() {
        app.getServerMessageTransport().stop() ;
        doCloseLog() ;
        System.exit( 0 ) ;
    }

    protected void doConnect() {
        app.println( "Starting server message transport..." ) ;
        app.getServerMessageTransport().start() ;
    }

    protected void doDisconnect() {
        app.println( "Disconnecting from all clients..." ) ;
        app.getServerMessageTransport().stop() ;
        app.println( "Done." ) ;
    }

    protected void doShowEventLogTable() {
        if ( app.getEventLog() != null ) {
            EventPDUTableComponent comp = new EventPDUTableComponent( app.getEventLog() ) ;
            JFrame frame = new JFrame( "Events" ) ;
            frame.setSize( 800, 600 ) ;
            frame.getContentPane().setLayout( new BorderLayout() ) ;
            frame.getContentPane().add( comp, BorderLayout.CENTER ) ;
            frame.setVisible( true ) ;
        }
        else {
            JOptionPane.showMessageDialog( this, "No event log is loaded.", "Show Event Log Table",
                 JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Does workflow analysis on the plan.
     */
    protected void doWorkflowAnalysis() {
        if (! checkGraphSettings() ) {
             return ;
        }

        PlanLogDatabase pld = new PlanLogDatabase() ;
        AgentWorkflowLogPlanBuilder builder = new AgentWorkflowLogPlanBuilder( pld ) ;
        SocietyDesc sd = new SocietyDesc() ;
        AgentLoadObserver observer = new AgentLoadObserver( pld, sd ) ;
        EventLog log = app.getEventLog() ;

        ArrayList dependencies = new ArrayList() ;
        ArrayList arPDUs = new ArrayList( log.getNumEvents() / 20 ) ;
        for (Iterator iter = log.getEvents(); iter.hasNext(); ) {
            PDU pdu = ( PDU ) iter.next() ;
            builder.processPDU( pdu );

            // Process DeclarePDUs.
            //
            if ( pdu instanceof DeclarePDU ) {
                observer.processPDU( pdu, dependencies ) ;
            }
            // Track all allocation results.
            else if ( pdu instanceof AllocationResultPDU ) {
                arPDUs.add( pdu ) ;
            }
        }

        // Second pass for allocation results.
        for (int i=0;i<arPDUs.size();i++) {
            builder.processARPDU( ( AllocationResultPDU ) arPDUs.get(i) );
        }

        System.out.println("BUILDING WORKFLOW GRAPH...");
        try {
            observer.buildGraph();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        System.out.println("\n=============================\nSOCIETY\n " + sd.toString() );

/*
        System.out.println("\nEntry points:");
        for (Iterator iter =observer.getEntryPoints();iter.hasNext(); ) {
            System.out.println( iter.next() );
        }

        System.out.println("\nExit points:");
        for (Iterator iter =observer.getExitPoints();iter.hasNext(); ) {
            System.out.println( iter.next() );
        }
*/

        System.out.println("\n============================\nBoundary Task Aggregates:");
        for (Iterator iter=observer.getAggregates();iter.hasNext(); ) {
            System.out.println( iter.next() );
        }

        File tempFile = app.getTempFile( ".dot" ) ;
        BoundaryAggregateLayout bal = new BoundaryAggregateLayout() ;
        int count = bal.layoutClusteredGraph( tempFile, observer ) ;

        if ( count == 0 ) {
            JOptionPane.showMessageDialog( this, "No aggregate nodes processed.", "Workflow graph", JOptionPane.ERROR_MESSAGE ) ;
        }
        else {
            JOptionPane.showMessageDialog( this, "A total of " + count + " nodes were processed.", "Workflow graph",
                JOptionPane.INFORMATION_MESSAGE ) ;
        }

        Graph g = GraphLayout.doLayout( app.getDotPath(), tempFile ) ;
        GraphFrame gf = new WorkflowGraphFrame( observer, "Workflow", g ) ;
        gf.setVisible( true );
    }

    protected void doAggregateGraphAnalysis() {
        if (! checkGraphSettings() ) {
            return ;
        }

        SelectTimeDialog std = new SelectTimeDialog( app.getEventLog().getFirstEventTime(), app.getEventLog().getLastEventTime() ) ;
        std.setTitle( "Select Time Range" ) ;
        std.setVisible( true ) ;

        if ( std.getResult() == JOptionPane.CANCEL_OPTION ) {
            return ;
        }

        // Perform analysis.
        LogPlanBuilder builder = new LogPlanBuilder( new PlanLogDatabase() ) ;

        EventLog log = app.getEventLog() ;
        long startTime = std.getSelectedStartTime() ;
        long endTime = std.getSelectedEndTime() ;

        int totalEvents = log.getNumEvents( startTime, endTime ) ;
        int uniqueUIDs = log.getNumUIDs( startTime, endTime ) ;

        int result = JOptionPane.showConfirmDialog( this, "A total of " + totalEvents + " messages and " + uniqueUIDs + " unique UIDs were found. Continue? ",
            "Task graph analysis", JOptionPane.YES_NO_OPTION );
        if ( result == JOptionPane.NO_OPTION ) {
            return ;
        }

        Iterator iter1 = log.getAssetEvents( 0, startTime ) ;
        long assetCount = 0 ;
        while ( iter1.hasNext() ) {
            assetCount++ ;
            PDU pdu = ( PDU ) iter1.next() ;
            builder.processPDU( pdu ) ;
        }
        ServerApp.instance().println( "There are a total of " + assetCount + " events before the start period." ) ;

        int mcount = 0 ;
        Iterator iter2 = log.getEvents( startTime, endTime ) ;
        while ( iter2.hasNext() ) {
            PDU pdu = ( PDU ) iter2.next() ;
            mcount++ ;
            builder.processPDU( pdu ) ;
        }
        ServerApp.instance().println( "Processed " + mcount + " events between " + PDU.formatTimeAndDate( startTime ) +  " and " +
           PDU.formatTimeAndDate( endTime ) + " for elapsed " + PDU.formatTime( endTime - startTime )  ) ;

        AggregateTaskBuilder abuilder = new AggregateTaskBuilder( builder ) ;
        abuilder.buildGraph() ;


        File tempFile = app.getTempFile( ".dot" ) ;
        GraphBuilderLayout gbl = new GraphBuilderLayout( app ) ;
        int count = gbl.layoutAggregateGraph( tempFile, abuilder ) ;
        if ( count == 0 ) {
            JOptionPane.showMessageDialog( this, "No aggregate nodes processed.", "Aggregate plan graph", JOptionPane.ERROR_MESSAGE ) ;
        }
        else {
            JOptionPane.showMessageDialog( this, "A total of " + count + " aggregate nodes were processed.", "Aggregate plan graph",
                JOptionPane.INFORMATION_MESSAGE ) ;
        }

        Graph graph = GraphLayout.doLayout( app.getDotPath(), tempFile ) ; // Convert dot file to graph
	    GraphFrame gf = new GraphFrame( "Task Graph", graph ) ;
        gf.setSize( 800, 600 ) ;
        gf.setVisible( true ) ;

    }

    protected void doAssetAnalysis() {
        if (! checkGraphSettings() ) {
            return ;
        }

        SelectTimeDialog std = new SelectTimeDialog( app.getEventLog().getFirstEventTime(), app.getEventLog().getLastEventTime() ) ;
        std.setTitle( "Select time range" ) ;
        std.setVisible( true ) ;

        if ( std.getResult() == JOptionPane.CANCEL_OPTION ) {
            return ;
        }
        // Perform analysis.
        LogPlanBuilder builder = new LogPlanBuilder( new PlanLogDatabase() ) ;

        EventLog log = app.getEventLog() ;
        long startTime = std.getSelectedStartTime() ;
        long endTime = std.getSelectedEndTime() ;

        int totalEvents = log.getNumEvents( startTime, endTime ) ;
        int uniqueUIDs = log.getNumUIDs( startTime, endTime ) ;

        int result = JOptionPane.showConfirmDialog( this, "A total of " + totalEvents + " messages and " + uniqueUIDs + " unique UIDs were found. Continue? ",
            "Task graph analysis", JOptionPane.YES_NO_OPTION );
        if ( result == JOptionPane.NO_OPTION ) {
            return ;
        }

        Iterator iter1 = log.getAssetEvents( 0, startTime ) ;
        long assetCount = 0 ;
        while ( iter1.hasNext() ) {
          assetCount++ ;
          PDU pdu = ( PDU ) iter1.next() ;
          builder.processPDU( pdu ) ;
        }
        ServerApp.instance().println( "There are a total of " + assetCount + " events before the start period." ) ;

        Iterator iter2 = log.getEvents( startTime, endTime ) ;
        int mcount = 0 ;
        while ( iter2.hasNext() ) {
            PDU pdu = ( PDU ) iter2.next() ;
            mcount++ ;
            builder.processPDU( pdu ) ;
        }

        AssetDependencyBuilder adb = new AssetDependencyBuilder( builder ) ;
        adb.buildGraph() ;

        File tempFile = app.getTempFile( ".dot" ) ;

        GraphBuilderLayout gbl = new GraphBuilderLayout( app ) ;
        int count = gbl.layoutAssetDependencyGraph( tempFile, builder ) ;
        if ( count == 0 ) {
            JOptionPane.showMessageDialog( this, "No asset nodes processed.", "Task Graph", JOptionPane.ERROR_MESSAGE ) ;
        }
        else {
            JOptionPane.showMessageDialog( this, "A total of " + count + " asset nodes processed.", "Task Graph",
                JOptionPane.INFORMATION_MESSAGE ) ;
        }
        Graph graph = GraphLayout.doLayout( app.getDotPath(), tempFile ) ; // Convert dot file to graph
	    GraphFrame gf = new GraphFrame( "Task Graph", graph ) ;
        gf.setSize( 800, 600 ) ;
        gf.setVisible( true ) ;
    }

    protected boolean checkGraphSettings() {
        if ( app.getDotPath() == null ) {
            JOptionPane.showMessageDialog( this, "Dot executable path is not set.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        File f = new File( app.getDotPath() ) ;

        if ( !f.exists() ) {
            JOptionPane.showMessageDialog( this, "Dot executable path \"" + f + "\" does not exist.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        if ( f.isDirectory() ) {
            JOptionPane.showMessageDialog( this, "Dot executable path \"" + f + "\" is not a valid file.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        if ( app.getTempPath() == null ) {
            JOptionPane.showMessageDialog( this, "Temp path does is not set.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        f = new File( app.getTempPath() ) ;

        if ( !f.exists() || !f.isDirectory() || !f.canWrite() ) {
            JOptionPane.showMessageDialog( this, "Temp path  \"" + f + "\" is invalid.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        if ( app.getEventLog() == null ) {
            JOptionPane.showMessageDialog( this, "No event log is open.", "Graph Analysis",
                JOptionPane.ERROR_MESSAGE ) ;
            return false ;
        }

        return true ;
    }

    protected void doGraphAnalysis() {
        if (! checkGraphSettings() ) {
            return ;
        }

        SelectTimeDialog std = new SelectTimeDialog( app.getEventLog().getFirstEventTime(), app.getEventLog().getLastEventTime() ) ;
        std.setTitle( "Select time range" ) ;
        std.setVisible( true ) ;

        if ( std.getResult() == JOptionPane.CANCEL_OPTION ) {
            return ;
        }


        // Perform analysis.
        LogPlanBuilder builder = new LogPlanBuilder( new PlanLogDatabase() ) ;

        EventLog log = app.getEventLog() ;
        long startTime = std.getSelectedStartTime() ;
        long endTime = std.getSelectedEndTime() ;

        int totalEvents = log.getNumEvents( startTime, endTime ) ;
        int uniqueUIDs = log.getNumUIDs( startTime, endTime ) ;

        int result = JOptionPane.showConfirmDialog( this, "A total of " + totalEvents + " messages and "
            + uniqueUIDs + " unique UIDs were found. Continue? ",
            "Task graph analysis", JOptionPane.YES_NO_OPTION );
        if ( result == JOptionPane.NO_OPTION ) {
            return ;
        }

        Iterator iter1 = log.getAssetEvents( 0, startTime ) ;
        long assetCount = 0 ;
        while ( iter1.hasNext() ) {
          assetCount++ ;
          PDU pdu = ( PDU ) iter1.next() ;
          builder.processPDU( pdu ) ;
        }
        // ServerApp.instance().println( "There are a total of " + assetCount + " events before the start period." ) ;

        int mcount = 0 ;
        Iterator iter2 = log.getEvents( startTime, endTime ) ;
        while ( iter2.hasNext() ) {
            PDU pdu = ( PDU ) iter2.next() ;
            mcount++ ;
            builder.processPDU( pdu ) ;
        }
        ServerApp.instance().println( "Processed " + mcount + " events between " + PDU.formatTimeAndDate( startTime ) +  " and " +
           PDU.formatTimeAndDate( endTime ) + " for elapsed " + PDU.formatTime( endTime - startTime )  ) ;

        File tempFile = app.getTempFile( ".dot" ) ;
        GraphBuilderLayout gbl = new GraphBuilderLayout( app ) ;
        int count = gbl.layoutTaskGraph( tempFile, builder ) ;  // Layout to temporary dot file
        if ( count == 0 ) {
            JOptionPane.showMessageDialog( this, "No graph nodes processed!", "Task Graph", JOptionPane.ERROR_MESSAGE ) ;
        }
        else {
            JOptionPane.showMessageDialog( this, "A total of " + count + " graph nodes were processed.", "Task Graph",
                JOptionPane.INFORMATION_MESSAGE ) ;
        }

        Graph graph = GraphLayout.doLayout( app.getDotPath(), tempFile ) ; // Convert dot file to graph
        GraphFrame gf = new GraphFrame( "Task Graph", graph ) ;
        gf.setSize( 800, 600 ) ;
        gf.setVisible( true ) ;
    }

    protected void doShowAbout() {
        AboutDialog ad = new AboutDialog( this ) ;
        ad.setVisible( true );
    }

    ServerApp app ;
    //SerializedMessageLog sml ;
    JMenu fileMenu, analyzeMenu, helpMenu ;
    JMenuItem miConnect, miDisconnect, miClear;
    JMenuItem miLoadMessageLog, miStartLogging, miStopLogging, miSettings, miSaveSettings, miQuit ;
    JMenuItem miGraphAnalysis, miAggregateGraphAnalysis, miAssetDependencyAnalysis, miAgentPluginAnalysis, miWorkflowAnalysis,
              miShowEventLogTable ;
    JMenuItem miOpenLog, miCreateNewLog, miCloseLog ;
    JMenuItem miAbout ;
    JDesktopPane desktop ;
    JSplitPane mainSplitPane, hSplitPane ;
    JPanel emptyPanel = new JPanel() ;
    MessagePanel messagePanel ;
    MainPanel mainPanel ;
    File logFile ;
}
