/*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
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
  */
package org.cougaar.tools.castellan.server.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.tools.castellan.plugin.RelayServerMTImpl;
import org.cougaar.tools.castellan.pdu.PDUSink;
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.DeclarePDU;
import org.cougaar.tools.castellan.pdu.TimeRequestPDU;
import org.cougaar.tools.castellan.planlog.PDUBuffer;

public class ServerPlugin extends ComponentPlugin implements PDUSink {

    protected void setupSubscriptions() {
        impl = new RelayServerMTImpl( null, getBlackboardService() ) ;
        impl.setPDUSink( this );
        buffer = new PDUBuffer() ;
        getBlackboardService().publishAdd( buffer ) ;
    }

    public void send( PDU pdu ) {
        impl.sendMessage( pdu );
    }

    protected void execute() {
        impl.execute();
    }

    /**
     * Processing messages from the server MT impl.
     */
    public void processPDU(PDU pdu) {
        if ( pdu instanceof DeclarePDU ) {
            TimeRequestPDU pdu2 = new TimeRequestPDU( 0, System.currentTimeMillis() ) ;
            pdu2.setDestination( pdu.getSource() );
            send( pdu2 );
        }

        synchronized ( buffer ) {
            buffer.addIncoming( pdu );
        }
    }

    protected PDUBuffer buffer ;
    protected RelayServerMTImpl impl ;
}
