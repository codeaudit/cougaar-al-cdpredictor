package org.cougaar.tools.castellan.planlog;

import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

/**
 * Title:        EventPDUTable
 * Description:
 */

public class EventPDUTable extends Table{

    // ATTRIBUTES
    PreparedStatement GetEventsStmt; 
    PreparedStatement GetEventsByTypeStmt;
    PreparedStatement GetEventsByTypeAndTimeIntervalStmt;
    PreparedStatement GetEventsByTimeIntervalStmt;
    PreparedStatement GetEventsByUIDStmt;
    PreparedStatement GetEventsByUIDAndTimeIntervalStmt;
    PreparedStatement GetNumEventsStmt;
    PreparedStatement GetNumEventsByTypeStmt;
    PreparedStatement GetNumEventsByTypeAndTimeIntervalStmt;
    PreparedStatement GetNumEventsByTimeIntervalStmt;
    PreparedStatement GetNumUIDsStmt;
    PreparedStatement SetEventStmt;
    
   // CONSTRUCTORS
   public EventPDUTable( Connection conn ){
      super(conn);
   }

   // METHODS

    public void add( PDU pdu ){
       if( pdu instanceof EventPDU ){
          EventPDU evpdu = ( EventPDU )pdu;
          int artype;
          UIDStringPDU uid;
          if ( evpdu.getType() == EventPDU.TYPE_ALLOCATION_RESULT )
          {
              AllocationResultPDU ARPDU = (AllocationResultPDU) evpdu;
              uid = (UIDStringPDU) ARPDU.getPlanElementUID();
              artype = ARPDU.getARType();
          }
          else
          {
              UniqueObjectPDU UOPDU = (UniqueObjectPDU) evpdu;
              uid = (UIDStringPDU) UOPDU.getUID();
              artype = 0;
          }
          // Add field values to the prepared statement: EventPDUStmt.
          try
          {
              SetEventStmt.setLong( 1, uid.getId() );      // UID
              SetEventStmt.setString( 2, uid.getOwner() ); // SID
              SetEventStmt.setLong( 3, evpdu.getTime() );   // TIME
              SetEventStmt.setInt( 4, evpdu.getType() );    // TYPE
              SetEventStmt.setInt( 5, artype );            // ARTYPE
              SetEventStmt.setInt( 6, evpdu.getAction() );  // EVENTTYPE
              ByteArrayOutputStream baostream = new ByteArrayOutputStream();
              ObjectOutputStream oostream = new ObjectOutputStream( baostream );
              oostream.writeObject( evpdu );
              oostream.flush();
              ByteArrayInputStream bis = new ByteArrayInputStream( baostream.toByteArray() );
              SetEventStmt.setBinaryStream( 7, bis, bis.available() ); // Blob the PDU
              SetEventStmt.executeUpdate();
          }
          catch ( SQLException ex )
          {
             printSQLException( ex );
             ex.printStackTrace();
             String UID = Long.toString( uid.getId() );
             String SID = "'" + uid.getOwner() + "'";
             String TIME = Long.toString( evpdu.getTime() );
             String TYPE = evpdu.typeToString( evpdu.getType() ) + Byte.toString( evpdu.getType() );
             String ARTYPE = Integer.toString( artype );
             String EVENTTYPE = Integer.toString( evpdu.getAction() );
             String q = UID + "," + SID + "," + TIME + "," + TYPE + "," + ARTYPE + "," + EVENTTYPE;
          }
          catch ( IOException ioe )
          {
             System.out.println( ioe );
          }
       }
    }

    public void createTable(Statement theStatement) {
       try {
         // Create the table for all EventPDUs.  Create an index on the
         // ( UIDNAME, UID ) combination.
            String query;
            query = "CREATE TABLE EVENTPDU ( ID BIGINT NOT NULL, " +
                    " OWNER CHAR(32) NOT NULL, TIME BIGINT NOT NULL, " +
                    " TYPE INT NOT NULL, ALLOCATION_RESULT_TYPE INT NOT NULL, " +
                    " ACTION INT NOT NULL, PDU BLOB NOT NULL )" ;
            myStatement.executeQuery( query ) ;
            query = "CREATE INDEX EVENTSBYTIME ON EVENTPDU ( TIME )" ;
            myStatement.executeQuery( query ) ;
            query = "CREATE INDEX EVENTSBYUID ON EVENTPDU ( OWNER( 16 ), ID )" ;
            myStatement.executeQuery( query ) ;
            query = "CREATE INDEX EVENTSBYTYPE ON EVENTPDU ( TYPE )" ;
            myStatement.executeQuery( query ) ;
            query = "CREATE INDEX EVENTSBYID ON EVENTPDU ( ID )" ;
            myStatement.executeQuery( query ) ;
        }
        catch ( SQLException e ) {
            printSQLException( e );
            e.printStackTrace();
        }
    }    
    
    // Null return indicates no entries in result set
    public Iterator getEvents( int type ){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetEventsByTypeStmt.setInt( 1, type );    // TYPE
          // Make the query.
          rs = GetEventsByTypeStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }
    
    public Iterator getEvents(){
       ResultSet rs = null;
       try{
          rs = GetEventsStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public Iterator getEvents( long start, long end ){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetEventsByTimeIntervalStmt.setLong( 1, start );
          GetEventsByTimeIntervalStmt.setLong( 2, end );
          // Make the query.
          rs = GetEventsByTimeIntervalStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public Iterator getEvents( int type, long start, long end ){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetEventsByTypeAndTimeIntervalStmt.setInt( 1, type );
          GetEventsByTypeAndTimeIntervalStmt.setLong( 2, start );
          GetEventsByTypeAndTimeIntervalStmt.setLong( 3, end );
          // Make the query.
          rs = GetEventsByTypeAndTimeIntervalStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }
    
    public Iterator getEvents( UID uid )
    {
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          // UID will have a lookup on 2 elements: owner and ID
          GetEventsByUIDStmt.setLong( 1, uid.getId() );    // ID
          GetEventsByUIDStmt.setString( 2, uid.getOwner() ); // Owner
          // Make the query.
          rs = GetEventsByUIDStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public Iterator getEvents( UID uid, long start, long end  )
    {
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          // UID will have a lookup on 2 elements: owner and ID
          GetEventsByUIDAndTimeIntervalStmt.setLong( 1, uid.getId() );    // ID
          GetEventsByUIDAndTimeIntervalStmt.setString( 2, uid.getOwner() ); // Owner
          GetEventsByUIDAndTimeIntervalStmt.setLong( 3, start ); // Start of time interval
          GetEventsByUIDAndTimeIntervalStmt.setLong( 4, end ); // End of time interval
          // Make the query.
          rs = GetEventsByUIDAndTimeIntervalStmt.executeQuery();
          System.out.println(" rs = " + rs );
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public long getFirstEventTime() {
      long firstTime = 0;
      try
      {
          ResultSet rs = myStatement.executeQuery( "SELECT MIN(TIME) FROM EVENTPDU" );
          if( rs == null ) return -1;
          if ( rs.next() )
          {
              // Get value from column "1" in the result set
              firstTime = rs.getLong( 1 );
          }
      }
      catch ( SQLException ex)
      {
         printSQLException( ex );
      }
      return firstTime;
    }    
    
    public long getLastEventTime() {
      long lastTime = 0;
      try
      {
          ResultSet rs = myStatement.executeQuery( "SELECT MAX(TIME) FROM EVENTPDU" );
          if( rs == null ) return -1;
          if ( rs.next() )
          {
              // Get value from column "1" in the result set
              lastTime = rs.getLong( 1 );
          }
      }
      catch ( SQLException ex)
      {
         printSQLException( ex );
      }
      return lastTime;
    }

// null ResultSet implies no results numEvents=0
    public int getNumEvents( ){
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Make the query.
          rs = GetNumEventsStmt.executeQuery();         
          if( rs == null ) return 0;
          // Result is in the   
          rs.first();
          numEvents = rs.getInt( 1 );
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return numEvents;
    }

    public int getNumEvents( long start, long end ){
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
          GetNumEventsByTimeIntervalStmt.setLong( 1, start );
          GetNumEventsByTimeIntervalStmt.setLong( 2, end );          
          // Make the query.
          rs = GetNumEventsByTimeIntervalStmt.executeQuery();         
          if( rs == null ) return 0;
          // Result is in the   
          rs.first();
          numEvents = rs.getInt( 1 );
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return numEvents;
    }

    public int getNumEvents( int type ){
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
          GetNumEventsByTypeStmt.setInt( 1, type );
          // Make the query.
          rs = GetNumEventsByTypeStmt.executeQuery();
          if( rs == null ) return 0;
          // Result is in the   
          rs.first();
          numEvents = rs.getInt( 1 );
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }       
       return numEvents;
    }

    public int getNumEvents( int type, long start, long end ){
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
          GetNumEventsByTypeAndTimeIntervalStmt.setInt( 1, type );
          GetNumEventsByTypeAndTimeIntervalStmt.setLong( 2, start );
          GetNumEventsByTypeAndTimeIntervalStmt.setLong( 3, end );
          // Make the query.
          rs = GetNumEventsByTypeAndTimeIntervalStmt.executeQuery();
          // Result is in the   
          if( rs == null ) return 0;
          rs.first();
          numEvents = rs.getInt( 1 );
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }       
       return numEvents;
    }

    public int getNumUIDs(){
       System.out.println( "Not implemented yet!" );
       return -1;
    }

    public int getNumUIDs( long start, long end ){
       System.out.println( "Not implemented yet!" );
       return -1;
       /*
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
         GetNumUniqueUIDsStmt.setLong( 1, start );         // STARTIME
         GetNumUniqueUIDsStmt.setLong( 2, end );           // STOPTIME
          // Make the query.
          rs = GetNumUniqueUIDsStmt.executeQuery();
          // Result is in the   
          rs.first();
          numEvents = rs.getInt( 1 );
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }       
       return numEvents;
        */
    }
    
    public void loadPreparedStatements( Connection conn ){
      try{
         SetEventStmt = conn.prepareStatement(
                 "INSERT INTO EVENTPDU VALUES( ?, ?, ?, ?, ?, ?, ? )" );
         GetEventsStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU" );
         GetEventsByTypeStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU WHERE TYPE = ? " );
         GetEventsByTypeAndTimeIntervalStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU WHERE TYPE = ? AND TIME >= ? AND TIME <= ? " );
         GetEventsByTimeIntervalStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU WHERE TIME >= ? AND TIME <= ? " );
         GetEventsByUIDStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU WHERE ID = ? AND OWNER = ? " );
         GetEventsByUIDAndTimeIntervalStmt = conn.prepareStatement(
                 "SELECT * FROM EVENTPDU WHERE ID = ? AND OWNER = ? " +
                 "AND TIME >= ? AND TIME <= ? " );
         GetNumEventsStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EVENTPDU" );
         GetNumEventsByTimeIntervalStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EVENTPDU WHERE TIME >= ? AND TIME <= ? " );
         GetNumEventsByTypeStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EVENTPDU WHERE TYPE = ? " );
         GetNumEventsByTypeAndTimeIntervalStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EVENTPDU WHERE TYPE = ? AND TIME >= ? " + 
                 "AND TIME <= ? " );
         GetNumUIDsStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EVENTPDU WHERE TIME >= ? AND TIME <= ? " +
                 "AND OWNER = ? AND ID = ? " );        
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}

