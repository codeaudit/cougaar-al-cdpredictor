package org.cougaar.tools.castellan.planlog;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.server.* ;
import org.cougaar.planning.ldm.plan.AspectValue ;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;

public class PDUList implements Iterator {
    Statement PdStatement;
    ResultSet myResultSet;
    boolean resultSetHasElements = true;
    
    public PDUList(ResultSet theResultSet) {
        myResultSet = theResultSet;
        if( myResultSet == null ){
           throw new IllegalArgumentException("The ResultSet CANNOT be null!");
        }
        // Make sure the cursor is before first row of result set before
        // any access is made to the result set.
        // Note that if the result set is empty, the isBeforeFirst() will return false.
        // Use that as a test for an empty result set.
        try{
           myResultSet.beforeFirst();
           resultSetHasElements = myResultSet.isBeforeFirst();
        }
        catch (SQLException ex) {
            Table.printSQLException(ex);
        }
    }
    
    public boolean hasNext() {
       boolean hasAnother = true; 
       if( !resultSetHasElements ){
          return false;
       }
       try {
          // If cursor is on the last row, then there is not another row.
          if( myResultSet.isLast() ){
               hasAnother = false;
          }
          
          //if( !myResultSet.next() ){
          //   System.out.println( "Has another is false" ) ;
          //   hasAnother = false;
          //}
          //boolean prev = myResultSet.previous();
          //System.out.println( "Previous = " + prev + ",beforefirst" + myResultSet.isBeforeFirst() ) ;
       }
       catch (SQLException ex) {
          Table.printSQLException(ex);
       }
       return hasAnother;
    }
    
    public Object next() {
       Object nextObj = null; 
       try {
          // Move cursor to next row in result set.
          if ( myResultSet.isLast() ) {
             throw new NoSuchElementException();             
          }
          myResultSet.next() ;
          
          //if( !myResultSet.next() ){
          //   throw new NoSuchElementException();
          //}
          ByteArrayInputStream bais  = (ByteArrayInputStream) myResultSet.getBinaryStream("PDU");
          ObjectInputStream ois = new ObjectInputStream(bais);
          if( ois != null ){
             nextObj = ois.readObject();
          }
       }
       catch (SQLException ex) {
          Table.printSQLException(ex);
       }
       catch ( IndexOutOfBoundsException ioobe) {
          System.out.println(ioobe.getMessage());
       }
       catch (ClassNotFoundException cnfe) {
          System.out.println(cnfe.getMessage());
       }
       catch ( IOException ioe) {
          System.out.println(ioe.getMessage());
       }
       return nextObj;
    }
    
    public void remove() {
       try{
          myResultSet.deleteRow();
       }
        catch (SQLException ex) {
            Table.printSQLException(ex);
        }
       return; 
    }
}
