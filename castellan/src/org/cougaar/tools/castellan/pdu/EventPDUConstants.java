/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  */

package org.cougaar.tools.castellan.pdu;

/**
 *
 * @author  bbowles
 */
public interface EventPDUConstants {
    /**  Signifies that the PDU corresponds to a logging directive from the
     *   server.
     */
    public static final int TYPE_DIRECTIVE = -1;
    public static final int TYPE_NONE = 0 ;
    public static final int TYPE_TASK = 1 ;
    public static final int TYPE_ALLOCATION = 2 ;
    public static final int TYPE_ASSET = 3 ;
    public static final int TYPE_EXPANSION = 4 ;
    public static final int TYPE_AGGREGATION = 5 ;
    public static final int TYPE_ALLOCATION_RESULT = 6 ;
    public static final int TYPE_DECLARE_AGENT = 7 ;

    public static final String STRING_NONE = "NONE" ;
    public static final String STRING_TASK = "TASK" ;
    public static final String STRING_ALLOCATION = "ALLOCATION" ;
    public static final String STRING_ASSET = "ASSET" ;
    public static final String STRING_EXPANSION = "EXPANSION" ;
    public static final String STRING_AGGREGATION = "AGGREGATION" ;
    public static final String STRING_ALLOCATION_RESULT = "ALLOCATION_RESULT" ;
    public static final String STRING_DECLARE_AGENT = "DECLARE_AGENT" ;

    public static final int ACTION_ADD = 0 ;
    public static final int ACTION_REMOVE = 1 ;
    public static final int ACTION_CHANGE = 2 ;
    
    public static final String STRING_ADD = "ADD" ;
    public static final String STRING_REMOVE = "REMOVE" ;
    public static final String STRING_CHANGE = "CHANGE" ;
    
    /** The server always as a Symbol ID of 0.
     */
    public static final int SERVER_SID = 0 ;
}
