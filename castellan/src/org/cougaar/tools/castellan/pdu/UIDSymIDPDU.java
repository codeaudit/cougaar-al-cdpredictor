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

package org.cougaar.tools.castellan.pdu;

/**
 * Compressed UID description using symbol ids.  These are created only
 * by the message transport layer.
 * 
 * @author  wpeng
 * @version 
 */
public class UIDSymIDPDU extends UIDPDU {

    public UIDSymIDPDU() {
    }
    
    /** Creates new UIDSymbolPDU */
    public UIDSymIDPDU(int ownerSID, long id ) {
        this.ownerSID = ownerSID ; 
        this.id = id ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        buf.append( ownerSID ).append( '/' ) .append( id ) ;
    }    
    
    public int getOwnerSID() { return ownerSID ; }
    
    public long getID() { return id ; }
    
    public int hashCode() {
        return ( int ) ( ownerSID + id ) ;   
    }    
    
    public void readExternal(java.io.ObjectInput objectInput) throws java.io.IOException, java.lang.ClassNotFoundException {
        ownerSID = objectInput.readInt() ;
        id = objectInput.readLong() ;
    }
    
    public void writeExternal(java.io.ObjectOutput objectOutput) throws java.io.IOException {
        objectOutput.writeInt( ownerSID  );
        objectOutput.writeLong( id ) ;
    }
    
    transient int ownerSID = -1 ;
    transient long id = -1 ;
    
    static final long serialVersionUID = -7174659568543327386L;    
}