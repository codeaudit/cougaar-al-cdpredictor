/*
 * EventPDUInterface.java
 *
 * Created on May 10, 2002, 12:47 PM
 */

package org.cougaar.tools.castellan.pdu;

/**
 *
 * @author  bbowles
 */
public interface EventPDUInterface {
    /**  Signifies that the PDU corresponds to a logging directive from the
     *   server.
     */
    int TYPE_DIRECTIVE = -1;
    int TYPE_NONE = 0 ;
    int TYPE_TASK = 1 ;
    int TYPE_ALLOCATION = 2 ;
    int TYPE_ASSET = 3 ;
    int TYPE_EXPANSION = 4 ;
    int TYPE_AGGREGATION = 5 ;
    int TYPE_ALLOCATION_RESULT = 6 ;
    
    String STRING_NONE = "NONE" ;
    String STRING_TASK = "TASK" ;
    String STRING_ALLOCATION = "ALLOCATION" ;
    String STRING_ASSET = "ASSET" ;
    String STRING_EXPANSION = "EXPANSION" ;
    String STRING_AGGREGATION = "AGGREGATION" ;
    String STRING_ALLOCATION_RESULT = "ALLOCATION_RESULT" ;    

    int ACTION_ADD = 0 ;
    int ACTION_REMOVE = 1 ;                  
    int ACTION_CHANGE = 2 ;
    
    String STRING_ADD = "ADD" ;
    String STRING_REMOVE = "REMOVE" ;
    String STRING_CHANGE = "CHANGE" ;
    
    /** The server always as a Symbol ID of 0.
     */
    int SERVER_SID = 0 ;    
}
