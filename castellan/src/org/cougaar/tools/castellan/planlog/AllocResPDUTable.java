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

public class AllocResPDUTable extends Table{

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

  //  PreparedStatement PlanElementStmt;
    PreparedStatement AllocResultStmt;
   // PreparedStatement AssetStmt;
   // PreparedStatement AllocResultStmt;
   // PreparedStatement ProcessedByStmt;
   // PreparedStatement ScoreStmt;
   // PreparedStatement AllocTaskOnStmt;
    
   // CONSTRUCTORS
   public AllocResPDUTable( Connection conn ){
      super(conn);
   }

    public void add( PDU pdu1 )
    {
		AllocationResultPDU pdu = (AllocationResultPDU) pdu1;
        String PEUID, PESID, PEVENTTYPE, PEVENTTIME, ARUID, ARTYPE, SUCCESS, TOTALSCORE,
                q, ASPECTTYPE, SOURCE, CREATEDEXECUTION, VALUE, SCORE, CONFIDENCE;

        // ALLOC_RESULT
        /**
         * TBD. The program should allocate new id on ARUID.
         * AllocationResultMessage does not have member functions for refering to
         * its instance variables.
         */
        // Table : ALLOC_RESULT
        UIDStringPDU uid = (UIDStringPDU) pdu.getPlanElementUID();
/*
      PESID           = "'"+uid.getOwner()+"'";

      uid = (UIDStringPDU) pdu.getPlanElementUID();

      PEUID       = Long.toString(uid.getId());
      ARTYPE      = Integer.toString(pdu.getARType());
      SUCCESS     = Byte.toString(pdu.getSuccess());
      SOURCE      = "'"+pdu.getSource()+"'";
      CREATEDEXECUTION = Long.toString(pdu.getExecutionTime());
      PEVENTTYPE       = Integer.toString(pdu.getAction());
      PEVENTTIME  = Long.toString(pdu.getTime());

      TOTALSCORE  = "0";
      CONFIDENCE  = Float.toString(pdu.getConfidence());

      q =  PEUID+","+ PESID+","+ SOURCE+","+CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME+","+ ARTYPE+","+ SUCCESS+","+ TOTALSCORE+","+CONFIDENCE;
      InsertIntoDb(ALLOC_RESULT,q);
*/
        try
        {
            AllocResultStmt.setLong( 1, uid.getId() );              // PEUID
            AllocResultStmt.setString( 2, uid.getOwner() );         // PESID
            AllocResultStmt.setString( 3, pdu.getSource() );        // SOURCE
            AllocResultStmt.setLong( 4, pdu.getExecutionTime() );   // CREATEDEXECUTION
            AllocResultStmt.setInt( 5, pdu.getAction() );           // PEVENTTYPE
            AllocResultStmt.setLong( 6, pdu.getTime() );            // PEVENTTIME
            AllocResultStmt.setInt( 7, pdu.getARType() );           // ARTYPE
            AllocResultStmt.setInt( 8, pdu.getSuccess() );          // SUCCESS
            AllocResultStmt.setFloat( 9, 1 );                       // TOTALSCORE
            AllocResultStmt.setFloat( 10, pdu.getConfidence() );    // CONFIDENCE
            AllocResultStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
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
            query = "CREATE TABLE ALLOC_RESULT ( PEUID BIGINT NOT NULL, " +
                    " PESID VARCHAR(20) NOT NULL, SOURCE VARCHAR(20), CREATEDEXECUTION BIGINT, " +
                    " PEVENTTYPE INT NOT NULL, PEVENTTIME BIGINT NOT NULL, ARTYPE INT NOT NULL, " +
                    " SUCCESS TINYINT, TOTLALSCORE FLOAT, CONFIDENCE FLOAT, FOREIGN KEY (PEUID) REFERENCES PLAN_ELEMENT ON DELETE CASCADE ON UPDATE CASCADE, " +
					" FOREIGN KEY (PEVENTTIME)  REFERENCES PLAN_ELEMENT ON DELETE CASCADE ON UPDATE CASCADE	)" ;
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
         AllocResultStmt = conn.prepareStatement(
                    "INSERT INTO ALLOC_RESULT VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}

