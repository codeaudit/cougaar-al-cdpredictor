/*
 * InMemoryLog.java
 *
 * Created on August 28, 2001, 4:26 PM
 */

package org.hydra.planlog;
import org.hydra.pdu.* ;
import org.hydra.util.* ;
import java.util.* ;

import org.dbinterface.*;

public class DatabaseEventLog implements EventLog {

    public DatabaseEventLog( String databaseName ) {
        this.databaseName = databaseName ;

     // Begin: This part is added by Yunho Hong, September 27, 2001
        PlanDb = new PlanDatabaseImpl();

        PlanDb.startLogging();

        PlanDb.establishConnection();

        PlanDb.createDatabase(databaseName);

        PlanDb.useDataBase(databaseName);

        PlanDb.createTables("def.xml");

     // PlanDb.makeSIDTable("TEST-NODE.INI");
     // End: This part is added by Yunho Hong, September 27, 2001
    }

    protected DatabaseEventLog( String databaseName, PlanDatabaseImpl impl ) {
        this.PlanDb = impl ;
        this.databaseName = databaseName ;
    }

    public static DatabaseEventLog createDatabase( String databaseName ) {
        if ( databaseName == null ) {
            throw new RuntimeException( "Invalid database name." ) ;
        }
        PlanDatabaseImpl planDb = new PlanDatabaseImpl();

        planDb.startLogging();

        planDb.establishConnection();

        try {
        planDb.createDatabase(databaseName);
        }
        catch ( Exception e ) {
            throw new RuntimeException( "Error creating database." ) ;
        }

        planDb.useDataBase(databaseName);

        planDb.createTables("def.xml");

        return new DatabaseEventLog( databaseName, planDb ) ;
    }

    public static DatabaseEventLog openDatabase( String databaseName ) {
        if ( databaseName == null ) {
            throw new RuntimeException( "Invalid database name." ) ;
        }
        PlanDatabaseImpl planDb = new PlanDatabaseImpl();

        planDb.startLogging();

        planDb.establishConnection();

        try {
        planDb.useDataBase(databaseName);
        }
        catch ( Exception e ) {
            throw new RuntimeException( "Error using database " + databaseName ) ;
        }
        return new DatabaseEventLog( databaseName, planDb ) ;
    }

    public void close() {
        PlanDb.closeConnection() ;
    }

    public String getName() { return databaseName ; }

    public synchronized void add( PDU p ) {
        Long l ;
        EventPDU pdu = ( EventPDU ) p ;
        PlanDb.storeEventPDUIntoDb((EventPDU) p);
    }

    public void clear() {
        //assetEvents.clear() ;
        //allEvents.clear() ;
        //set.clear() ;
    }

    /** Get all events between a start time and end time.
     */
    public synchronized Iterator getEventsBetween(long start, long end) {

         int k;
         UniqueObjectPDU p1, p2;
         Iterator i2 = PlanDb.getEventsBetween(start, end, 0);
        return i2;
    }

    public synchronized long getFirstEventTime() {
        return PlanDb.getFirstEventTime();
    }

    public synchronized long getLastEventTime() {
        return PlanDb.getLastEventTime() ;
    }

    public Iterator getEventsByUID(String uid) {
        return null ;
    }
    
    public int getNumUniqueUIDs( long start, long end ) {
        return PlanDb.getNumUniqueUIDs( start, end ) ;
    }

    /** Returns all asset events between start and end.
     */
    public synchronized Iterator getAssetEvents(long start, long end) {
        Iterator i2 = PlanDb.getEventsBetween(start, end, 3);  // 3 == TYPE_ASSET;
        return i2;
    }

    /** Returns all asset events.
     */
    public synchronized Iterator getAssetEvents() {
        Iterator i2 = PlanDb.getEventsBetween(getFirstEventTime(),
            getLastEventTime(), 3 ) ;
        return i2 ;
    }

    String databaseName ;

    PlanDatabase PlanDb;

    public static void main( String[] args ) {
        System.out.println( "Creating database" ) ;
        DatabaseEventLog del = new DatabaseEventLog( "Test" ) ;

        System.out.println( "Closing database." ) ;
        del.close();
    }
    
    public int getNumEventsBetween(long start, long end) {
        return PlanDb.getNumEventsBetween( start, end ) ;
    }
    
}
