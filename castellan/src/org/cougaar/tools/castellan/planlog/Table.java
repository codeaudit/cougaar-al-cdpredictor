package org.cougaar.tools.castellan.planlog;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

/**
 * Title:        Table
 * Description:
 */

public abstract class Table {

   // ATTRIBUTES
    Statement myStatement;

   // METHODS
   public static void printSQLException( SQLException ex )
   {
      System.err.println( "SQLException \n SQLState:  " + ex.getSQLState() );
      System.err.println( "Message:  " + ex.getMessage() );
      System.err.println( "Vendor:  " + ex.getErrorCode() );
   }
   public Table( Connection conn ){
      // Load the prepared statements for this class that will be used by the database server.
      loadPreparedStatements( conn );
      try{
         myStatement = conn.createStatement();
      }
      catch( SQLException ex ){
          printSQLException( ex );
      }
   }

   abstract public void add(PDU pdu);
   abstract public void createTable( Statement theStatement );
   abstract public void loadPreparedStatements( Connection conn );
}