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
 * UIDStringPDU.java
 *
 * Created on June 12, 2001, 11:09 AM
 */

package org.hydra.pdu;
import java.io.* ;

/**
 * UID represented as a string, id combination.  All UIDPDUs will be resolved to
 * this after they are received.
 *
 * @author  wpeng
 * @version 
 */
public class UIDStringPDU extends UIDPDU implements java.io.Externalizable {

    public UIDStringPDU() {}
    
    /** Creates new UIDStringPDU */
    public UIDStringPDU(String owner, long id) {
        this.owner = owner.intern() ;
        this.id = id ;
    }
    
    public void outputParamString( StringBuffer buf ) {
        buf.append( owner ).append( '/' ) .append( id ) ;
    }
    
    public String toString() {
        if ( id < 0 ) {
            return owner + '/' + idToString( id ) ;
        }
        return owner + '/' + id ; 
    }
    
    public boolean equals( Object o ) {
        if ( o instanceof UIDStringPDU ) {
            UIDStringPDU updu = ( UIDStringPDU ) o ;
            return ( updu.owner == owner && updu.id == id ) ;
        }
        return false ; 
    }
    
    public int hashCode() {
        return owner.hashCode() + ( int ) id ;   
    }
    
    public String getOwner() { return owner ; }
    
    public long getId() { return id ; }
    
    public void readExternal(java.io.ObjectInput objectInput) throws java.io.IOException, java.lang.ClassNotFoundException {
        owner = objectInput.readUTF().intern() ;
        id = objectInput.readLong() ;
    }
    
    public void writeExternal(java.io.ObjectOutput objectOutput) throws java.io.IOException {
        objectOutput.writeUTF( owner  );
        objectOutput.writeLong( id ) ;
    }

    transient String owner ;
    transient long id = -1;

    static final long serialVersionUID = 3421090090728360054L;    
}
