package org.cougaar.tools.castellan.planlog;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.*;
import org.cougaar.planning.ldm.plan.AspectValue;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

/**
 * Title:        ExecutionPDUTable
 * Description:
 */

public class ExecutionPDUTable extends Table{
   
   // ATTRIBUTES
    PreparedStatement GetAgentsStmt;
    PreparedStatement GetExecutionsActiveOnlyStmt;
    PreparedStatement GetExecutionsActiveSometimeStmt;
    PreparedStatement GetExecutionsByAgentStmt;
    PreparedStatement GetExecutionsStartedStmt;
    PreparedStatement GetExecutionsStoppedStmt;
    PreparedStatement GetFirstExecutionTimeStmt;
    PreparedStatement GetLastExecutionTimeStmt;
    PreparedStatement GetNumExecutionsStartedStmt;
    PreparedStatement GetNumExecutionsStoppedStmt;
    PreparedStatement SetExecutionStmt;

   // CONSTRUCTORS
   public ExecutionPDUTable( Connection conn ){
      super(conn);
   }

   // METHODS
    public void add(PDU pdu ){
       if( pdu instanceof ExecutionPDU ){
          ExecutionPDU expdu = ( ExecutionPDU )pdu;
          // Add field values to the prepared statement: EventPDUStmt.
          try
          {
              SetExecutionStmt.setString( 1, expdu.getClusterIdentifier() );   // AGENTNAME
              SetExecutionStmt.setString( 2, expdu.getPlugInName() ); // PLUGIN NAME
              SetExecutionStmt.setInt( 3, expdu.getPlugInHash() );   // PLUGIN HASH CODE
              SetExecutionStmt.setLong( 4, expdu.getStartTime() );    // START TIME
              SetExecutionStmt.setLong( 5, expdu.getStopTime( ) );      // STOP TIME
              ByteArrayOutputStream baostream = new ByteArrayOutputStream();
              ObjectOutputStream oostream = new ObjectOutputStream( baostream );
              oostream.writeObject( expdu );
              oostream.flush();
              ByteArrayInputStream bis = new ByteArrayInputStream( baostream.toByteArray() );
              SetExecutionStmt.setBinaryStream( 6, bis, bis.available() ); // Blob the PDU
              SetExecutionStmt.executeUpdate();
          }
          catch ( SQLException ex )
          {
             printSQLException( ex );
             ex.printStackTrace();
             String AGENTNAME = expdu.getClusterIdentifier();
             String PLUGINNAME = expdu.getPlugInName();
             int PLUGINHASHCODE = expdu.getPlugInHash();
             String STARTTIME = Long.toString( expdu.getStartTime() );
             String STOPTIME = Long.toString( expdu.getStopTime() );
             String q = AGENTNAME + "," + PLUGINNAME + "," + PLUGINHASHCODE + "," + STARTTIME + "," + STOPTIME;
          }
          catch ( IOException ioe )
          {
             System.out.println( ioe );
          }
       }
    }

    public void createTable(Statement stmt) {
        try {
            String query;
            query = "CREATE TABLE EXECUTIONPDU ( AGENTNAME CHAR(64) NOT NULL, " +
               "PLUGINNAME VARCHAR(255) NOT NULL, PLUGINHASHCODE INT NOT NULL, " +
               "STARTTIME BIGINT NOT NULL, STOPTIME BIGINT NOT NULL, " +
               "PDU BLOB NOT NULL )" ;
            stmt.executeQuery( query ) ;
        }
        catch ( SQLException e ) {
            printSQLException( e );
        }
    }
    
    public Collection getAgentNames(){
       ResultSet rs = null;
       Vector agentNames = null;
       String agentName = null;
       try{
          // Make the query.
          rs = GetAgentsStmt.executeQuery();
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       if( rs == null ) return null;
       else{
          agentNames = new Vector();
          try{
             while( !rs.isLast() ){
                rs.next();
                agentName = rs.getString( 1 );
                agentNames.add( agentName );
             }
          }
          catch ( SQLException ex ){
             printSQLException( ex );
          }
          return agentNames;
       }
    }
    
    public Iterator getExecutionsActiveOnly(long start, long end){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetExecutionsActiveOnlyStmt.setLong( 1, start );
          GetExecutionsActiveOnlyStmt.setLong( 2, end );
          // Make the query.
          rs = GetExecutionsActiveOnlyStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public Iterator getExecutionsActiveSometime(long start, long end){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetExecutionsActiveSometimeStmt.setLong( 1, start );
          GetExecutionsActiveSometimeStmt.setLong( 2, end );
          // Make the query.
          rs = GetExecutionsActiveSometimeStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }
    public  Iterator getExecutionsByAgent( String theAgentName ){
       ResultSet rs = null;
       try{
          // Add values for prepared statement.
          GetExecutionsByAgentStmt.setString( 1, theAgentName );
          // Make the query
          rs = GetExecutionsByAgentStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }
          
    public Iterator getExecutionsStarted(long start, long end){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
         GetExecutionsStartedStmt.setLong( 1, start );
         GetExecutionsStartedStmt.setLong( 2, end );
          // Make the query.
          rs = GetExecutionsStartedStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public Iterator getExecutionsStopped(long start, long end){
       ResultSet rs = null;
       try{
          // Add field values for prepared statement.
          GetExecutionsStoppedStmt.setLong( 1, start );
          GetExecutionsStoppedStmt.setLong( 2, end );
          // Make the query.
          rs = GetExecutionsStoppedStmt.executeQuery();
          if( rs == null ) return null;
       }
       catch ( SQLException ex ){
          printSQLException( ex );
       }
       return new PDUList( rs );
    }

    public long getFirstExecutionTime(){
      long firstTime = 0;
      try
      {
          ResultSet rs = myStatement.executeQuery( "SELECT MIN(STARTTIME) FROM EXECUTIONPDU" );
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

    public long getLastExecutionTime(){
      long lastTime = 0;
      try
      {
          ResultSet rs = myStatement.executeQuery( "SELECT MAX(STOPTIME) FROM EXECUTIONPDU" );
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

    public int getNumExecutionsStarted(long start, long end)
    {
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
          GetNumExecutionsStartedStmt.setLong( 1, start );
          GetNumExecutionsStartedStmt.setLong( 2, end );          
          // Make the query.
          rs = GetNumExecutionsStartedStmt.executeQuery();         
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

    public int getNumExecutionsStopped(long start, long end)
    {
       ResultSet rs = null;
       int numEvents = -1;
       try{
          // Add field values for prepared statement.
          GetNumExecutionsStoppedStmt.setLong( 1, start );
          GetNumExecutionsStoppedStmt.setLong( 2, end );          
          // Make the query.
          rs = GetNumExecutionsStoppedStmt.executeQuery();         
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

    public void loadPreparedStatements( Connection conn ){
      try{         
         GetAgentsStmt = conn.prepareStatement(
                 "SELECT DISTINCT AGENTNAME FROM EXECUTIONPDU" );
         GetExecutionsActiveOnlyStmt = conn.prepareStatement(
                 "SELECT * FROM EXECUTIONPDU WHERE STARTTIME >= ? AND STOPTIME <= ? " );
         GetExecutionsActiveSometimeStmt = conn.prepareStatement(
                 "SELECT * FROM EXECUTIONPDU WHERE STARTTIME >= ? OR STOPTIME <= ? " );
         GetExecutionsByAgentStmt = conn.prepareStatement(
                 "SELECT * FROM EXECUTIONPDU WHERE AGENTNAME = ? " ); 
         GetExecutionsStartedStmt = conn.prepareStatement(
                 "SELECT * FROM EXECUTIONPDU WHERE STARTTIME >= ? AND STARTTIME <= ? " );
         GetExecutionsStoppedStmt = conn.prepareStatement(
                 "SELECT * FROM EXECUTIONPDU WHERE STOPTIME >= ? AND STOPTIME <= ? " );
         GetNumExecutionsStartedStmt = conn.prepareStatement( 
                 "SELECT COUNT(*) FROM EXECUTIONPDU WHERE STARTTIME >= ? AND STARTTIME <= ? " );
         GetNumExecutionsStoppedStmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM EXECUTIONPDU WHERE STOPTIME >= ? AND STOPTIME <= ? " );
         SetExecutionStmt = conn.prepareStatement( 
                 "INSERT INTO EXECUTIONPDU VALUES( ?, ?, ?, ?, ?, ? )" );
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}

          

