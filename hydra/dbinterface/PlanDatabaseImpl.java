package org.dbinterface;

import org.dbinterface.*;
import org.hydra.pdu.*;
import org.hydra.server.* ;
import org.cougaar.domain.planning.ldm.plan.AspectValue ;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

import org.gjt.mm.mysql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Title:        DB Interface
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PSU
 * @author Yunho Hong
 * @version 1.0
 */

public class PlanDatabaseImpl implements PlanDatabase {

  /** Logging */
  private java.io.BufferedWriter outFile, messageFile, comparisonFile, comparisonFile2, comparisonFile3, comparisonFile4;

  private java.io.BufferedWriter dballocation, dbtask, dbasset, dbexpansion, dballocResult, dbsubtask, dbav;
  private java.io.BufferedWriter allocation, task, asset, expansion, allocResult, subtask, av;

  private Hashtable SIDTable, ClusterTable;

  /** JDBC Connection */
  java.sql.Connection con;

  /** Statement */
  java.sql.Statement PdStatement;
  java.sql.Statement PdStatement2;

  java.sql.PreparedStatement EventPDUStmt;
  java.sql.PreparedStatement PlanElementStmt;
  java.sql.PreparedStatement TaskStmt;
  java.sql.PreparedStatement AssetStmt;
  java.sql.PreparedStatement AllocResultStmt;
  java.sql.PreparedStatement ProcessedByStmt;
  java.sql.PreparedStatement ScoreStmt;
  java.sql.PreparedStatement AllocTaskOnStmt;

  java.sql.PreparedStatement TaskRetreiveStmt;
  java.sql.PreparedStatement AssetRetreiveStmt;
  java.sql.PreparedStatement PlanElementRetreiveStmt;
  java.sql.PreparedStatement ProcessedbyRetreiveStmt;
  java.sql.PreparedStatement AllocTaskOnRetreiveStmt;
  java.sql.PreparedStatement AllocResultRetreiveStmt;
  java.sql.PreparedStatement ScoreRetreiveStmt;


  /** Constructor. The specific database setting is loaded from XML file */
  public void PlanDatabaseImpl(String XMLfile){

  }

  public void PlanDatabaseImpl() {


  }

  public void closeConnection() {
      try {
      con.close() ;
      }
      catch ( Exception e ) {
        e.printStackTrace() ;
      }
  }

  public void startLogging() {

     try
      {
          outFile = new java.io.BufferedWriter ( new java.io.FileWriter ("PlanDB.txt", true ));
          messageFile = new java.io.BufferedWriter ( new java.io.FileWriter ("Messages.txt", true ));

/* 
          dballocation = new java.io.BufferedWriter ( new java.io.FileWriter ("allocationdb.txt", true ));
          dbtask = new java.io.BufferedWriter ( new java.io.FileWriter ("taskdb.txt", true ));
          dbasset = new java.io.BufferedWriter ( new java.io.FileWriter ("assetdb.txt", true ));
          dbexpansion = new java.io.BufferedWriter ( new java.io.FileWriter ("expansiondb.txt", true ));
          dballocResult = new java.io.BufferedWriter ( new java.io.FileWriter ("allocResultdb.txt", true ));
          dbsubtask = new java.io.BufferedWriter ( new java.io.FileWriter ("subtaskdb.txt", true ));
          dbav = new java.io.BufferedWriter ( new java.io.FileWriter ("avdb.txt", true ));

          allocation = new java.io.BufferedWriter ( new java.io.FileWriter ("allocation.txt", true ));
          task = new java.io.BufferedWriter ( new java.io.FileWriter ("task.txt", true ));
          asset = new java.io.BufferedWriter ( new java.io.FileWriter ("asset.txt", true ));
          expansion = new java.io.BufferedWriter ( new java.io.FileWriter ("expansion.txt", true ));
          allocResult = new java.io.BufferedWriter ( new java.io.FileWriter ("allocResult.txt", true ));
          subtask = new java.io.BufferedWriter ( new java.io.FileWriter ("subtask.txt", true ));
          av = new java.io.BufferedWriter ( new java.io.FileWriter ("av.txt", true ));
 */
//          System.out.println("PlanDB.txt");
//          System.out.println("Messages.txt");

      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write file, io error" );
      }
  }

  public void showsTables() {}

  public void makeSIDTable(String iniFile) {

    String l, t, q;
    int k=0;

    Integer i = new Integer(k);
    SIDTable = new Hashtable();
    ClusterTable = new Hashtable();

    try
    {
         java.io.BufferedReader inFile = new java.io.BufferedReader ( new java.io.FileReader(iniFile));

         while ((l = inFile.readLine())!= null ) {
            t=l.substring(10);

            SIDTable.put(t.trim(),i.toString());
            ClusterTable.put(i,t.trim());

            q = "'" + t.trim()+ "'," + i.toString();

//            System.out.println(q);

            InsertIntoDb(SIDTABLE,q);
            k++;
            i=new Integer(k);
         }
    }
    catch (java.io.IOException ioexc)
    {
          System.err.println ("can't read file, io error" );
    }

    SIDTable.toString();
  }

  public void establishConnection(){

    // Open DB
    try {
      Class.forName("org.gjt.mm.mysql.Driver");

    } catch(java.lang.ClassNotFoundException e) {
      System.err.print("(EstablishConnetction) ClassNotFoundException:");
      System.err.println(e.getMessage());
    }

    String dbPath = ServerApp.instance().getDbPath() ;

    if ( dbPath == null ) {
        System.out.println( "dbPath variable not set. Could not establish connection to database." );
        return ;
    }

    System.out.println( "PlanDatabase:: Connecting to dababase " + dbPath ) ;
    try {
      con = DriverManager.getConnection(dbPath);

      PdStatement = con.createStatement();
      PdStatement2 = con.createStatement();


      // Storing
//      EventPDUStmt = con.prepareStatement(
//               "INSERT INTO EVENTPDU VALUES( ?, ?, ?, ?, ?, ?)");

      EventPDUStmt = con.prepareStatement(
               "INSERT INTO EVENTPDU VALUES( ?, ?, ?, ?, ?, ?, ?)");

      PlanElementStmt = con.prepareStatement(
               "INSERT INTO PLAN_ELEMENT VALUES( ?, ?, ?, ?, ?, ?, ?)");
      TaskStmt  = con.prepareStatement(
               "INSERT INTO TASK VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      AssetStmt = con.prepareStatement(
               "INSERT INTO ASSET VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      AllocResultStmt  = con.prepareStatement(
               "INSERT INTO ALLOC_RESULT VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      ProcessedByStmt  = con.prepareStatement(
               "INSERT INTO PROCESSED_BY VALUES( ?, ?, ?, ?, ?, ?, ?, ?)");
      ScoreStmt  = con.prepareStatement(
               "INSERT INTO SCORE VALUES( ?, ?, ?, ?, ?, ?, ?, ?)");
      AllocTaskOnStmt  = con.prepareStatement(
               "INSERT INTO ALLOC_TASK_ON VALUES( ?, ?, ?, ?, ?, ?, ?)");

      // Retreival
      TaskRetreiveStmt = con.prepareStatement(
               "SELECT * FROM TASK WHERE TASKUID = ? AND TASKSID = ? AND TEVENTTIME = ? AND TEVENTTYPE = ? ");

      AssetRetreiveStmt = con.prepareStatement(
               "SELECT * FROM ASSET WHERE ASSETUID = ? AND ASID = ? AND AEVENTTIME = ? AND AEVENTTYPE = ? ") ;

      PlanElementRetreiveStmt = con.prepareStatement(
               "SELECT * FROM PLAN_ELEMENT WHERE PEUID = ? AND PESID = ? AND TYPE = ? AND PEVENTTIME = ? AND PEVENTTYPE = ? ");

      ProcessedbyRetreiveStmt = con.prepareStatement(
              "SELECT * FROM PROCESSED_BY WHERE PEUID = ? AND PESID = ? AND PROCESSED = ? AND PEVENTTIME = ? AND PEVENTTYPE = ? ");

      AllocTaskOnRetreiveStmt = con.prepareStatement(
              "SELECT * FROM ALLOC_TASK_ON WHERE PEUID = ? AND PESID = ? AND PEVENTTIME = ? AND PEVENTTYPE = ? ");

      AllocResultRetreiveStmt = con.prepareStatement(
              "SELECT * FROM ALLOC_RESULT WHERE PEUID = ? AND PESID = ? AND PEVENTTIME = ? AND PEVENTTYPE = ? AND ARTYPE = ? ");

      ScoreRetreiveStmt = con.prepareStatement(
              "SELECT * FROM SCORE WHERE PEUID = ? AND PESID = ? AND PEVENTTIME = ? AND PEVENTTYPE = ? AND ARTYPE = ? ");

    } catch(SQLException ex) {

      System.err.println("(EstablishConnetction) -----SQLException-----");
      System.err.println("SQLState:  " + ex.getSQLState());
      System.err.println("Message:  " + ex.getMessage());
      System.err.println("Vendor:  " + ex.getErrorCode());
    } 

  }

  public void createDatabase(String DatabaseName){
        try {

           PdStatement.executeUpdate(  "CREATE DATABASE " + DatabaseName);

        } catch (SQLException E) {
        	System.out.println("SQLException: " + E.getMessage());
                System.out.println("SQLState:     " + E.getSQLState());
                System.out.println("VendorError:  " + E.getErrorCode());
        }
  }

  public void useDataBase(String DatabaseName) {

        try {
           PdStatement.executeQuery("USE " + DatabaseName);
        } catch (SQLException E) {
        	System.out.println("SQLException: " + E.getMessage());
                System.out.println("SQLState:     " + E.getSQLState());
                System.out.println("VendorError:  " + E.getErrorCode());
        }
  }
  public void createTables(String DbdefFile){

        int i,s,e;

        try {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();

//             File f = new File(DbdefFile);
             Class c = this.getClass() ;
             InputStream is = c.getResourceAsStream( "defs/" + DbdefFile ) ;
             Document document = builder.parse( is );

//             Document document = builder.parse(DbdefFile);
//                   System.out.println("C-4");

             NodeList list = document.getElementsByTagName("CREATETABLE");
//                   System.out.println("C-5");

             int sizeOfList = list.getLength();
             for(i=0;i<sizeOfList;i++) {
                Node thisNode = list.item(i);
                Node defNode = thisNode.getFirstChild();

                if (defNode instanceof org.w3c.dom.Text) {

//                    System.out.println("success");

                }

				String tabledef = defNode.toString();

                s = tabledef.indexOf("CREATE");
                e = tabledef.lastIndexOf("]");

//                System.out.println(tabledef.substring(s,e));

                PdStatement.executeUpdate(tabledef.substring(s,e));
            }

        }
	catch (Exception E) {

              System.out.println(E.toString());
              if (E instanceof SQLException ) {
                SQLException SQLE = (SQLException) E;
        	System.out.println("SQLException: " + SQLE.getMessage());
                System.out.println("SQLState:     " + SQLE.getSQLState());
                System.out.println("VendorError:  " + SQLE.getErrorCode());
              }
       }

  }

  public void inputTestData(){

  }


  public void InsertIntoDb(String table, String values) {
    try {
      PdStatement.executeUpdate("INSERT INTO "+ table + " VALUES ("+ values +")");
    } catch (SQLException E) {
        System.out.println("SQLException: " + E.getMessage());
        System.out.println("SQLState:     " + E.getSQLState());
        System.out.println("VendorError:  " + E.getErrorCode());
        loggingError("["+table+"] "+values);
        loggingError(E.toString());
    }
  }


  /** Data search function */

  /* Return the lists of Parent Task UID at specific time */

  public Vector getParentTask(int TaskUID, int SID, long Time) {

    Vector v= new Vector();

    return v;
  }

  /* Return the lists of Child Task UID at specific time */

  public Vector getChildTask(int TaskUID, int SID, long Time){
    Vector v= new Vector();

    return v;
  }

  /* Return AssetUID where Task is allocated at specific time */

  public String      getAllocatedAsset(int TaskUID, int SID, long Time){

      String a = new String();

      return a;
  }

  private void loggingDB(String e, int i) {
      try {

          if (i == EventPDU.TYPE_TASK) {
              dbtask.write(e);  dbtask.newLine();  dbtask.flush();
          } else if (i == EventPDU.TYPE_ASSET) {
              dbasset.write(e);  dbasset.newLine();  dbasset.flush();
          }else if (i == EventPDU.TYPE_ALLOCATION || i == EventPDU.TYPE_AGGREGATION) {
              dballocation.write(e);  dballocation.newLine();  dballocation.flush();
          }else if (i == EventPDU.TYPE_EXPANSION) {
              dbexpansion.write(e);  dbexpansion.newLine();  dbexpansion.flush();
          }else if (i == EventPDU.TYPE_ALLOCATION_RESULT) {
              dballocResult.write(e);  dballocResult.newLine();  dballocResult.flush();
          }else if (i == 10) {
              dbsubtask.write(e);  dbsubtask.newLine();  dbsubtask.flush();
          }else if (i == 11) {
              dbav.write(e);  dbav.newLine();  dbav.flush();
          }

      } catch (java.io.IOException ioexc) {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }

  private void loggingMem(String e, int i) {
      try {

          if (i == EventPDU.TYPE_TASK) {
              task.write(e);  task.newLine();  task.flush();
          } else if (i == EventPDU.TYPE_ASSET) {
              asset.write(e);  asset.newLine();  asset.flush();
          }else if (i == EventPDU.TYPE_ALLOCATION || i == EventPDU.TYPE_AGGREGATION) {
              allocation.write(e);  allocation.newLine();  allocation.flush();
          }else if (i == EventPDU.TYPE_EXPANSION) {
              expansion.write(e);  expansion.newLine();  expansion.flush();
          }else if (i == EventPDU.TYPE_ALLOCATION_RESULT) {
              allocResult.write(e);  allocResult.newLine();  allocResult.flush();
          }else if (i == 10) {
              subtask.write(e);  subtask.newLine();  subtask.flush();
          }else if (i == 11) {
              av.write(e);  av.newLine();  av.flush();
          }

      } catch (java.io.IOException ioexc) {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }
  private void loggingComparison(String e) {
      try
      {
          comparisonFile.write(e);
          comparisonFile.newLine();
          comparisonFile.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }

  private void loggingComparison2(String e) {
      try
      {
          comparisonFile2.write(e);
          comparisonFile2.newLine();
          comparisonFile2.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }

  private void loggingComparison3(String e) {
      try
      {
          comparisonFile3.write(e);
          comparisonFile3.newLine();
          comparisonFile3.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }

 private void loggingComparison4(String e) {
      try
      {
          comparisonFile4.write(e);
          comparisonFile4.newLine();
          comparisonFile4.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
  }

  private void loggingError(String e) {
      try
      {
          outFile.write(e);
          outFile.newLine();
          outFile.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write PlanDBLog file, io error" );
      }
   }

  private void loggingMessage(String e) {
      try {
          messageFile.write(e);
          messageFile.newLine();
          messageFile.flush();
      }
      catch (java.io.IOException ioexc)
      {
          System.err.println ("can't write MessageLog file, io error" );
      }
   }

	// Newly added functions for hydra, Sept. 10, 2001

  public void storeEventPDUIntoDb(EventPDU pdu){


	if (pdu instanceof TaskPDU){

          storePDUByTime(pdu,EventPDU.TYPE_TASK);
//          storeTaskPDUIntoDb((TaskPDU)pdu);

	}else if ( pdu instanceof ExpansionPDU){

          storePDUByTime(pdu,EventPDU.TYPE_EXPANSION);
//          storeExpansionPDUIntoDb((ExpansionPDU) pdu);
	}else if ( pdu instanceof AllocationPDU){

          storePDUByTime(pdu,EventPDU.TYPE_ALLOCATION);
//          storeAllocationPDUIntoDb((AllocationPDU)pdu);
	}else if ( pdu instanceof AggregationPDU){

          storePDUByTime(pdu,EventPDU.TYPE_AGGREGATION);
//          storeAggregationPDUIntoDb((AggregationPDU) pdu);
	}else if ( pdu instanceof AssetPDU){

          storePDUByTime(pdu,EventPDU.TYPE_ASSET);
//          storeAssetPDUIntoDb((AssetPDU) pdu);
	}else if ( pdu instanceof AllocationResultPDU){

          storePDUByTime(pdu,EventPDU.TYPE_ALLOCATION_RESULT);
//          storeAllocationResultPDUIntoDb((AllocationResultPDU) pdu);
	}
  }


  public void storePDUByTime(EventPDU epdu, int TYPE){

      String q, t;
      String TIME, SID, UID, ARTYPE, EVENTTYPE;
	byte buf[];
      int artype;
      UIDStringPDU uid;

      if (TYPE == EventPDU.TYPE_ALLOCATION_RESULT) {
        AllocationResultPDU ARPDU = (AllocationResultPDU) epdu;
        uid = (UIDStringPDU) ARPDU.getPlanElementUID();
        artype = ARPDU.getARType();
      } else {
        UniqueObjectPDU UOPDU = (UniqueObjectPDU) epdu;
        uid = (UIDStringPDU) UOPDU.getUID();
        artype = 0;
      }

       // field
      try {
            EventPDUStmt.setLong(1, uid.getId());      // UID
            EventPDUStmt.setString(2, uid.getOwner()); // SID
            EventPDUStmt.setLong(3, epdu.getTime());   // TIME
            EventPDUStmt.setInt(4, TYPE);              // TYPE
            EventPDUStmt.setInt(5, artype);            // ARTYPE
	      EventPDUStmt.setInt(6, epdu.getAction());  // EVENTTYPE

		ByteArrayOutputStream ostream = new ByteArrayOutputStream (); 
		ObjectOutputStream p = new ObjectOutputStream(ostream); 


		p.writeObject(epdu); 
		p.flush();

		buf = ostream.toByteArray();

		ByteArrayInputStream bis = new ByteArrayInputStream(buf);

		EventPDUStmt.setBinaryStream(7, bis, bis.available()); // PDU

//		bis.close();
//		ostream.close(); 
//		p.close();

            EventPDUStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());

          UID   = Long.toString(uid.getId());
          SID   = "'"+uid.getOwner()+"'";
          TIME  = Long.toString(epdu.getTime());
          ARTYPE = Integer.toString(artype);
          EVENTTYPE = Integer.toString(epdu.getAction());

          q = UID+","+SID+","+TIME+","+TYPE+","+ARTYPE+","+EVENTTYPE;

          loggingError("[EVENTPDU"+q);
          loggingError(E.toString());
      } catch (IOException ioe) {


	}

//      InsertIntoDb(EVENTPDU,q);		  // EventPDU list by time sequence

//      loggingMessage(epdu.toString());
  }

  public void storeTaskPDUIntoDb(TaskPDU pdu){

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
      if (pdu instanceof MPTaskPDU) {  MPTASK = 1; } // This is MPTask.
      else                          {  MPTASK = 0; } // This is not MPTask.

//      SOURCE           = "'"+pdu.getSource()+"'";

      UIDStringPDU puid = (UIDStringPDU) pdu.getParentTask();

//      TEVENTTYPE       = Integer.toString(pdu.getAction());
//      TEVENTTIME       = Long.toString(pdu.getTime());

//      q = TASKUID+","+SID+","+CREATEDEXECUTION+","+VERB+","+MPTASK+","+SOURCE+","+PARENTID+","+PARENTSID+","+TEVENTTYPE+","+TEVENTTIME;

//      InsertIntoDb(TASK,q);

      try {

            TaskStmt.setLong(1, uid.getId());                         // TASKUID
            TaskStmt.setString(2, uid.getOwner());                    // SID
            TaskStmt.setLong(3, pdu.getExecutionTime());              // CREATEDEXECUTION
            TaskStmt.setString(4, (pdu.getTaskVerb()).toString());    // VERB

            if (pdu instanceof MPTaskPDU) {  TaskStmt.setInt(5, 1); } // This is MPTask.
            else                          {  TaskStmt.setInt(5, 0); } // This is not MPTask.

            TaskStmt.setString(6, pdu.getSource());                   // SOURCE
            if (puid != null) {
              TaskStmt.setLong(7, puid.getId());                      // PARENTID
              TaskStmt.setString(8, puid.getOwner());                 // PARENTSID
            } else {
              TaskStmt.setLong(7, 0);                                 // PARENTID
              TaskStmt.setString(8, "''");                            // PARENTSID
            }
            TaskStmt.setInt(9, pdu.getAction());                      // TEVENTTYPE
            TaskStmt.setLong(10, pdu.getTime());                      // TEVENTTIME
            TaskStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println(pdu.toString());
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
          loggingError(E.toString());
      }



//      loggingMessage(pdu.toString());
  }


  public void storeAssetPDUIntoDb(AssetPDU pdu){

      String q, t;
      String ASSETUID, SID, ITEMID, TYPEID, AEVENTTYPE, AEVENTTIME, SOURCE, ASSETCLASS, CREATEDEXECUTION;

      int tsize;

      UIDStringPDU uid = (UIDStringPDU) pdu.getUID();

      // field
      try {
            AssetStmt.setLong(1, uid.getId());                           // ASSETUID
            AssetStmt.setString(2, uid.getOwner());                      // SID
            AssetStmt.setString(3, pdu.getItemId());                     // ITEMID
            AssetStmt.setString(4, pdu.getAssetTypeId());                // TYPEID
            AssetStmt.setString(5, pdu.getSource());                     // SOURCE
            AssetStmt.setString(6, (pdu.getAssetClass()).toString());    // ASSETCLASS
            AssetStmt.setLong(7, pdu.getExecutionTime());                // CREATEDEXECUTION
            AssetStmt.setInt(8, pdu.getAction());                        // AEVENTTYPE
            AssetStmt.setLong(9, pdu.getTime());                         // AEVENTTIME
            AssetStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println(pdu.toString());
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
          loggingError(E.toString());
      }


//      loggingMessage(pdu.toString());
  }

  public void storeExpansionPDUIntoDb(ExpansionPDU pdu)throws RuntimeException {

      String q, t;
      String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE, CREATEDEXECUTION;  // for PLAN_ELEMENT
      String PESID ,TASKUID ,TASKSID ,PROCESSED ;       // for PROCESSED_BY

      int tsize, i;
      UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

      // PLAN_ELEMENT
      try {
            PlanElementStmt.setLong(1, uid1.getId());             // PEUID
            PlanElementStmt.setString(2, uid1.getOwner());        // PESID
            PlanElementStmt.setInt(3, 2);                         // TYPE   => // EXPANSION == 2.
            PlanElementStmt.setString(4, pdu.getSource());        // SOURCE
            PlanElementStmt.setLong(5, pdu.getExecutionTime());   // CREATEDEXECUTION
            PlanElementStmt.setInt(6, pdu.getAction());           // PEVENTTYPE
            PlanElementStmt.setLong(7, pdu.getTime());            // PEVENTTIME
            PlanElementStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());

          PEUID            = Long.toString(uid1.getId());
          PESID            = "'"+uid1.getOwner()+"'";
          TYPE             = "2";                           // EXPANSION == 2.
          SOURCE           = "'"+pdu.getSource()+"'";
          CREATEDEXECUTION = Long.toString(pdu.getExecutionTime());
          PEVENTTYPE       = Integer.toString(pdu.getAction());
          PEVENTTIME       = Long.toString(pdu.getTime());

          q = PEUID+","+ PESID+","+ TYPE+","+SOURCE+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PLAN_ELEMENT] "+q);

          loggingError(E.toString());
      }

      // Table : PROCESSED_BY (for Task procecessed by this expansion)
      UIDStringPDU uid2 = (UIDStringPDU) pdu.getParentTask();

      try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 1);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
          loggingError(E.toString());
      }

      // Table : PROCESSED_BY (for the tasks generated by expansion)

      int asize = pdu.getNumTasks();

      for (i=0;i<asize;i++) {

          uid2 = (UIDStringPDU) pdu.getTask(i);

          try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 0);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
          } catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
            System.out.println("SQLState:     " + E.getSQLState());
            System.out.println("VendorError:  " + E.getErrorCode());
          	loggingError(E.toString());
        }
      }
  }

  public void storeAggregationPDUIntoDb(AggregationPDU pdu)  throws RuntimeException {

      String q, t;
      String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE, CREATEDEXECUTION;  // for PLAN_ELEMENT
      String PESID ,TASKUID ,TASKSID ,PROCESSED ;       // for PROCESSED_BY

      int i;

      UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

      // PLAN_ELEMENT
      try {
            PlanElementStmt.setLong(1, uid1.getId());              // PEUID
            PlanElementStmt.setString(2, uid1.getOwner());         // PESID
            PlanElementStmt.setInt(3, 3);                         // TYPE   => AGGREGATION is 3.
            PlanElementStmt.setString(4, pdu.getSource());        // SOURCE
            PlanElementStmt.setLong(5, pdu.getExecutionTime());   // CREATEDEXECUTION
            PlanElementStmt.setInt(6, pdu.getAction());           // PEVENTTYPE
            PlanElementStmt.setLong(7, pdu.getTime());            // PEVENTTIME
            PlanElementStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());

          PEUID            = Long.toString(uid1.getId());
          PESID            = "'"+uid1.getOwner()+"'";
          TYPE             = "3";                                    // AGGREGATION is 3.
          SOURCE           = "'"+pdu.getSource()+"'";
          CREATEDEXECUTION = Long.toString(pdu.getExecutionTime());
          PEVENTTYPE       = Integer.toString(pdu.getAction());
          PEVENTTIME       = Long.toString(pdu.getTime());

          q = PEUID+","+ PESID+","+ TYPE+","+SOURCE+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PLAN_ELEMENT] "+q);
          loggingError(E.toString());
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

      try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 1);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
          loggingError(E.toString());
      }

      /**
       * If asset is an organizational asset, then this part is not needed.
       * TBD. Checking whether the asset is an organizational asset.
       */
      // PROCESSED_BY for newAllocTask

      uid2 = (UIDStringPDU) pdu.getCombinedTask();

      try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 0);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "0";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
          loggingError(E.toString());
      }

//      loggingMessage(pdu.toString());
  }

  public void  storeAllocationPDUIntoDb(AllocationPDU pdu ) throws RuntimeException {

      String q, t;
      String PEUID, TYPE, PEVENTTYPE, PEVENTTIME, SOURCE,CREATEDEXECUTION;  // for PLAN_ELEMENT
      String PESID ,TASKUID ,TASKSID ,PROCESSED ;       // for PROCESSED_BY
      String ARUID, ARTYPE, SUCCESS, TOTALSCORE ;       // for ALLOC_RESULT
      String ASSETUID, ASID, AEVENTTIME;                // for ALLOC_TAKS_ON

      int tsize;

      UIDStringPDU uid1 = (UIDStringPDU) pdu.getUID();

      // PLAN_ELEMENT
      try {
            PlanElementStmt.setLong(1, uid1.getId());             // PEUID
            PlanElementStmt.setString(2, uid1.getOwner());        // PESID
            PlanElementStmt.setInt(3, 1);                         // TYPE   => ALLOCATION is 1.
            PlanElementStmt.setString(4, pdu.getSource());        // SOURCE
            PlanElementStmt.setLong(5, pdu.getExecutionTime());   // CREATEDEXECUTION
            PlanElementStmt.setInt(6, pdu.getAction());           // PEVENTTYPE
            PlanElementStmt.setLong(7, pdu.getTime());            // PEVENTTIME
            PlanElementStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
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
          loggingError(E.toString());
      }

      // PROCESSED_BY for Task

      UIDStringPDU uid2 = (UIDStringPDU) pdu.getTask();     // to be allocated

      try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 1);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
      } catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
          loggingError(E.toString());
      }

      /**
       * If asset is an organizational asset, then this part is not needed.
       * TBD. Checking whether the asset is an organizational asset.
       */
      // PROCESSED_BY for newAllocTask

      if (pdu.getAllocTask() != null) {

          uid2 = (UIDStringPDU) pdu.getAllocTask(); // generated by allocation

          try {
            ProcessedByStmt.setLong(1, uid1.getId());             // PEUID
            ProcessedByStmt.setString(2, uid1.getOwner());        // PESID
            ProcessedByStmt.setLong(3, uid2.getId());             // TASKUID
            ProcessedByStmt.setString(4, uid2.getOwner());        // TASKSID
            ProcessedByStmt.setInt(5, 0);                         // PROCESSED
            ProcessedByStmt.setLong(6, pdu.getExecutionTime());   // CREATEDEXECUTION
            ProcessedByStmt.setInt(7, pdu.getAction());           // PEVENTTYPE
            ProcessedByStmt.setLong(8, pdu.getTime());            // PEVENTTIME
            ProcessedByStmt.executeUpdate();
          } catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
            System.out.println("SQLState:     " + E.getSQLState());
            System.out.println("VendorError:  " + E.getErrorCode());
/*
          TASKUID         = Long.toString(uid.getId());
          TASKSID         = "'"+uid.getOwner()+"'";
          PROCESSED       = "1";
          PEVENTTYPE       = Integer.toString(pdu.getAction());		// TBD.
          PEVENTTIME       = Long.toString(pdu.getTime());

          q =  PEUID+","+ PESID+","+ TASKUID+","+ TASKSID+","+ PROCESSED+","+ CREATEDEXECUTION+","+ PEVENTTYPE+","+ PEVENTTIME;

          loggingError("[PROCESSED_BY] "+q);
*/
            loggingError(E.toString());
          }
      }


      // ALLOC_TAKS_ON

      uid2 = (UIDStringPDU) pdu.getAsset();

      try {
            AllocTaskOnStmt.setLong(1, uid1.getId());             // PEUID
            AllocTaskOnStmt.setLong(2, uid2.getId());             // ASSETUID
            AllocTaskOnStmt.setString(3, uid1.getOwner());        // PESID
            AllocTaskOnStmt.setString(4, uid2.getOwner());        // ASID
            AllocTaskOnStmt.setLong(5, pdu.getExecutionTime());   // CREATEDEXECUTION
            AllocTaskOnStmt.setInt(6, pdu.getAction());           // PEVENTTYPE
            AllocTaskOnStmt.setLong(7, pdu.getTime());            // PEVENTTIME
            AllocTaskOnStmt.executeUpdate();
      } catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
            System.out.println("SQLState:     " + E.getSQLState());
            System.out.println("VendorError:  " + E.getErrorCode());
            loggingError(E.toString());
          }
//      loggingMessage(pdu.toString());
  }

  public void storeAllocationResultPDUIntoDb(AllocationResultPDU pdu) {

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
      try {
            AllocResultStmt.setLong(1, uid.getId());              // PEUID
            AllocResultStmt.setString(2, uid.getOwner());         // PESID
            AllocResultStmt.setString(3, pdu.getSource());        // SOURCE
            AllocResultStmt.setLong(4, pdu.getExecutionTime());   // CREATEDEXECUTION
            AllocResultStmt.setInt(5, pdu.getAction());           // PEVENTTYPE
            AllocResultStmt.setLong(6, pdu.getTime());            // PEVENTTIME
            AllocResultStmt.setInt(7, pdu.getARType());           // ARTYPE
            AllocResultStmt.setInt(8, pdu.getSuccess());          // SUCCESS
            AllocResultStmt.setFloat(9, 1);                       // TOTALSCORE
            AllocResultStmt.setFloat(10, pdu.getConfidence());    // CONFIDENCE
            AllocResultStmt.executeUpdate();
          } catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
            System.out.println("SQLState:     " + E.getSQLState());
            System.out.println("VendorError:  " + E.getErrorCode());

            loggingError(E.toString());
          }

      // Table : SCORE
      // This part can be implemented after Dr. Peng add function 'getAspectValues'
      // score related function.

      int n = pdu.getNumAspects();
      for (int j=0;j<n;j++) {
           AspectValue av = pdu.getAspectValues(j); // org.cougaar.domain.planning.ldm.plan.AspectValue ;
/*
           ASPECTTYPE = Integer.toString(av.getAspectType());
           VALUE = "'"+Double.toString(av.getValue())+"'";
           SCORE = "0";
           q =  PEUID+","+ PESID+","+ PEVENTTYPE+","+ PEVENTTIME+","+ ARTYPE+","+ ASPECTTYPE+","+ VALUE+","+ SCORE;

           InsertIntoDb(SCORETABLE,q);
*/
           try {

              ScoreStmt.setLong(1, uid.getId());          // PEUID
              ScoreStmt.setString(2, uid.getOwner());     // PESID
              ScoreStmt.setInt(3, pdu.getAction());       // PEVENTTYPE
              ScoreStmt.setLong(4, pdu.getTime());        // PEVENTTIME
              ScoreStmt.setInt(5, pdu.getARType());       // ARTYPE
              ScoreStmt.setInt(6, av.getAspectType());    // ASPECTTYPE
              ScoreStmt.setString(7, Double.toString(av.getValue())); // VALUE
              ScoreStmt.setFloat(8, 1);                 // SCORE
              ScoreStmt.executeUpdate();

          } catch (SQLException E) {
              System.out.println("SQLException: " + E.getMessage());
              System.out.println("SQLState:     " + E.getSQLState());
              System.out.println("VendorError:  " + E.getErrorCode());

              loggingError(E.toString());
          }
      }

//       loggingMessage(pdu.toString());

  }
  
  public int getNumUniqueUIDs( long start, long end ) {
      try {
        java.sql.PreparedStatement stmt =
          con.prepareStatement( "SELECT SID, ID FROM EVENTPDU WHERE TIME >="
          + start + " AND TIME < " + end ) ;
         java.sql.ResultSet set = stmt.executeQuery() ;
         int count = 0 ;
         HashMap map = new HashMap() ;
         while ( set.next() ) {
             String owner = set.getString( 1 ).intern() ;
             long id = set.getLong( 2 ) ;
             UIDPDU uidpdu = new UIDStringPDU( owner, id ) ;
             map.put( uidpdu, uidpdu ) ;
         }
         return map.size() ;
      }
      catch ( SQLException e ) {
        e.printStackTrace() ;
      }
      return 0 ;
      
  }
  
  public int getNumUniqueUIDs( long start, long end, int type ) {
      try {
        java.sql.PreparedStatement stmt =
          con.prepareStatement( "SELECT SID, ID FROM EVENTPDU WHERE TIME >="
          + start + " AND TIME < " + end  + " AND TYPE=" + type ) ;
         java.sql.ResultSet set = stmt.executeQuery() ;
         int count = 0 ;
         HashMap map = new HashMap() ;
         while ( set.next() ) {
             String owner = set.getString( 1 ).intern() ;
             long id = set.getLong( 2 ) ;
             UIDPDU uidpdu = new UIDStringPDU( owner, id ) ;
             map.put( uidpdu, uidpdu ) ;
         }
         return map.size() ;
      }
      catch ( SQLException e ) {
        e.printStackTrace() ;
      }
      return 0 ;
  }
  
  public int getNumEventsBetween( long start, long end ) {
      try {
        java.sql.PreparedStatement stmt =
          con.prepareStatement( "SELECT COUNT(*) FROM EVENTPDU WHERE TIME >="
          + start + " AND TIME < " + end  ) ;
         java.sql.ResultSet set = stmt.executeQuery() ;
         set.first() ;
         int count = set.getInt(1) ;
         System.out.println( "count=" + count ) ;
         return count ;
      }
      catch ( SQLException e ) {
        e.printStackTrace();
      }

      return 0 ;  
  }

  protected int getNumEventsBetween( long start, long end, int entityType ) {
      try {
        java.sql.PreparedStatement stmt =
          con.prepareStatement( "SELECT COUNT(*) FROM EVENTPDU WHERE TIME >="
          + start + " AND TIME < " + end + " AND TYPE=" + entityType ) ;

         java.sql.ResultSet set = stmt.executeQuery() ;
         set.first() ;
         int count = set.getInt(0) ;
         return count ;
      }
      catch ( SQLException e ) {
        e.printStackTrace();
      }

      return 0 ;
  }

  public int getNumAssetEventsBetween( long start, long end ) {
        return getNumEventsBetween( start, end, 3 ) ;
  }

  // Get number of unique UIDs between

  public EventList getEventsBetween(long start, long end, int entityType) {

      long   id=0, time=0, executionTime=0, parentid=0;
      int    type=0, action=0, s, i, eventtype=0, artype=0, c=0;
      String ID, SID, TIME, TYPE, sid="", source, EVENTTYPE, ARTYPE, parentsid;
      String q, q2;
      java.sql.ResultSet rs, rs2;
      ByteArrayInputStream bai;

      SymbolPDU taskVerb;
      UIDPDU parentTaskUID, uid;
      TaskPDU tpdu;
      
      return new EventList(PdStatement, start, end, entityType);
       
//      Vector v = new Vector();
      
//      for ( ; el.hasNext() ; ) {
//            v.add(el.next()) ;
//      }
      
      
      
/*
      try {
        if (entityType == EventPDU.TYPE_ASSET) {
            q = "SELECT * FROM EVENTPDU WHERE TIME >= " + Long.toString(start)+
                                                                   " AND TIME < "+ Long.toString(end)
                                                                 + " AND TYPE = 3 ";
        } else {
            q = "SELECT * FROM EVENTPDU WHERE TIME >= " + Long.toString(start)+
                                                                  " AND TIME < "+ Long.toString(end);

        }

        System.out.println(q);
        rs = PdStatement.executeQuery(q);

 	  while (rs.next()) {
	     c++;
	     if (c % 100 == 0) { System.out.println("<"+c+">"); }

           type = rs.getInt("TYPE");
	     bai  = (ByteArrayInputStream) rs.getBinaryStream("PDU");
	     ObjectInputStream p2 = new ObjectInputStream(bai); 

           if ( type == EventPDU.TYPE_TASK) {
		        TaskPDU taskpdu = (TaskPDU ) p2.readObject(); 

//		        TaskPDU taskpdu = (TaskPDU ) rs.getObject("PDU");

          	    v.add(taskpdu);
			
           } else if ( type == EventPDU.TYPE_ASSET) {
		        AssetPDU apdu = (AssetPDU ) p2.readObject(); 
//		        AssetPDU apdu = (AssetPDU ) rs.getObject("PDU");

           	    v.add(apdu);

           } else if (type == EventPDU.TYPE_EXPANSION) {
		        ExpansionPDU epdu = (ExpansionPDU ) p2.readObject(); 
//		        ExpansionPDU epdu = (ExpansionPDU ) rs.getObject("PDU");

	          v.add(epdu);

           } else if (type == EventPDU.TYPE_ALLOCATION) {
		        AllocationPDU alpdu = (AllocationPDU ) p2.readObject(); 
//		        AllocationPDU alpdu = (AllocationPDU ) rs.getObject("PDU");

	          v.add(alpdu);

           } else if (type == EventPDU.TYPE_AGGREGATION) {
		        AggregationPDU agpdu = (AggregationPDU ) p2.readObject(); 
//		        AggregationPDU agpdu = (AggregationPDU ) rs.getObject("PDU");

	          v.add(agpdu);

           } else if ( type == EventPDU.TYPE_ALLOCATION_RESULT) {
		        AllocationResultPDU arpdu = (AllocationResultPDU ) p2.readObject(); 
//		        AllocationResultPDU arpdu = (AllocationResultPDU ) rs.getObject("PDU"); 

	          v.add(arpdu);

	     } else {

			System.out.println("there is no corresponding type !!");

	     }

// 	          bai.close(); 
        }

*/


/*
	while (rs.next()) {
           type = rs.getInt("TYPE");

	   id  = rs.getLong("ID");     //ID = Long.toString(id);
	   sid  = rs.getString("SID"); //SID = "'"+sid+"'";
	   time = rs.getLong("TIME");  //TIME = Long.toString(time);
	   type = rs.getInt("TYPE");   //TYPE = Integer.toString(type);
       eventtype = rs.getInt("EVENTTYPE"); //EVENTTYPE = Integer.toString(eventtype);
       artype =  rs.getInt("ARTYPE");      //ARTYPE = Integer.toString(artype);

//           System.out.println(rs.toString());

           if ( type == EventPDU.TYPE_TASK) {

              // Query on TASK
              TaskRetreiveStmt.setLong(1, rs.getLong("ID"));         // ID
              TaskRetreiveStmt.setString(2, rs.getString("SID"));    // SID
              TaskRetreiveStmt.setLong(3, rs.getLong("TIME"));       // TIME
              TaskRetreiveStmt.setInt(4, rs.getInt("EVENTTYPE"));    // EVENTTYPE

              rs2 = TaskRetreiveStmt.executeQuery();

              if ( rs2.next()) {
                  taskVerb = new SymStringPDU(rs2.getString("VERB"));

                  parentsid = rs2.getString("PARENTSID");
                  parentid = rs2.getLong("PARENTID");

                  if (parentid != 0) {
                      parentTaskUID = new UIDStringPDU(parentsid,parentid);
                  } else {
                      parentTaskUID = null;
                  }

                  uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
                  action = rs2.getInt("TEVENTTYPE");
                  executionTime = rs2.getLong("CREATEDEXECUTION");
                  time = rs2.getLong("TEVENTTIME");
                  if (rs2.getInt("MPTASK") == 1) {
                    tpdu = new MPTaskPDU(taskVerb, uid, action, executionTime, time );
                  } else {
                    tpdu = new TaskPDU(taskVerb, parentTaskUID, uid, action, executionTime, time );
                  }

                  tpdu.setSource(rs2.getString("SOURCE"));
                  v.add(tpdu);
              }

           } else if ( type == EventPDU.TYPE_ASSET) {

              // Query on ASSET
              AssetRetreiveStmt.setLong(1, rs.getLong("ID"));         // ASSETUID
              AssetRetreiveStmt.setString(2, rs.getString("SID"));    // ASID
              AssetRetreiveStmt.setLong(3, rs.getLong("TIME"));       // AEVENTTIME
              AssetRetreiveStmt.setInt(4, rs.getInt("EVENTTYPE"));    // AEVENTTYPE

              rs2 = AssetRetreiveStmt.executeQuery();

              while( rs2.next()) {
                  SymbolPDU assetClass = new SymStringPDU(rs2.getString("ASSETCLASS"));

                  String typeId = rs2.getString("TYPEID");
                  String newItemId = rs2.getString("ITEMID");
                  uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
                  action = rs2.getInt("AEVENTTYPE");
                  executionTime = rs2.getLong("CREATEDEXECUTION");
                  time = rs2.getLong("AEVENTTIME");

                  AssetPDU apdu = new AssetPDU(assetClass, typeId, newItemId, uid, action, executionTime, time );
                  apdu.setSource(rs2.getString("SOURCE"));
                  v.add(apdu);
              }

           } else if (type == EventPDU.TYPE_EXPANSION) {

               // Query on PLAN_ELEMENT

              PlanElementRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              PlanElementRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              PlanElementRetreiveStmt.setInt(3, 2);                         // TYPE
              PlanElementRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              PlanElementRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE
				
              rs2 = PlanElementRetreiveStmt.executeQuery();
              rs2.next();
			  
			  System.out.print("1");

                  uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
                  action = rs2.getInt("PEVENTTYPE");;
                  executionTime = rs2.getLong("CREATEDEXECUTION");
                  time = rs2.getLong("PEVENTTIME");
                  source = rs2.getString("SOURCE");

              // Query on PROCESSED_BY for the parent task.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 1);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();
              rs2.next();

			  System.out.print("2");

              UIDPDU parentTask = new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID"));

              // Query on PROCESSED_BY for the subtasks.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 0);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();

              Vector vuidpdu = new Vector();

			  while (rs2.next()) {

                  vuidpdu.add(new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID")));

              }

			  System.out.print("3");

              s = vuidpdu.size();
              UIDPDU [] tasks = new UIDPDU[s];

              for (i=0;i<s;i++) {
                tasks[i] = (UIDPDU) vuidpdu.get(i);
              }

              ExpansionPDU epdu = new ExpansionPDU(parentTask, tasks, uid, action, executionTime, time );
              epdu.setSource(source);

              v.add(epdu);

//              v.add(new ExpansionPDU(parentTask, vuidpdu.toArray(), uid, action, executionTime, time ));
          } else if (type == EventPDU.TYPE_ALLOCATION) {
                //
              // Query on PLAN_ELEMENT
              PlanElementRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              PlanElementRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              PlanElementRetreiveStmt.setInt(3, 1);                         // TYPE
              PlanElementRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              PlanElementRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = PlanElementRetreiveStmt.executeQuery();
              rs2.next();

              uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
              int newAction = rs.getInt("EVENTTYPE");
              executionTime = rs2.getLong("CREATEDEXECUTION");
              time = rs2.getLong("PEVENTTIME");
              source = rs2.getString("SOURCE");

              // Query on PROCESSED_BY for the parent task.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 1);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();
              rs2.next();

              UIDPDU task = new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID"));

              // Query on PROCESSED_BY for the subtasks.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 0);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();

              UIDPDU allocTask = null;

		      if (rs2.next()) {

                  allocTask = new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID"));

              }

              // Query on ALLOC_TASK_ON for the asset

              AllocTaskOnRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              AllocTaskOnRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              AllocTaskOnRetreiveStmt.setLong(3, rs.getLong("TIME"));       // PEVENTTIME
              AllocTaskOnRetreiveStmt.setInt(4, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = AllocTaskOnRetreiveStmt.executeQuery();
              rs2.next();

              UIDPDU asset = new UIDStringPDU(rs2.getString("ASID"),rs2.getLong("ASSETUID"));
              AllocationPDU alpdu = new AllocationPDU(task, asset, allocTask, uid, newAction, executionTime, time );
              alpdu.setSource(source);
              v.add(alpdu);

          } else if (type == EventPDU.TYPE_AGGREGATION) {

               // Query on PLAN_ELEMENT
              PlanElementRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              PlanElementRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              PlanElementRetreiveStmt.setInt(3, 3);                         // TYPE
              PlanElementRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              PlanElementRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = PlanElementRetreiveStmt.executeQuery();
              rs2.next();

              uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
              action = rs2.getInt("PEVENTTYPE");;
              executionTime = rs2.getLong("CREATEDEXECUTION");
              time = rs2.getLong("PEVENTTIME");
              source = rs2.getString("SOURCE");

              // Query on PROCESSED_BY for the parent task.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 1);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();
              rs2.next();

              UIDPDU task = new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID"));

              // Query on PROCESSED_BY for the subtasks.
              ProcessedbyRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ProcessedbyRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ProcessedbyRetreiveStmt.setInt(3, 0);                         // PROCESSED
              ProcessedbyRetreiveStmt.setLong(4, rs.getLong("TIME"));       // PEVENTTIME
              ProcessedbyRetreiveStmt.setInt(5, rs.getInt("EVENTTYPE"));    // PEVENTTYPE

              rs2 = ProcessedbyRetreiveStmt.executeQuery();
              rs2.next();

              UIDPDU mpTask = new UIDStringPDU(rs2.getString("TASKSID"),rs2.getLong("TASKUID"));

              AggregationPDU arpdu = new AggregationPDU(mpTask, task, uid, action, executionTime, time );
              arpdu.setSource(source);
              v.add(arpdu);

           } else if ( type == EventPDU.TYPE_ALLOCATION_RESULT) {

              // Query on AllocationResultPDU
              AllocResultRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              AllocResultRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              AllocResultRetreiveStmt.setLong(3, rs.getLong("TIME"));       // PEVENTTIME
              AllocResultRetreiveStmt.setInt(4, rs.getInt("EVENTTYPE"));    // PEVENTTYPE
              AllocResultRetreiveStmt.setInt(5, rs.getInt("ARTYPE"));       // ARTYPE

              rs2 = AllocResultRetreiveStmt.executeQuery();
              rs2.next();

              uid = new UIDStringPDU(rs.getString("SID"),rs.getLong("ID"));
              action = rs2.getInt("PEVENTTYPE");;
              executionTime = rs2.getLong("CREATEDEXECUTION");
              time = rs2.getLong("PEVENTTIME");

              short arType = rs2.getShort("ARTYPE");

              AllocationResultPDU ar = new AllocationResultPDU(uid, arType, action, executionTime, time );
              boolean b;
              if (rs2.getInt("SUCCESS")== 1) {
                  b = true;
              } else {
                  b = false;
              }
              ar.setSuccess(b);
              ar.setSource(rs2.getString("SOURCE"));
              ar.setConfidence(rs2.getFloat("CONFIDENCE"));

              // Query on SCORE
              ScoreRetreiveStmt.setLong(1, rs.getLong("ID"));         // PEUID
              ScoreRetreiveStmt.setString(2, rs.getString("SID"));    // PESID
              ScoreRetreiveStmt.setLong(3, rs.getLong("TIME"));       // PEVENTTIME
              ScoreRetreiveStmt.setInt(4, rs.getInt("EVENTTYPE"));    // PEVENTTYPE
              ScoreRetreiveStmt.setInt(5, rs.getInt("ARTYPE"));       // ARTYPE

              rs2 = ScoreRetreiveStmt.executeQuery();

              Vector sv = new Vector();

              while (rs2.next()) {
//                  sv.add(new AspectValue(rs2.getInt("ASPECTTYPE"),rs2.getFloat("VALUE")));
                  sv.add(new AspectValue(rs2.getInt("ASPECTTYPE"),Double.valueOf(rs2.getString("VALUE")).doubleValue()));
              }

              int k = sv.size();

              AspectValue[] aa = new AspectValue[k];

              for (int l=0;l<k;l++) {
                  aa[l] = (AspectValue)sv.get(l);
              }

              ar.setAspectValues(aa);

              v.add(ar);
           }

		}
*/
/*
      } catch (SQLException E) {
        System.out.println("SQLException: " + E.getMessage());
        System.out.println("SQLState:     " + E.getSQLState());
        System.out.println("VendorError:  " + E.getErrorCode());

        loggingError(E.toString());
		loggingError(id + ","+ sid +","+ time +","+ type +","+ artype +","+ eventtype );
      } catch (IOException ioe) {


	} catch (ClassNotFoundException cfe) {

	}
*/
  //    return v.iterator();
  }

  public long getFirstEventTime(){

    long l = 0;

    try {
          java.sql.ResultSet rs = PdStatement.executeQuery("SELECT MIN(TIME) FROM EVENTPDU");

          if(rs.next()) {
              l = rs.getLong(1);
          }

    } catch (SQLException E) {
        System.out.println("SQLException: " + E.getMessage());
        System.out.println("SQLState:     " + E.getSQLState());
        System.out.println("VendorError:  " + E.getErrorCode());
        loggingError(E.toString());
    }

    return l;
  }

  public long getLastEventTime(){

    long l=0;

    try {
         java.sql.ResultSet rs = PdStatement.executeQuery("SELECT MAX(TIME) FROM EVENTPDU");

         if(rs.next()) {
              l = rs.getLong(1);
          }

    } catch (SQLException E) {
        System.out.println("SQLException: " + E.getMessage());
        System.out.println("SQLState:     " + E.getSQLState());
        System.out.println("VendorError:  " + E.getErrorCode());
        loggingError(E.toString());
    }
    return l;
 }

/*
  public void compare(Iterator i, long start, long end, int type) {

      Iterator i2;
      EventPDU epdu1, epdu2;
      String p, a, b;
      boolean c1=false, c2=false;

      if (type == 3) {
          while (i.hasNext()) {  loggingComparison((i.next()).toString());   }

          i2= getEventsBetween(start, end, type);

          while (i2.hasNext()) { loggingComparison2(((PDU) i2.next()).toString()); }

      } else {

          i2 = getEventsBetween(start, end, type);

          c1=i.hasNext();
          c2=i2.hasNext();

          while (c1 == true && c2 == true) {

                if (c1 == true && c2 == true) {

                    epdu1 = (EventPDU) i.next();
                    epdu2 = (EventPDU) i2.next();

                    if ((epdu1.getClass().getName()).compareTo(epdu2.getClass().getName()) != 0) {

                        a = returnContents(epdu1);
                        b = returnContents(epdu2);

                        loggingComparison(a);
                        loggingComparison2(b);

                        loggingComparison3("1m="+a);
                        loggingComparison3("1d="+b);
                    } else {
                       comparePDU(epdu1, epdu2);
                    }

                } else if (c1 == false && c2 == true) {

                    a = returnContents(null);
                    b = returnContents((EventPDU) i2.next());

                    loggingComparison(a);
                    loggingComparison2(b);

                    loggingComparison3("2m="+a);
                    loggingComparison3("2d="+b);

                } else if (c1 == true && c2 == false) {

                    a = returnContents((EventPDU) i.next());
                    b = returnContents(null);

                    loggingComparison(a);
                    loggingComparison2(b);

                    loggingComparison3("3m="+a);
                    loggingComparison3("3d="+b);
                }
                c1=i.hasNext();
                c2=i2.hasNext();
          }

          System.out.println("Getting Data from DB is finished !!");
      }
}

  private void comparePDU(EventPDU epdu1, EventPDU epdu2) {
      String a, b;
       a = returnContents(epdu1);
       b = returnContents(epdu2);

       loggingComparison(a);
       loggingComparison2(b);

       if (a.compareTo(b) != 0) {
          loggingComparison3("m="+a);
          loggingComparison3("d="+b);
      }
  } */

  public void compare(Iterator i, long start, long end, int type) {

      Iterator i2;
      EventPDU epdu1, epdu2;
      String p, a, b;
      boolean c1=false, c2=false;

        while (i.hasNext()) {
           p = "";
           epdu1 = (EventPDU) (i.next());

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

          if (epdu1 instanceof AssetPDU) {

              loggingMem(p,EventPDU.TYPE_ASSET);

          } else if (epdu1 instanceof TaskPDU) {

              loggingMem(p,EventPDU.TYPE_TASK);

          } else if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();

              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }

              loggingMem(p,EventPDU.TYPE_ALLOCATION);

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
                  loggingMem("Exapansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString(),10);
              }

              loggingMem(p,EventPDU.TYPE_EXPANSION);

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p = p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();

             for (int j=0;j<n;j++) {

                  AspectValue av = allocrpdu.getAspectValues(j);

                  loggingMem("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()),11);
             }
             loggingMem(p,EventPDU.TYPE_ALLOCATION_RESULT);
          } else {

              loggingMem(p,EventPDU.TYPE_ALLOCATION);
          }
       }

        i2 = getEventsBetween(start, end, type);

        while (i2.hasNext()) {
           p = "";
           epdu1 = (EventPDU) (i2.next());

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

          if (epdu1 instanceof AssetPDU) {

              loggingDB(p,EventPDU.TYPE_ASSET);

          } else if (epdu1 instanceof TaskPDU) {

              loggingDB(p,EventPDU.TYPE_TASK);

          } else if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();

              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }

              loggingDB(p,EventPDU.TYPE_ALLOCATION);

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
                  loggingDB("Exapansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString(),10);
              }

              loggingDB(p,EventPDU.TYPE_EXPANSION);

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p = p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();

             for (int j=0;j<n;j++) {

                  AspectValue av = allocrpdu.getAspectValues(j);

                  loggingDB("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()),11);
             }
             loggingDB(p,EventPDU.TYPE_ALLOCATION_RESULT);
           } else {

              loggingDB(p,EventPDU.TYPE_ALLOCATION);
          }
       }
       System.out.println("Getting Data from DB is finished !!");
}


    private void comparePDU(EventPDU epdu1, EventPDU epdu2) {

           String p1 = "", p2 ="", q1 = "", q2 = "";

           if (epdu1 instanceof MPTaskPDU) {     p1 = "MPTask"; p2 = "MPTask";        }

           p1 = p1.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());
           p2 = p2.concat(epdu2.toString()+", ExTi="+epdu2.getExecutionTime());

           if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu1 = (AllocationPDU) epdu1;
              AllocationPDU allocpdu2 = (AllocationPDU) epdu2;

              UIDPDU alloctask1 = allocpdu1.getAllocTask();
              UIDPDU alloctask2 = allocpdu2.getAllocTask();

              if (alloctask1 != null) {
                  p1 = p1.concat("AllocTask="+alloctask1.toString());
              } else {
                  p1 = p1.concat("AllocTask=");
              }

              if (alloctask2 != null) {
                  p2 = p2.concat("AllocTask="+alloctask2.toString());
              } else {
                  p2 = p2.concat("AllocTask=");
              }

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu1 = (ExpansionPDU) epdu1;
              ExpansionPDU expdu2 = (ExpansionPDU) epdu2;

              int n1 = expdu1.getNumTasks();
              int n2 = expdu2.getNumTasks();
              int ik1 = 0, ik2 = 0;

              if (n1 == n2) {
                  ik2 = 1;
                  for (int i = 0;i< n1; i++) {

                      ik1 = 0;
                      for (int j=0;j<n2;j++) {
                          UIDStringPDU uids = (UIDStringPDU) expdu1.getTask(i);
                          if (uids.equals((UIDStringPDU) expdu2.getTask(j))== true) {
                            ik1 = 1;
                            break;
                          }
                      }

                      if (ik1 == 0) {
                        ik2 = 0;
                        break;
                      }
                  }
              }

              if (ik2 == 0) {

                  for (int j=0;j<n1;j++) {
                      q1 = q1.concat(",Expansion="+(expdu1.getUID()).toString()+", SubTasks="+ (expdu1.getTask(j)).toString());
                  }

                  for (int j=0;j<n2;j++) {
                      q2 = q2.concat(",Expansion="+(expdu2.getUID()).toString()+", SubTasks="+ (expdu2.getTask(j)).toString());
                  }
              }

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu1 = (AllocationResultPDU) epdu1;
             AllocationResultPDU allocrpdu2 = (AllocationResultPDU) epdu2;

             p1=p1.concat("Confidence="+Float.toString(allocrpdu1.getConfidence()));
             p2=p2.concat("Confidence="+Float.toString(allocrpdu2.getConfidence()));
             int n1 = allocrpdu1.getNumAspects();
             int n2 = allocrpdu2.getNumAspects();
              int ik1 = 0, ik2 = 0;

              if (n1 == n2) {
                  ik2 = 1;
                  for (int i = 0;i< n1; i++) {

                      ik1 = 0;
                      for (int j=0;j<n2;j++) {
                          AspectValue av = allocrpdu1.getAspectValues(i);

                          if (av.equals(allocrpdu1.getAspectValues(j))== true) {
                            ik1 = 1;
                            break;
                          }
                      }

                      if (ik1 == 0) {
                        ik2 = 0;
                        break;
                      }
                  }
              }

              if (ik2 == 0) {

                  for (int j=0;j<n1;j++) {
                      AspectValue av = allocrpdu1.getAspectValues(j);
                      q1 = q1.concat(",AllocResultPEID="+(allocrpdu1.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()));
                  }

                  for (int j=0;j<n2;j++) {
                      AspectValue av = allocrpdu2.getAspectValues(j);

                      q2 = q2.concat(",AllocResultPEID="+(allocrpdu2.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()));
                  }
              }
          }

      loggingComparison(p1+q1);
      loggingComparison2(p2+q2);

      if (p1.compareTo(p2) != 0 || q1.compareTo(q2) != 0 ) {
          loggingComparison3("4m="+p1+q1);
          loggingComparison3("4d="+p2+q2);
      }
  }

  private String returnContents(EventPDU epdu1) {

           String p = "", q = "";

           if (epdu1 == null ) {
//                loggingComparison2("null PDU");
                return "null PDU";
           }

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

           if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();
              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
//                  loggingComparison("Expansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString());
                  q = q.concat(",Expansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString());
              }

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p=p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();
             for (int j=0;j<n;j++) {
                  AspectValue av = allocrpdu.getAspectValues(j);

//                  loggingComparison("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
//                                                                                    +", Value="+Double.toString(av.getValue()) );
                  q = q.concat(",AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()));
             }
          }

          return p+q;
//          loggingComparison2(p);
//          System.out.println(p);
  }

  private void loggingPDU(EventPDU epdu1) {

           String p = "";

           if (epdu1 == null ) {
                loggingComparison2("null PDU");
                return;
           }

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

           if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();
              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
                  loggingComparison("Expansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString());
              }

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p=p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();
             for (int j=0;j<n;j++) {
                  AspectValue av = allocrpdu.getAspectValues(j);

                  loggingComparison("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()) );
             }
          }
          loggingComparison2(p);
          System.out.println(p);
    }

/*
        while (i.hasNext()) {
           p = "";
           epdu1 = (EventPDU) (i.next());

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

           if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();
              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }
          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
                  loggingComparison("Expansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString());
              }

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p = p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();
             for (int j=0;j<n;j++) {
                  AspectValue av = allocrpdu.getAspectValues(j);

                  loggingComparison("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()) );
             }
          }

           loggingComparison2(p);
        }

        i2 = getEventsBetween(start, end, type);

        while (i2.hasNext()) {
           p = "";
           epdu1 = (EventPDU) (i2.next());

           if (epdu1 instanceof MPTaskPDU) {     p = "MPTask";        }

           p = p.concat(epdu1.toString()+", ExTi="+epdu1.getExecutionTime());

           if (epdu1 instanceof AllocationPDU) {

              AllocationPDU allocpdu = (AllocationPDU) epdu1;

              UIDPDU alloctask = allocpdu.getAllocTask();

              if (alloctask != null) {
                  p = p.concat("AllocTask="+alloctask.toString());
              } else {
                  p = p.concat("AllocTask=");
              }

          } else if (epdu1 instanceof ExpansionPDU) {

              ExpansionPDU expdu = (ExpansionPDU) epdu1;

              int n = expdu.getNumTasks();

              for (int j=0;j<n;j++) {
                  loggingComparison3("Exapansion="+(expdu.getUID()).toString()+", SubTasks="+ (expdu.getTask(j)).toString());
              }

          } else if (epdu1 instanceof AllocationResultPDU) {

             AllocationResultPDU allocrpdu = (AllocationResultPDU) epdu1;

             p = p.concat("Confidence="+Float.toString(allocrpdu.getConfidence()));

             int n = allocrpdu.getNumAspects();

             for (int j=0;j<n;j++) {

                  AspectValue av = allocrpdu.getAspectValues(j);

                  loggingComparison3("AllocResultPEID="+(allocrpdu.getPlanElementUID()).toString()+", AspectType="+ Integer.toString(av.getAspectType())
                                                                                    +", Value="+Double.toString(av.getValue()) );
             }
          }

           loggingComparison4(p);
         }
        */


    //  1. Number of tasks to be allocated
  public void generateTimeSeries_Numberoftasks(String Cluster) {}

  //  2. Time to allocate : time taken for a task to be allocated to a cluster or an asset
  public void generateTimeSeries_TimetoAllocate(String Cluster){}

  //  3. Time to respond : time taken to report result about the allocated task.
  public void generateTimeSeries_TimetoRespond(String Cluster){}

  //  4. Task arrival rate
  public void generateTimeSeries_TaskArrivalRate(String Cluster){}

  //  5. Task interarrival time
  public void generateTimeSeries_TaskInterarrivalTime(String Cluster){}

  //  6. Local generation rate
  public void generateTimeSeries_LocalGenerationRate(String Cluster){}

  //  7. Rescind rate
  public void generateTimeSeries_RescindRate(String Cluster){}

  //  8. Allocation rate
  public void generateTimeSeries_AllocationRate(String Cluster){}

  //  9. Allocation success rate
  public void generateTimeSeries_AllocationSuccessRate(String Cluster){}

  // 10. Tasks generated without allocation result
  public void generateTimeSeries_TaskNoAR(String Cluster){}

  
  // 11. Percentage for the above aspsects
  // public void generateTimeSeries_Numberoftasks(String Cluster);

}
