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
  *  8/16/01 Initial version  by Penn State/IAI
  */

/*
 * StringSymPDU.java
 *
 * Created on August 16, 2001, 4:47 PM
 */

package org.cougaar.tools.castellan.pdu;

/**
 * Represents a string token as a string.
 */
public class SymStringPDU extends SymbolPDU implements java.io.Externalizable {

    public SymStringPDU() {   
    }
    
    /** Creates new StringSymPDU */
    public SymStringPDU(String s) {
        this.s = s ;
    }
    
    public String toString() { return s ; }
    
    public void outputParamString( StringBuffer buf ) { buf.append( s ) ; }

    public void readExternal(java.io.ObjectInput objectInput) throws java.io.IOException, java.lang.ClassNotFoundException {
        s = objectInput.readUTF().intern() ;
    }
    
    public void writeExternal(java.io.ObjectOutput objectOutput) throws java.io.IOException {
        objectOutput.writeUTF( s ) ;
    }
    
    private transient String s ;
    
    static final long serialVersionUID = 8752876260627557215L;    
}
