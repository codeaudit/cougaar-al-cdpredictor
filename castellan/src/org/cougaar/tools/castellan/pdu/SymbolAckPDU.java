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
  *  6/15/01 Initial version  by Penn State/IAI
  */

/*
 * SymbolAckPDU.java
 *
 * Created on June 15, 2001, 6:16 PM
 */

package org.cougaar.tools.castellan.pdu;

/**
 *
 * @author  wpeng
 * @version 
 */
public class SymbolAckPDU extends PDU {

    /** Creates new SymbolAckPDU */
    public SymbolAckPDU(String symbol,int sid) {
        this.symbol = symbol ;
        this.sid = sid ;
    }

    public String getSymbol() { return symbol ; }

    public int getSymId() { return sid ; }

    public String toString() {
        return( "[SymbolAckPDU, value=" + symbol + ",id=" + sid + "]" ) ;
    }

    String symbol;
    int sid ;

    static final long serialVersionUID = -745952681382295156L;    
}
