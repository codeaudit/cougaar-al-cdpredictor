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

public class PlanElementPDUTable extends Table{

    PreparedStatement PlanElementStmt;
	PreparedStatement ProcessedByStmt;
	PreparedStatement AllocTaskOnStmt;
    java.sql.Statement PdStatement;
	
   // CONSTRUCTORS
   public PlanElementPDUTable( Connection conn ){
      super(conn);

	  try {
		
	      PdStatement = conn.createStatement();
	    
	  } catch(SQLException ex) {

		System.err.println("(EstablishConnetction) -----SQLException-----");
		System.err.println("SQLState:  " + ex.getSQLState());
		System.err.println("Message:  " + ex.getMessage());
		System.err.println("Vendor:  " + ex.getErrorCode());
	  } 
   }

   // METHODS

    public void add(PDU pdu)
    {
		if ( pdu instanceof ExpansionPDU )
        {
            storeExpansionPDUIntoDb((ExpansionPDU) pdu);
        }
        else if ( pdu instanceof AllocationPDU )
        {
	        storeAllocationPDUIntoDb((AllocationPDU)pdu);
        }
        else if ( pdu instanceof AggregationPDU )
        {
            storeAggregationPDUIntoDb((AggregationPDU) pdu);
        }
	}

    public void storeExpansionPDUIntoDb( ExpansionPDU pdu ) throws RuntimeException
    {

        String q, t;
        String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE, CREATEDEXECUTION;  // for PLAN_ELEMENT
        String PESID ,TASKUID ,TASKSID ,PROCESSED;       // for PROCESSED_BY

        int tsize, i;
        UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

        // PLAN_ELEMENT
        try
        {
            PlanElementStmt.setLong( 1, uid1.getId() );             // PEUID
            PlanElementStmt.setString( 2, uid1.getOwner() );        // PESID
            PlanElementStmt.setInt( 3, 2 );                         // TYPE   => // EXPANSION == 2.
            PlanElementStmt.setString( 4, pdu.getSource() );        // SOURCE
            PlanElementStmt.setLong( 5, pdu.getExecutionTime() );   // CREATEDEXECUTION
            PlanElementStmt.setInt( 6, pdu.getAction() );           // PEVENTTYPE
            PlanElementStmt.setLong( 7, pdu.getTime() );            // PEVENTTIME
            PlanElementStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
            System.out.println( "Error in Expandion PE");
            PEUID = Long.toString( uid1.getId() );
            PESID = "'" + uid1.getOwner() + "'";
            TYPE = "2";                           // EXPANSION == 2.
            SOURCE = "'" + pdu.getSource() + "'";
            CREATEDEXECUTION = Long.toString( pdu.getExecutionTime() );
            PEVENTTYPE = Integer.toString( pdu.getAction() );
            PEVENTTIME = Long.toString( pdu.getTime() );

            q = PEUID + "," + PESID + "," + TYPE + "," + SOURCE + "," + CREATEDEXECUTION + "," + PEVENTTYPE + "," + PEVENTTIME;

//            loggingError( "[PLAN_ELEMENT] " + q );

//            loggingError( E.toString() );
        }

        // Table : PROCESSED_BY (for Task procecessed by this expansion)
        UIDStringPDU uid2 = (UIDStringPDU) pdu.getParentTask();

        try
        {
            ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
            ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
            ProcessedByStmt.setLong( 3, uid2.getId() );             // TASKUID
            ProcessedByStmt.setString( 4, uid2.getOwner() );        // TASKSID
            ProcessedByStmt.setInt( 5, 1 );                         // PROCESSED
            ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
            ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
            ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "Error in Expansion Pby");
//            loggingError( E.toString() );
        }

        // Table : PROCESSED_BY (for the tasks generated by expansion)

        int asize = pdu.getNumTasks();

        for ( i = 0 ; i < asize ; i++ )
        {

            uid2 = (UIDStringPDU) pdu.getTask( i );

            try
            {
                ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
                ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
                ProcessedByStmt.setLong( 3, uid2.getId() );             // TASKUID
                ProcessedByStmt.setString( 4, uid2.getOwner() );        // TASKSID
                ProcessedByStmt.setInt( 5, 0 );                         // PROCESSED
                ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
                ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
                ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
                ProcessedByStmt.executeUpdate();
            }
            catch ( SQLException E )
            {
                System.out.println( "SQLException: " + E.getMessage() );
                System.out.println( "SQLState:     " + E.getSQLState() );
                System.out.println( "VendorError:  " + E.getErrorCode() );
				System.out.println( "Processed by expansion" );
//                loggingError( E.toString() );
            }
        }
    }

    public void storeAggregationPDUIntoDb( AggregationPDU pdu ) throws RuntimeException
    {

        String q, t;
        String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE, CREATEDEXECUTION;  // for PLAN_ELEMENT
        String PESID ,TASKUID ,TASKSID ,PROCESSED;       // for PROCESSED_BY

        int i;

        UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

        // PLAN_ELEMENT
        try
        {
            PlanElementStmt.setLong( 1, uid1.getId() );              // PEUID
            PlanElementStmt.setString( 2, uid1.getOwner() );         // PESID
            PlanElementStmt.setInt( 3, 3 );                         // TYPE   => AGGREGATION is 3.
            PlanElementStmt.setString( 4, pdu.getSource() );        // SOURCE
            PlanElementStmt.setLong( 5, pdu.getExecutionTime() );   // CREATEDEXECUTION
            PlanElementStmt.setInt( 6, pdu.getAction() );           // PEVENTTYPE
            PlanElementStmt.setLong( 7, pdu.getTime() );            // PEVENTTIME
            PlanElementStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "PlanElementStmt Error");
            PEUID = Long.toString( uid1.getId() );
            PESID = "'" + uid1.getOwner() + "'";
            TYPE = "3";                                    // AGGREGATION is 3.
            SOURCE = "'" + pdu.getSource() + "'";
            CREATEDEXECUTION = Long.toString( pdu.getExecutionTime() );
            PEVENTTYPE = Integer.toString( pdu.getAction() );
            PEVENTTIME = Long.toString( pdu.getTime() );

            q = PEUID + "," + PESID + "," + TYPE + "," + SOURCE + "," + CREATEDEXECUTION + "," + PEVENTTYPE + "," + PEVENTTIME;

//            loggingError( "[PLAN_ELEMENT] " + q );
//            loggingError( E.toString() );
        }



        // Table : PROCESSED_BY (for the Task processed by this aggregation)

/*
      int asize = tlist.length;

      for (i=0;i<asize;i++) {
          tsize = tlist[i].length();

          TASKUID         = tlist[i].substring(tsize - 12);
          TASKSID         = (SIDTable.get(tlist[i].substring(0,tsize - 13))).toString();
          PROCESSED       = "1";

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED;
          InsertIntoDb(PROCESSED_BY,q);
      }
*/
        UIDStringPDU uid2 = (UIDStringPDU) pdu.getTask();

        try
        {
            ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
            ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
            ProcessedByStmt.setLong( 3, uid2.getId() );             // TASKUID
            ProcessedByStmt.setString( 4, uid2.getOwner() );        // TASKSID
            ProcessedByStmt.setInt( 5, 1 );                         // PROCESSED
            ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
            ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
            ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "Error in Processedby here" );
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
//            loggingError( E.toString() );
        }

        /**
         * If asset is an organizational asset, then this part is not needed.
         * TBD. Checking whether the asset is an organizational asset.
         */
        // PROCESSED_BY for newAllocTask

        uid2 = (UIDStringPDU) pdu.getCombinedTask();

        try
        {
            ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
            ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
            ProcessedByStmt.setLong( 3, uid2.getId() );             // TASKUID
            ProcessedByStmt.setString( 4, uid2.getOwner() );        // TASKSID
            ProcessedByStmt.setInt( 5, 0 );                         // PROCESSED
            ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
            ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
            ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "Error in Processedby combined" );
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "0";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
//            loggingError( E.toString() );
        }

//      loggingMessage(pdu.toString());
    }

    public void storeAllocationPDUIntoDb( AllocationPDU pdu ) throws RuntimeException
    {

        String q, t;
        String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE,CREATEDEXECUTION;  // for PLAN_ELEMENT
        String PESID ,TASKUID ,TASKSID ,PROCESSED;       // for PROCESSED_BY
        String ARUID, ARTYPE, SUCCESS, TOTALSCORE;       // for ALLOC_RESULT
        String ASSETUID, ASID, AEVENTTIME;                // for ALLOC_TAKS_ON
		UIDStringPDU uid3, uid4;

        int tsize;

        UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

        // PLAN_ELEMENT
        try
        {
            PlanElementStmt.setLong( 1, uid1.getId() );             // PEUID
            PlanElementStmt.setString( 2, uid1.getOwner() );        // PESID
            PlanElementStmt.setInt( 3, 1 );                         // TYPE   => ALLOCATION is 1.
            PlanElementStmt.setString( 4, pdu.getSource() );        // SOURCE
            PlanElementStmt.setLong( 5, pdu.getExecutionTime() );   // CREATEDEXECUTION
            PlanElementStmt.setInt( 6, pdu.getAction() );           // PEVENTTYPE
            PlanElementStmt.setLong( 7, pdu.getTime() );            // PEVENTTIME
            PlanElementStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "Error in PlanElement");
			
			/*
          PEUID            = Long.toString(uid.getId());
          PESID            = "'"+uid.getOwner()+"'";
          TYPE             = "1";                                    // Allocation is 1.
          SOURCE           = "'"+pdu.getSource()+"'";
          CREATEDEXECUTION = Long.toString(pdu.getExecutionTime());
          PEVENTTYPE       = Integer.toString(pdu.getAction());
          PEVENTTIME       = Long.toString(pdu.getTime());

          q = PEUID+","+ PESID+","+ TYPE+","+SOURCE+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PLAN_ELEMENT] "+q);
*/
//            loggingError( E.toString() );
        }

        // PROCESSED_BY for Task

        UIDStringPDU uid2 = (UIDStringPDU) pdu.getTask();     // to be allocated

        try
        {
            ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
            ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
            ProcessedByStmt.setLong( 3, uid2.getId() );             // TASKUID
            ProcessedByStmt.setString( 4, uid2.getOwner() );        // TASKSID
            ProcessedByStmt.setInt( 5, 1 );                         // PROCESSED
            ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
            ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
            ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "Error in ProcessedBy");
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
//            loggingError( E.toString() );
        }

        /**
         * If asset is an organizational asset, then this part is not needed.
         * TBD. Checking whether the asset is an organizational asset.
         */
        // PROCESSED_BY for newAllocTask

        if ( pdu.getAllocTask() != null )
        {

            uid3 = (UIDStringPDU) pdu.getAllocTask(); // generated by allocation

            try
            {
                ProcessedByStmt.setLong( 1, uid1.getId() );             // PEUID
                ProcessedByStmt.setString( 2, uid1.getOwner() );        // PESID
                ProcessedByStmt.setLong( 3, uid3.getId() );             // TASKUID
                ProcessedByStmt.setString( 4, uid3.getOwner() );        // TASKSID
                ProcessedByStmt.setInt( 5, 0 );                         // PROCESSED
                ProcessedByStmt.setLong( 6, pdu.getExecutionTime() );   // CREATEDEXECUTION
                ProcessedByStmt.setInt( 7, pdu.getAction() );           // PEVENTTYPE
                ProcessedByStmt.setLong( 8, pdu.getTime() );            // PEVENTTIME
                ProcessedByStmt.executeUpdate();
            }
            catch ( SQLException E )
            {
                System.out.println( "SQLException: " + E.getMessage() );
                System.out.println( "SQLState:     " + E.getSQLState() );
                System.out.println( "VendorError:  " + E.getErrorCode() );
				System.out.println( "In ProcessedBy Not Null");
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
//                loggingError( E.toString() );
            }
        }


        // ALLOC_TAKS_ON

        uid4 = (UIDStringPDU) pdu.getAsset();
/*
        try
        {
            AllocTaskOnStmt.setLong( 1, uid1.getId() );             // PEUID
            AllocTaskOnStmt.setLong( 2, uid4.getId() );             // ASSETUID
            AllocTaskOnStmt.setString( 3, uid1.getOwner() );        // PESID
            AllocTaskOnStmt.setString( 4, uid4.getOwner() );        // ASID
            AllocTaskOnStmt.setLong( 5, pdu.getExecutionTime() );   // CREATEDEXECUTION
            AllocTaskOnStmt.setInt( 6, pdu.getAction() );           // PEVENTTYPE
            AllocTaskOnStmt.setLong( 7, pdu.getTime() );            // PEVENTTIME
            AllocTaskOnStmt.executeUpdate();
        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "In AllocTaskOn");
			//            loggingError( E.toString() );
        }

*/
		// input data into ALLOCATION table
		
		String s = "INSERT INTO ALLOCATION ( PEUID, PESID, SOURCE, CREATEDEXECUTION, PEVENTTYPE, PEVENTTIME, " +
											" TASKUID, TASKSID, ASSETUID, ASSETSID ) " + 
											" VALUES ("+uid1.getId()+",'"+uid1.getOwner()+"','"+pdu.getSource()+"'," + 
											pdu.getExecutionTime()+","+ pdu.getAction()+","+pdu.getTime()+"," + 
											uid2.getId()+",'"+uid2.getOwner()+"'," +
											uid4.getId()+",'"+uid4.getOwner()+"')";
		try
        {

			PdStatement.executeUpdate(s);

        }
        catch ( SQLException E )
        {
            System.out.println( "SQLException: " + E.getMessage() );
            System.out.println( "SQLState:     " + E.getSQLState() );
            System.out.println( "VendorError:  " + E.getErrorCode() );
			System.out.println( "In AllocTaskOn");
			//            loggingError( E.toString() );
        }
		
    }

    public void createTable(Statement theStatement) {
       try {
         // Create the table for all EventPDUs.  Create an index on the
         // ( UIDNAME, UID ) combination.
            String query;
            query = "CREATE TABLE PLAN_ELEMENT( PEUID BIGINT NOT NULL, " +
                    " PESID VARCHAR(20) NOT NULL, TYPE INT, " +
                    " SOURCE VARCHAR(20), CREATEDEXECUTION BIGINT, " +
                    " PEVENTTYPE INT, PEVENTTIME BIGINT NOT NULL)" ;
            myStatement.executeQuery( query ) ;
            query = "CREATE TABLE PROCESSED_BY( PEUID BIGINT NOT NULL, " +
                    " PESID VARCHAR(20) NOT NULL, TASKUID BIGINT NOT NULL, " +
                    " TASKSID VARCHAR(20) NOT NULL, PROCESSED TINYINT, CREATEDEXECUTION BIGINT, " +
                    " PEVENTTYPE INT, PEVENTTIME BIGINT NOT NULL)";
			//, FOREIGN KEY (PEUID) REFERENCES PLAN_ELEMENT, " +
				//	"FOREIGN KEY (TASKSUID) REFERENCES TASK)";
			myStatement.executeQuery( query ) ;
/*			query = "CREATE TABLE ALLOC_TASK_ON( PEUID BIGINT NOT NULL, " +
                    " ASSETUID BIGINT NOT NULL, PESID VARCHAR(20) NOT NULL, " +
                    " ASID VARCHAR(20) NOT NULL, CREATEDEXECUTION BIGINT, " +
                    " PEVENTTYPE INT NOT NULL, PEVENTTIME BIGINT NOT NULL) " ;
				   // " FOREIGN KEY (ASSETUID) REFERENCES ASSET,FOREIGN KEY (PEVENTTYPE) REFERENCES PLAN_ELEMENT ON DELETE CASCADE ON UPDATE CASCADE, " +
					//" FOREIGN KEY (PEVENTTIME) REFERENCES PLAN_ELEMENT ON DELETE CASCADE ON UPDATE CASCADE)";

			myStatement.executeQuery( query ) ; */
           // myStatement.executeQuery( query ) ;
           // query = "CREATE INDEX EVENTSBYUID ON EVENTPDU ( OWNER( 16 ), ID )" ;
           // myStatement.executeQuery( query ) ;
           // query = "CREATE INDEX EVENTSBYTYPE ON EVENTPDU ( TYPE )" ;
           // myStatement.executeQuery( query ) ;
          //  query = "CREATE INDEX EVENTSBYID ON EVENTPDU ( ID )" ;
          //  myStatement.executeQuery( query ) ;
		  /* Allocation table */
            query = "CREATE TABLE ALLOCATION( PEUID BIGINT NOT NULL, " +
											" PESID VARCHAR(20) NOT NULL, " +
											" SOURCE VARCHAR(20), " + 
											" CREATEDEXECUTION BIGINT, " +
											" PEVENTTYPE INT, " + 
											" PEVENTTIME BIGINT NOT NULL, " +
											" TASKUID BIGINT, " +
											" TASKSID VARCHAR(20), " +
											" ASSETUID BIGINT, " +
											" ASSETSID VARCHAR(20))" ;
            myStatement.executeQuery( query ) ;
        }
        catch ( SQLException e ) {
            printSQLException( e );
            e.printStackTrace();
        }
    }    
    
	
    public void loadPreparedStatements( Connection conn ){
      try{
           PlanElementStmt = conn.prepareStatement(
                    "INSERT INTO PLAN_ELEMENT VALUES( ?, ?, ?, ?, ?, ?, ?)" );
		   ProcessedByStmt = conn.prepareStatement(
                    "INSERT INTO PROCESSED_BY VALUES( ?, ?, ?, ?, ?, ?, ?, ?)" );
		   AllocTaskOnStmt = conn.prepareStatement(
                    "INSERT INTO ALLOC_TASK_ON VALUES( ?, ?, ?, ?, ?, ?, ?)" );
        }
        catch ( SQLException ex )
        {
           printSQLException( ex );
        }
   }
}