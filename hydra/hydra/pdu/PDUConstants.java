/*
  * <copyright>
  *  Copyright 1997-2001 (Intelligent Automation, Inc.)
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
  * CHANGE RECORD
  *  8/23/01 Initial version  Penn State/IAI
  */

/*
 * PDUConstants.java
 *
 * Created on August 23, 2001, 3:16 PM
 */

package org.hydra.pdu;

/**
 *
 * @author  wpeng
 * @version 
 */
public abstract class PDUConstants {

    /** Creates new PDUConstants */
    public PDUConstants() {
    }

    /** Tag for whether task preferences should be included.
     */
    public final static String TASK_PREFERENCES = "TASK PREFERENCES" ;
    
    /** Tag indicate whether AllocationResults should be included.
     */
    public static final String ALLOCATION_RESULTS = "ALLOCATION RESULTS" ;
}
