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

package org.hydra.pdu;

/**
 *
 * @author  wpeng
 * @version 
 */
public abstract class UniqueObjectPDU extends EventPDU {

    /** Creates new UniqueObjectPDU */
    public UniqueObjectPDU( UIDPDU uidPDU, int type, int newAction, long executionTime, long time ) {
        super( type, newAction, executionTime, time ) ;
        this.uidPDU = uidPDU ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ',' ) ;
        format( buf, uidPDU ) ;
    }
    
    public UIDPDU getUID() { return uidPDU ; }

    UIDPDU uidPDU ;
    
    static final long serialVersionUID = 8562510338684578920L;    
}
