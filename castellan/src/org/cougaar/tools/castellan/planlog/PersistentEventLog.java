/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/planlog/PersistentEventLog.java,v 1.1 2002-05-22 23:10:13 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the license you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: PersistentEventLog.java,v $
* Revision 1.1  2002-05-22 23:10:13  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.planlog;

import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.ServerApp;

import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;
import java.util.ArrayList;
import java.sql.*;


/**
 * Persistent event log for MySQL databases.
 */

public class PersistentEventLog implements EventLog
{
    // ATTRIBUTES
    String name ;
    Properties props ;
    EventPDUTable myEventPDUTable;
    ExecutionPDUTable myExecutionPDUTable;

    /**
     * Connection to database.  Should probably use connection pool in deployed version.
     */
    Connection conn ;

    // METHODS
    /**
     * Create a new database.
     *
     * @param name Name of database.  If it already exists, a runtime exception is thrown.
     */
    public PersistentEventLog( String name ) {
        this( name, null ) ;
    }

    public PersistentEventLog( String name, Properties props ) {
        this.name = name ;
        if ( name == null ) {
            throw new IllegalArgumentException( "Name of database input argument : String name,  CANNOT be null." ) ;
        }
        if ( props != null ) {
            this.props = props ;
        }
        connect() ;

        if ( exists( name ) ) {
            dropDatabase() ;
        }

        // Go ahead and create the database
        createDatabase();
    }

    public String getName() {
        return name;
    }

    public static ArrayList getValidDatabases( String dbPath, String user, String password ) {
        try {
            Class.forName( "org.gjt.mm.mysql.Driver" );
        }
        catch ( java.lang.ClassNotFoundException e ) {
            throw new RuntimeException( "Driver not found for MySQL database." ) ;
        }

        Connection conn = null ;
        try {
            conn = DriverManager.getConnection( dbPath, user, password  );
        }
        catch ( SQLException ex ){
            Table.printSQLException( ex );
            throw new RuntimeException( "PersistentEventLog:: Error connecting to database." );
        }

        return getValidDatabases( conn ) ;
    }

    protected static ArrayList getValidDatabases( Connection conn ) {

        ArrayList results = new ArrayList() ;
        try {
            Statement s = conn.createStatement() ;
            ResultSet set = s.executeQuery( "SHOW DATABASES" ) ;
            if ( set.isBeforeFirst() ) {
                set.next() ;
            }
            else {
                return results ;  // No databases exist.
            }

            ArrayList databases = new ArrayList() ;
            // Look for EVENTPDU and EXECUTIONPDU
            while ( !set.isAfterLast() ) {
                String dbName = set.getString(1 ) ;
                databases.add( dbName ) ;
                set.next() ;
            }

            for (int i=0;i<databases.size();i++) {
                String dbName = ( String ) databases.get(i) ;
                s.executeQuery( "USE " + dbName ) ;
                Statement s2 = conn.createStatement() ;
                ResultSet tableSet = s2.executeQuery( "SHOW TABLES" ) ;
                if ( !tableSet.isBeforeFirst() ) {
                    break ;
                }
                else {
                    tableSet.next() ;
                }

                boolean f1 = false , f2 = false ;
                while (!tableSet.isAfterLast() ) {
                    String tableName = tableSet.getString( 1 ) ;
                    if ( tableName.equals( "eventpdu" ) ) {
                        f1 = true ;
                    }
                    else if ( tableName.equals( "executionpdu" ) ) {
                        f2 = true ;
                    }
                    if ( f1 && f2 ) {
                        results.add( dbName ) ;
                        break ;
                    }
                    tableSet.next() ;
                }
            }

            return results ;

//            while ( !set.isAfterLast() ) {
//                String dbName = set.getString( 1 ) ;
//                System.out.println("Using " + dbName );
//                s.executeQuery( "USE " + dbName ) ;
//                Statement s2 = conn.createStatement() ;
//                ResultSet tableSet = s2.executeQuery( "SHOW TABLES" ) ;
//                if ( !tableSet.isBeforeFirst() ) {
//                    break ;
//                }
//                else {
//                    tableSet.next() ;
//                }
//
//                boolean f1 = false , f2 = false ;
//                while (!tableSet.isAfterLast() ) {
//                    String tableName = tableSet.getString( 1 ) ;
//                    if ( tableName.equals( "eventpdu" ) ) {
//                        f1 = true ;
//                    }
//                    else if ( tableName.equals( "executionpdu" ) ) {
//                        f2 = true ;
//                    }
//                    if ( f1 && f2 ) {
//                        results.add( dbName ) ;
//                        break ;
//                    }
//                    tableSet.next() ;
//                }
//                set.next() ;
//            }
//            return results ;
        }
        catch ( SQLException e ) {
            Table.printSQLException( e );
            e.printStackTrace();
            throw new RuntimeException( "Error connecting to database." ) ;
        }

    }

    protected void finalize() throws Throwable
    {
        super.finalize();
        if ( conn != null && !conn.isClosed() ) {
            try {
                conn.close();
            }
            catch ( Exception e ) {
                // Die silently
            }
        }
    }

    private void connect() {
        try
        {
            Class.forName( "org.gjt.mm.mysql.Driver" );

        }
        catch ( java.lang.ClassNotFoundException e )
        {
            throw new RuntimeException( "Driver not found for MySQL database." ) ;
        }

        String dbPath = props.getProperty( "dbpath" ) ;

        if ( dbPath == null )
        {
            throw new RuntimeException( "PersistentEventLog:: dbPath variable not set. Could not establish connection to database." );
        }

        try
        {
            conn = DriverManager.getConnection( dbPath, props.getProperty("user"), props.getProperty("password") );
        }
        catch ( SQLException ex )
        {
            Table.printSQLException( ex );
            throw new RuntimeException( "PersistentEventLog:: Error connecting to database:" );
        }

    }

    private void disconnect() {
        if ( conn != null ) {
            try {
                conn.close();
            }
            catch ( SQLException e ) {
            }
            conn = null ;
        }
    }

    /**
     * Does this database exist?
     */
    private boolean exists( String name ) {
        try {
            Statement s = conn.createStatement() ;
            ResultSet set = s.executeQuery( "SHOW DATABASES" ) ;
            if ( set.isBeforeFirst() ) {
                set.next() ;
            }
            while ( !set.isAfterLast() ) {
                String dbName = set.getString( 1 ) ;
                if ( dbName.equals( name ) ) {
                    return true ;
                }
                // Advance the cursor.
                set.next() ;
            }
            return false ;
        }
        catch ( SQLException e ) {
            Table.printSQLException( e );
            throw new RuntimeException( "Could not access database." ) ;
        }
    }

    public void clear()
    {
       System.out.println( "Not implemented yet!" );

    }

    public void close()
    {
        try {
            if ( conn != null && !conn.isClosed() ) {
                conn.close();
            }
        }
        catch ( Exception e ) {
        }
    }

     private synchronized void createDatabase() {
        Statement stmt = null;
        try {
            stmt = conn.createStatement() ;
            stmt.executeQuery( "CREATE DATABASE " + name ) ;
            stmt.executeQuery( "USE " + name ) ;
        }
        catch ( SQLException e ) {
            Table.printSQLException( e );
            throw new RuntimeException( "Could not create database." ) ;
        }
        createTables( stmt );
    }

    private synchronized void dropDatabase() {
        Statement stmt = null;
        try {
            System.out.println("Dropping existing database.");
            stmt = conn.createStatement() ;
            stmt.executeQuery( "DROP DATABASE " + name ) ;
        }
        catch ( SQLException e ) {
            Table.printSQLException( e );
            throw new RuntimeException( "Could not drop existing database." ) ;
        }
    }

    private synchronized void createTables( Statement stmt ){
        // Instantiate myEventPDUTable
        myEventPDUTable = new EventPDUTable( conn );
        myEventPDUTable.createTable( stmt );

        // Instantiate myExecutionPDUTable
        myExecutionPDUTable = new ExecutionPDUTable( conn );
        myExecutionPDUTable.createTable( stmt );
    }

    public synchronized void add(PDU pdu)
    {
        int Type = 0;
        if ( pdu instanceof EventPDU ) {
            myEventPDUTable.add( (EventPDU)pdu );
        }
        else if( pdu instanceof ExecutionPDU ) {
            myExecutionPDUTable.add( (ExecutionPDU)pdu );
        }
    }

    public synchronized Iterator getAssetEvents()
    {
        return myEventPDUTable.getEvents( EventPDUInterface.TYPE_ASSET );
    }

    public synchronized Iterator getAssetEvents( long start, long end )
    {
        return myEventPDUTable.
          getEvents( EventPDUInterface.TYPE_ASSET, start, end );
    }

    public synchronized Iterator getEvents()
    {
        return myEventPDUTable.getEvents();
    }

    public synchronized Iterator getEvents(long start, long end)
    {
        return myEventPDUTable.getEvents( start, end );
    }

    public synchronized Iterator getEvents( UID uid )
    {
        return myEventPDUTable.getEvents( uid );
    }

    public synchronized Iterator getEvents( UID uid, long start, long end )
    {
        return myEventPDUTable.getEvents( uid, start, end );
    }

    public synchronized Iterator getExecutionsActiveOnly(long start, long end)
    {
        return myExecutionPDUTable.getExecutionsActiveOnly( start, end );
    }

    public synchronized Iterator getExecutionsActiveSometime(long start, long end)
    {
        return myExecutionPDUTable.getExecutionsActiveSometime( start, end );
    }

    public synchronized Iterator getExecutionsStarted(long start, long end)
    {
        return myExecutionPDUTable.getExecutionsStarted( start, end );
    }

    public synchronized Iterator getExecutionsStopped(long start, long end)
    {
        return myExecutionPDUTable.getExecutionsStopped( start, end );
    }

    public synchronized long getFirstEventTime()
    {
        return myEventPDUTable.getFirstEventTime();
    }

    public synchronized long getFirstExecutionTime()
    {
        return myExecutionPDUTable.getFirstExecutionTime();
    }

    public synchronized long getLastEventTime()
    {
        return myEventPDUTable.getLastEventTime();
    }

    public synchronized long getLastExecutionTime()
    {
        return myExecutionPDUTable.getLastExecutionTime();
    }

    public synchronized int getNumAssetEvents()
    {
        return myEventPDUTable.getNumEvents( EventPDUInterface.TYPE_ASSET );
    }

    public synchronized int getNumAssetEvents(long start, long end)
    {
        return myEventPDUTable.getNumEvents( EventPDUInterface.TYPE_ASSET, start, end  );
    }

    public synchronized int getNumEvents()
    {
        return myEventPDUTable.getNumEvents();
    }

    public synchronized int getNumEvents(long start, long end)
    {
        return myEventPDUTable.getNumEvents( start, end );
    }

    public synchronized int getNumExecutionsStarted(long start, long end)
    {
        return myExecutionPDUTable.getNumExecutionsStarted( start, end );
    }

    public synchronized int getNumExecutionsStopped(long start, long end)
    {
        return myExecutionPDUTable.getNumExecutionsStopped( start, end );
    }

    public synchronized int getNumUIDs()
    {
        return myEventPDUTable.getNumUIDs();
    }

    public synchronized int getNumUIDs(long start, long end )
    {
        return myEventPDUTable.getNumUIDs( start, end );
    }

    public static void main( String[] args ) {
        // Tester must populate props with own database settings


        ArrayList result = PersistentEventLog.getValidDatabases( "jdbc:mysql://localhost/", "sysdba",  "masterkey" ) ;

        for (int i=0;i<result.size();i++) {
            System.out.println( result.get(i) ) ;
        }
    }

    public static void main2( String[] args ) {
        Properties props = new Properties() ;
        props.put( "user", "cougaaruser" ) ;
        props.put( "password", "cougaarpass" ) ;
        props.put( "dbpath", "jdbc:mysql://localhost/" ) ;

        try {
            System.out.println("_______________TESTING PersistentEventLog_______________");
            PersistentEventLog log = new PersistentEventLog( "bbpelog", props ) ;

            // CREATE AND ADD PDUs TO DATABASE
            System.out.println("++++ ADDING FOLLOWING PDUs TO THE DATABASE:");
            PDU pdus[] = new PDU [13];
            int cnt = 0;
            // Create some TaskPDUs
            // public TaskPDU( SymbolPDU taskVerb, UIDPDU parentTaskUID, UIDPDU uid,
            //                 int action, long executionTime, long time )
            // public UIDStringPDU(String owner, long id)

            TaskPDU tPDU1 = new TaskPDU( new SymStringPDU("taskVerb1"), new UIDStringPDU("owner_1", 30),
               new UIDStringPDU("owner_11", 30), null, EventPDUInterface.ACTION_ADD, 50, 55555);
            if( tPDU1 != null ){
               System.out.println("TASK PDU " + cnt + ": " + tPDU1.toString() );
               pdus[cnt++] = tPDU1;

            }
            TaskPDU tPDU2 = new TaskPDU( new SymStringPDU("taskVerb2"), new UIDStringPDU("owner_2", 4),
               new UIDStringPDU("owner_22", 44), null, EventPDUInterface.ACTION_CHANGE, 30, 33333 );
            if( tPDU2 != null ){
               System.out.println("TASK PDU " + cnt + ": " + tPDU2.toString() );
               pdus[cnt++] = tPDU2;
            }
            TaskPDU tPDU3= new TaskPDU( new SymStringPDU("taskVerb3"), new UIDStringPDU("owner_3", 5),
               new UIDStringPDU("owner_33", 4), null, EventPDUInterface.ACTION_REMOVE, 10, 11111 );
            if( tPDU3 != null ){
               System.out.println("TASK PDU " + cnt + ": " + tPDU3.toString() );
               pdus[cnt++] = tPDU3;
            }

            // Create some ExecutionPDUs
            // public ExecutionPDU( String clusterIdentifier, String plugInName,
            //             int plugInHash, long seqNumber, long startTime, long stopTime )
            ExecutionPDU exPDU1 = new ExecutionPDU( "Cluster A", "Plugin A", 3,
                                                     543216, 0, 88000, 110000 );
            if( exPDU1 != null ){
               System.out.println("EXECUTION PDU " + cnt + ": " + exPDU1.toString() );
               pdus[cnt++] = exPDU1;
            }
            ExecutionPDU exPDU2 = new ExecutionPDU( "Cluster B", "Plugin A", 1,
                                                     432165, 0, 220000, 330000 );
            if( exPDU2 != null ){
               System.out.println("EXECUTION PDU " + cnt + ": " + exPDU2.toString() );
               pdus[cnt++] = exPDU2;
            }
            ExecutionPDU exPDU3 = new ExecutionPDU( "Cluster A", "Plugin B", 2,
                                                     321654, 0, 300000, 440000 );
            if( exPDU3 != null ){
               System.out.println("EXECUTION PDU " + cnt + ": " + exPDU3.toString() );
               pdus[cnt++] = exPDU3;
            }


            // Add PDUs to the database
            // public AssetPDU( SymbolPDU assetClass, String typeId, String newItemId,
            //                  String assetTypeNomenclature, UIDPDU uid, int action,
            //                  long executionTime, long time )
            AssetPDU asPDU1 = new AssetPDU( new SymStringPDU("assetClass1"), EventPDUInterface.STRING_ASSET,
                              "ItemId_1", "assetTypeNomenclature_1", new UIDStringPDU("owner_4", 2),
                              EventPDUInterface.ACTION_REMOVE, 20, 20000 );
            if( asPDU1 != null ){
               System.out.println("ASSET PDU " + cnt + ": " + asPDU1.toString() );
               pdus[cnt++] = asPDU1;
            }
            AssetPDU asPDU2 = new AssetPDU( new SymStringPDU("assetClass2"), EventPDUInterface.STRING_ASSET,
                              "ItemId_2", "assetTypeNomenclature_2", new UIDStringPDU("owner_5", 7),
                              EventPDUInterface.ACTION_ADD, 10, 100000 );
            if( asPDU2 != null ){
               System.out.println("ASSET PDU " + cnt + ": " + asPDU2.toString() );
               pdus[cnt++] = asPDU2;
            }
            AssetPDU asPDU3 = new AssetPDU( new SymStringPDU("assetClass3"), EventPDUInterface.STRING_ASSET,
                              "ItemId_3", "assetTypeNomenclature_3", new UIDStringPDU("owner_6", 1),
                              EventPDUInterface.ACTION_ADD, 30, 300000 );
            if( asPDU3 != null ){
               System.out.println("ASSET PDU " + cnt + ": " + asPDU3.toString() );
               pdus[cnt++] = asPDU3;
            }

            // Add ExpansionPDU
            // public ExpansionPDU( UIDPDU parentTask, UIDPDU[] tasks,
            // UIDPDU uid, int action, long executionTime, long time )

            UIDPDU thearray[] = { new UIDStringPDU("owner_2",  12), new UIDStringPDU("owner_13",  7)};

            ExpansionPDU expPDU1 = new ExpansionPDU( new UIDStringPDU("owner_6",  5),
               thearray, new UIDStringPDU("owner_10",  7), EventPDUInterface.ACTION_ADD, 12, 201250 );
            if( expPDU1 != null ){
               System.out.println("EXPANSION PDU " + cnt + ": " + expPDU1.toString() );
               pdus[cnt++] = expPDU1;
            }
            ExpansionPDU expPDU2 = new ExpansionPDU( new UIDStringPDU("owner_8", 6),
               thearray, new UIDStringPDU("owner_13", 2),
               EventPDUInterface.ACTION_REMOVE, 22, 200022 );
            if( expPDU2 != null ){
               System.out.println("EXPANSION PDU " + cnt + ": " + expPDU2.toString() );
               pdus[cnt++] = expPDU2;
            }
            ExpansionPDU expPDU3 = new ExpansionPDU( new UIDStringPDU("owner_9", 4),
               thearray, new UIDStringPDU("owner_2", 3),
               EventPDUInterface.ACTION_CHANGE, 50, 500055 );
            if( expPDU3 != null ){
               System.out.println("EXPANSION PDU " + cnt + ": " + expPDU3.toString() );
               pdus[cnt++] = expPDU3;
            }

            // Add AllocationResultPDU
            // public AllocationResultPDU( UIDPDU planElementUID, short arType,
            // int action, long executionTime, long time )

            short arType = 1;
            AllocationResultPDU arPDU1 = new AllocationResultPDU(  new UIDStringPDU("owner_6", 5),
               arType, EventPDUInterface.ACTION_REMOVE, 455, 50000 );
            if( arPDU1 != null ){
               System.out.println("ALLOCATION RESULT PDU " + cnt + ": " + arPDU1.toString() );
               pdus[cnt++] = arPDU1;
            }

            // Add PDUs to the database
            for( cnt=0; cnt < 13; cnt++ ){
               if( pdus[cnt] != null ){
                  log.add( pdus[cnt] );
               }
            }

            // Test pdu[]
            System.out.println();
            System.out.println("__________________________________");
            System.out.println("ARRAY OF PDUs ADDED TO DATABASE");
            for( cnt=0; cnt < 13; cnt++ ){
               System.out.println( pdus[cnt] + pdus[cnt].toString() );
            }

            // Test log commands
            Iterator iter = null;
            EventPDU event = null;
            UID uid = new UID("owner_11", 30);

            // Test getFirstEventTime() and getLastEventTime()
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "Test getFirstEventTime() AND etLastEventTime()" );
            System.out.println( "First event time: " + log.getFirstEventTime() );
            System.out.println( "Last event time: " + log.getLastEventTime() );
            System.out.println("__________________________________");

            // Set time variables to use during rest of the tests.
            long delta = (long)( .2 * (log.getLastEventTime() - log.getFirstEventTime()));
            long startTime = log.getFirstEventTime() +  delta;
            long stopTime = log.getLastEventTime() - delta;
            int num;

            // Test getEvents()
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getEvents() AND getNumEvents()" );
            System.out.println( "EventPDUs found: " + log.getNumEvents() );
            event = null;
            iter = log.getEvents();
            if( iter != null ){
               while( iter.hasNext() ){
                  event = (EventPDU) iter.next();
                  System.out.println( event.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test getEvents(long start, long end)
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getEvents( startTime, stopTime ) AND getNumEvents( startTime, stopTime )" );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            System.out.println( "EventPDUs found: "  + log.getNumEvents( startTime, stopTime ));
            event = null;
            iter = log.getEvents( startTime, stopTime );
            if( iter != null ){
               while( iter.hasNext() ){
                  event = (EventPDU) iter.next();
                  System.out.println( event.toString() );
               }
            }
            System.out.println("__________________________________");


            // Test getEvents( UID uid )
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getEvents(UID uid)" );
            System.out.println( "uid = " + uid.toString() );
            System.out.println( "EventPDUs found: " );
            event = null;
            iter = log.getEvents( uid );
            if( iter != null ){
               while( iter.hasNext() ){
                  event = (EventPDU) iter.next();
                  System.out.println( event.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test getEvents( UID uid, startTime, stopTime )
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getEvents(UID uid, startTime, stopTime )" );
            System.out.println( "uid = " + uid.toString() );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            System.out.println( "EventPDUs found: " );
            event = null;
            iter = log.getEvents( uid, startTime, stopTime );
            if( iter != null ){
               while( iter.hasNext() ){
                  event = (EventPDU) iter.next();
                  System.out.println( event.toString() );
               }
            }
            System.out.println("__________________________________");


            // Test getAssetEvents()
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getAssetEvents()" );
            System.out.println( "AssetPDUs found: " + log.getNumAssetEvents() );
            AssetPDU asset = null;
            iter = log.getAssetEvents();
            if( iter != null ){
               while( iter.hasNext() ){
                  asset = (AssetPDU) iter.next();
                  System.out.println( asset.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test getAssetEvents( long start, long end )
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getAssetEvents( startTime, stopTime )" );
            num = log.getNumAssetEvents( startTime, stopTime );
            System.out.println( "AssetPDUs found: " + num );
            asset = null;
            iter = log.getAssetEvents( startTime, stopTime );
            if( iter != null ){
               while(  iter.hasNext() ){
                  asset = (AssetPDU) iter.next();
                  System.out.println( asset.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test getFirstExecutionTime()
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getFirstExecutionTime()" );
            long begin = log.getFirstExecutionTime();
            System.out.println( "First Execution Start Time: " + begin );
            System.out.println("__________________________________");

            // Test getLastExecutionTime()
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getLastExecutionTime()" );
            long end = log.getLastExecutionTime();
            System.out.println( "Last Execution End Time: " + end );
            System.out.println("__________________________________");

            // Test Iterator getExecutionsStarted(long start, long end)
            // And int getNumExecutionsStarted(long start, long end)
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getExecutionsStarted(long start, long end)" );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            num = -1;
            num = log.getNumExecutionsStarted( startTime, stopTime );
            System.out.println( "ExecutionPDUs found: " + num );
            ExecutionPDU exec = null;
            iter = null;
            iter = log.getExecutionsStarted( startTime, stopTime );
            if( iter != null ){
               while( iter.hasNext() ){
                  exec = (ExecutionPDU) iter.next();
                  System.out.println( exec.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test Iterator getExecutionsStopped(long start, long end)
            // And int getNumExecutionsStopped(long start, long end)
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getExecutionsStopped(long start, long end)" );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            num = -1;
            num = log.getNumExecutionsStopped( startTime, stopTime );
            System.out.println( "ExecutionPDUs found: " + num );
            exec = null;
            iter = log.getExecutionsStopped( startTime, stopTime );
            if( iter != null ){
              while( iter.hasNext() ){
                 exec = (ExecutionPDU) iter.next();
                 System.out.println( exec.toString() );
              }
            }
            System.out.println("__________________________________");

            // Test Iterator getExecutionsActiveOnly(long start, long end)
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getExecutionsActiveOnly(long start, long end)" );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            System.out.println( "ExecutionPDUs found: ");
            exec = null;
            iter = log.getExecutionsActiveOnly( startTime, stopTime );
            if( iter != null ){
               while( iter.hasNext() ){
                  exec = (ExecutionPDU) iter.next();
                  System.out.println( exec.toString() );
               }
            }
            System.out.println("__________________________________");

            // Test Iterator getExecutionsActiveSometime(long start, long end)
            System.out.println();
            System.out.println("__________________________________");
            System.out.println( "TEST getExecutionsActiveSometime(long start, long end)" );
            System.out.println( "startTime= " + startTime + ", stopTime= " + stopTime );
            System.out.println( "ExecutionPDUs found: ");
            exec = null;
            iter = log.getExecutionsActiveSometime( startTime, stopTime );
            if( iter != null ){
               while( iter.hasNext() ){
                  exec = (ExecutionPDU) iter.next();
                  System.out.println( exec.toString() );
               }
            }
            System.out.println("__________________________________");

        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
