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

public class EventList implements Iterator {

	
 	java.sql.Statement PdStatement;
        java.sql.ResultSet rs;
        
	public EventList(java.sql.Statement pdstmt, long start, long end, int entityType) {
                
                String q;
                PdStatement = pdstmt;
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
                    
		} catch (SQLException E) {
    		    System.out.println("SQLException: " + E.getMessage());
		    System.out.println("SQLState:     " + E.getSQLState());
		    System.out.println("VendorError:  " + E.getErrorCode());
   	        } 
	}
        
        public boolean hasNext() {
            
            try {
 	           if(!rs.next()) {
                        return false;
                   } 
                   
                   rs.previous();                   
            } catch (SQLException E) {
                System.out.println("SQLException: " + E.getMessage());
                System.out.println("SQLState:     " + E.getSQLState());
                System.out.println("VendorError:  " + E.getErrorCode());
            }
            
            return true;        
        }
           
        public Object next() {
            //long   id=0, time=0, executionTime=0, parentid=0;
            int    eventtype=0, artype=0, c=0;
            String ID, SID, TIME, TYPE, sid="", source, EVENTTYPE, ARTYPE, parentsid;
            long time, id;
            
            int    type=0;
            
            try {
 	           if(!rs.next()) {
                        return null;                    
                   }

                   type = rs.getInt("TYPE");
/*                   
                   id  = rs.getLong("ID");     //ID = Long.toString(id);
//	           sid  = rs.getString("SID"); //SID = "'"+sid+"'";
	           time = rs.getLong("TIME");  //TIME = Long.toString(time);
	           type = rs.getInt("TYPE");   //TYPE = Integer.toString(type);
                   
                   System.out.println(id+","+time+","+type);                   
*/                 System.out.print(".");  
                   ByteArrayInputStream bai  = (ByteArrayInputStream) rs.getBinaryStream("PDU");
	           ObjectInputStream p2 = new ObjectInputStream(bai); 
                   
                   
                   if ( type == EventPDU.TYPE_TASK) {
	        
                        TaskPDU taskpdu = (TaskPDU ) p2.readObject(); 
        	        return taskpdu;
			
                   } else if ( type == EventPDU.TYPE_ASSET) {
	                AssetPDU apdu = (AssetPDU ) p2.readObject(); 
         	        return apdu;

                   } else if (type == EventPDU.TYPE_EXPANSION) {
		
                        ExpansionPDU epdu = (ExpansionPDU ) p2.readObject(); 
	                return epdu;

                   } else if (type == EventPDU.TYPE_ALLOCATION) {
		
                        AllocationPDU alpdu = (AllocationPDU ) p2.readObject(); 
                        return alpdu;

                   } else if (type == EventPDU.TYPE_AGGREGATION) {
		        AggregationPDU agpdu = (AggregationPDU ) p2.readObject(); 
	                return agpdu;

                   } else if ( type == EventPDU.TYPE_ALLOCATION_RESULT) {
		        AllocationResultPDU arpdu = (AllocationResultPDU ) p2.readObject(); 
	                return arpdu;

	           } else {

		        System.out.println("there is no corresponding type !!");

	           }

            } catch (SQLException E) {
                System.out.println("SQLException: " + E.getMessage());
                System.out.println("SQLState:     " + E.getSQLState());
                System.out.println("VendorError:  " + E.getErrorCode());

            } catch (IOException ioe) {

	    } catch (ClassNotFoundException cfe) {

	    } 
            return null;
        }
          
        public void remove() {
         return;       
        }

}
