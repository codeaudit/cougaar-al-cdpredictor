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
  * CHANGE RECORD
  *  6/12/01 Initial version  by Penn State/IAI
  */

/*
 * UIDPDU.java
 *
 * Created on June 12, 2001, 11:55 AM
 */

package org.cougaar.tools.castellan.pdu;

import java.io.*;

/**
 * Base class for all UID PDUs.
 * 
 * @author  wpeng
 * @version 
 */
public abstract class UIDPDU implements java.io.Serializable, java.io.Externalizable {
    
   public static final int UNKNOWN = -1 ;
   public static final int SOURCE_ASSET = -2 ;
   public static final int TASK_SOURCE = -3 ;
   public static final int SINK_ASSET = -4 ;
   public static final int SHADOW_ALLOCATION = -5 ;
    
    /** Creates new UIDPDU */
    public UIDPDU() {
    }
    
    public static final String idToString( long id ) {
        switch ( ( int ) id ) {
            case SOURCE_ASSET :
                return "SOURCE_ASSET" ;
            case TASK_SOURCE :
                return "TASK_SOURCE" ;
            case SINK_ASSET :
                return "SINK_ASSET" ;
            default :
                return "UNKNOWN" ;
        }
    }

    public abstract void outputParamString( StringBuffer buf ) ;
   
    static final long serialVersionUID = 2747687692526173210L;    
}
