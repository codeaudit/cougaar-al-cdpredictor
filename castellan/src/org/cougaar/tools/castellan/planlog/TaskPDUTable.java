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

public class TaskPDUTable extends Table{


      PreparedStatement TaskStmt;
    
	// CONSTRUCTORS
	public TaskPDUTable( Connection conn ){
      super(conn);
	}

	// METHODS

    public void add(PDU pdu1 )
    {

		TaskPDU pdu = (TaskPDU) pdu1;
        String q, t, a;
        String TASKUID, SID, CREATEDEXECUTION, RESCINDTIME, VERB, TEVENTTYPE, TEVENTTIME, SOURCE, PARENTID, PARENTSID;

        int tsize, MPTASK;

        UIDStringPDU uid = (UIDStringPDU) pdu.getUID();

        // field
/*
      TASKUID          = Long.toString(uid.getId());
      SID               ="'"+ uid.getOwner()+"'";
      CREATEDEXECUTION = Long.toString(pdu.getExecutionTime());
      VERB             = "'"+(pdu.getTaskVerb()).toString() +"'";         //String
*/
        if ( pdu instanceof MPTaskPDU )
        {
            MPTASK = 1;
        } // This is MPTask.
        else
        {
            MPTASK = 0;
        } // This is not MPTask.

//      SOURCE           = "'"+pdu.getSource()+"'";

        UIDStringPDU puid = (UIDStringPDU) pdu.getParentTask();

//      TEVENTTYPE       = Integer.toString(pdu.getAction());
//      TEVENTTIME       = Long.toString(pdu.getTime());

//      q = TASKUID+","+SID+","+CREATEDEXECUTION+","+VERB+","+MPTASK+","+SOURCE+","+PARENTID+","+PARENTSID+","+TEVENTTYPE+","+TEVENTTIME;

//      InsertIntoDb(TASK,q);

        try
        {

            TaskStmt.setLong( 1, uid.getId() );                         // TASKUID
            TaskStmt.setString( 2, uid.getOwner() );                    // SID
            TaskStmt.setLong( 3, pdu.getExecutionTime() );              // CREATEDEXECUTION
            TaskStmt.setString( 4, ( pdu.getTaskVerb() ).toString() );    // VERB

            if ( pdu instanceof MPTaskPDU )
            {
                TaskStmt.setInt( 5, 1 );
            } // This is MPTask.
            else
            {
                TaskStmt.setInt( 5, 0 );
            } // This is not MPTask.

            TaskStmt.setString( 6, pdu.getSource() );                   // SOURCE
            if ( puid != null )
            {
                TaskStmt.setLong( 7, puid.getId() );                      // PARENTID
                TaskStmt.setString( 8, puid.getOwner() );                 // PARENTSID
            }
            else
            {
                TaskStmt.setLong( 7, 0 );                                 // PARENTID
                TaskStmt.setString( 8, "''" );                            // PARENTSID
            }
            TaskStmt.setInt( 9, pdu.getAction() );                      // TEVENTTYPE
            TaskStmt.setLong( 10, pdu.getTime() );                      // TEVENTTIME

//			int ii = 0;
			String s ="";
			if (MPTASK == 0)
			{
//				System.out.println(pdu.getDirectObject()+":");
//				System.out.println((Integer.valueOf(pdu.getDirectObject())).intValue()+ ", " +pdu.getDirectObject());
//				ii = (Integer.valueOf(pdu.getDirectObject())).intValue();
			
				s = pdu.getDirectObject();
			}

//			TaskStmt.setInt( 11,ii);	// DIRECTOBJECT
			TaskStmt.setString( 11, s );	// DIRECTOBJECT
            TaskStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( pdu.toString() );
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
//            loggingError( E.toString() );
        }

//      loggingMessage(pdu.toString());
    }


    public void createTable(Statement theStatement) {
       try {
         // Create the table for all EventPDUs.  Create an index on the
         // ( UIDNAME, UID ) combination.
			String query;
/*
            query = "CREATE TABLE TASK ( TASKUID BIGINT NOT NULL, " +
                    " TASKSID VARCHAR(20) NOT NULL, CREATEDEXECUTION BIGINT, " +
                    " VERB VARCHAR(30), MPTASK INT, SOURCE VARCHAR(20), " +
                    " PARENTID BIGINT, PARENTSID VARCHAR(20), TEVENTTYPE INT, TEVENTTIME BIGINT NOT NULL, DIRECTOBJECT INT)" ;
*/
	        query = "CREATE TABLE TASK ( TASKUID BIGINT NOT NULL, " +
                    " TASKSID VARCHAR(20) NOT NULL, CREATEDEXECUTION BIGINT, " +
                    " VERB VARCHAR(30), MPTASK INT, SOURCE VARCHAR(20), " +
                    " PARENTID BIGINT, PARENTSID VARCHAR(20), TEVENTTYPE INT, TEVENTTIME BIGINT NOT NULL, DIRECTOBJECT VARCHAR(30))" ;
			
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
         TaskStmt = conn.prepareStatement(
                    "INSERT INTO TASK VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}

