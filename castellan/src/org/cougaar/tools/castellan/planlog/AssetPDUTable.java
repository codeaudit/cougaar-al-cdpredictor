package org.cougaar.tools.castellan.planlog;

import org.cougaar.core.util.UID;
import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

/**
 * Title:        TaskPDUTable
 * Description:
 */

public class AssetPDUTable extends Table{

    // ATTRIBUTES
  //  PreparedStatement GetTasksStmt; 
  //  PreparedStatement GetTasksByTypeStmt;
  //  PreparedStatement GetEventsByTypeAndTimeIntervalStmt;
  //  PreparedStatement GetEventsByTimeIntervalStmt;
  //  PreparedStatement GetEventsByUIDStmt;
  //  PreparedStatement GetEventsByUIDAndTimeIntervalStmt;
  //  PreparedStatement GetNumEventsStmt;
  //  PreparedStatement GetNumEventsByTypeStmt;
  //  PreparedStatement GetNumEventsByTypeAndTimeIntervalStmt;
  //  PreparedStatement GetNumEventsByTimeIntervalStmt;
  //  PreparedStatement GetNumUIDsStmt;
   // PreparedStatement SetEventStmt;


//    java.sql.Statement PdStatement;
//    java.sql.Statement PdStatement2;

    PreparedStatement AssetStmt;
  //  PreparedStatement TaskStmt;
   // PreparedStatement AssetStmt;
   // PreparedStatement AllocResultStmt;
   // PreparedStatement ProcessedByStmt;
   // PreparedStatement ScoreStmt;
   // PreparedStatement AllocTaskOnStmt;
    
   // CONSTRUCTORS
   public AssetPDUTable( Connection conn ){
      super(conn);
   }

   // METHODS

     public void add(PDU pdu1)
    {
		AssetPDU pdu = (AssetPDU) pdu1; 
        String q, t;
        String ASSETUID, SID, ITEMID, TYPEID, AEVENTTYPE, AEVENTTIME, SOURCE, ASSETCLASS, CREATEDEXECUTION;

        int tsize;

        UIDStringPDU uid = (UIDStringPDU) pdu.getUID();

        // field
        try
        {
            AssetStmt.setLong( 1, uid.getId() );                           // ASSETUID
            AssetStmt.setString( 2, uid.getOwner() );                      // SID
            AssetStmt.setString( 3, pdu.getItemId() );                     // ITEMID
            AssetStmt.setString( 4, pdu.getAssetTypeId() );                // TYPEID
            AssetStmt.setString( 5, pdu.getSource() );                     // SOURCE
            AssetStmt.setString( 6, ( pdu.getAssetClass() ).toString() );    // ASSETCLASS
            AssetStmt.setLong( 7, pdu.getExecutionTime() );                // CREATEDEXECUTION
            AssetStmt.setInt( 8, pdu.getAction() );                        // AEVENTTYPE
            AssetStmt.setLong( 9, pdu.getTime() );                         // AEVENTTIME
            AssetStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( pdu.toString() );
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
//            loggingError( E.toString() );
        }
	}

    public void createTable(Statement theStatement) {

       try {
         // Create the table for all EventPDUs.  Create an index on the
         // ( UIDNAME, UID ) combination.
            String query;
            query = "CREATE TABLE ASSET ( ASSETUID BIGINT NOT NULL, " +
                    " ASID VARCHAR(20) NOT NULL, ITEMID TINYTEXT, " +
                    " TYPEID TINYTEXT, SOURCE VARCHAR(20), ASSETCLASS VARCHAR(80), " +
                    " CREATEDEXECUTION BIGINT, AEVENTTYPE INT, AEVENTTIME BIGINT NOT NULL)" ;
            myStatement.executeQuery( query ) ;
           // query = "CREATE INDEX EVENTSBYTIME ON EVENTPDU ( TIME )" ;
           // myStatement.executeQuery( query ) ;
           // query = "CREATE INDEX EVENTSBYUID ON EVENTPDU ( OWNER( 16 ), ID )" ;
           // myStatement.executeQuery( query ) ;
           // query = "CREATE INDEX EVENTSBYTYPE ON EVENTPDU ( TYPE )" ;
           // myStatement.executeQuery( query ) ;
          //  query = "CREATE INDEX EVENTSBYID ON EVENTPDU ( ID )" ;
          //  myStatement.executeQuery( query ) ;

        }
        catch ( SQLException e ) {
            printSQLException( e );
            e.printStackTrace();
        }
    }    
    
	
    public void loadPreparedStatements( Connection conn ){
      try{
         AssetStmt = conn.prepareStatement(
                    "INSERT INTO ASSET VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}

