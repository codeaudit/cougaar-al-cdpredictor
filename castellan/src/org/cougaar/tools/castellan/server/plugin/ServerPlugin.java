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
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.tools.castellan.plugin.RelayServerMTImpl;
import org.cougaar.tools.castellan.pdu.PDUSink;
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.DeclarePDU;
import org.cougaar.tools.castellan.pdu.TimeRequestPDU;
import org.cougaar.tools.castellan.planlog.PDUBuffer;
import org.cougaar.util.UnaryPredicate;

import java.util.Collection;

public class ServerPlugin extends ComponentPlugin implements PDUSink {

    class FlushThread extends Thread {

        public long getInterval ()
        {
            return interval;
        }

        public void setInterval ( long interval )
        {
            this.interval = interval;
        }

        public boolean isStop ()
        {
            return stop;
        }

        public void setStop ( boolean stop )
        {
            this.stop = stop;
        }

        public void run ()
        {
            while ( !stop ) {
                try {
                   sleep( interval ) ;
                }
                catch ( InterruptedException e ) {
                }

                if ( stop ) {
                    break ;
                }

                BlackboardService bs = getBlackboardService() ;
                bs.openTransaction();
                impl.flush();
                bs.closeTransaction();
            }
        }

        long interval = 4000 ;
        boolean stop = false ;
    }


    public void setupSubscriptions() {
        ServiceBroker sb = getServiceBroker() ;
        log = ( LoggingService ) sb.getService( this, LoggingService.class, null ) ;

        impl = new RelayServerMTImpl( null, getBlackboardService() ) ;
        impl.setPDUSink( this );

        findOrCreateBuffer() ;
        flushThread = new FlushThread() ;
        flushThread.start();
    }

    public void unload () {
        if ( flushThread != null ) {
             flushThread.setStop( true );
             flushThread.interrupt();
        }
    }

    protected void findOrCreateBuffer() {
        Collection c = getBlackboardService().query( new UnaryPredicate() {
            public boolean execute ( Object o )
            {
                if ( o instanceof PDUBuffer ) {
                    return true ;
                }
                return false;
            }
        } ) ;

        if ( c.size() == 0 ) {
            buffer = new PDUBuffer() ;
            getBlackboardService().publishAdd( buffer ) ;
        }
        else {
            Object[] buffers = c.toArray() ;
            if ( buffers.length > 1 ) {
                if ( log != null && log.isWarnEnabled() ) {
                    log.warn( "More than one PDU buffer created for agent \"" + getBindingSite().getAgentIdentifier() + "\". Using first." );
                }
            }
            buffer = ( PDUBuffer ) buffers[0] ;
        }

    }

    public void send( PDU pdu ) {
        impl.sendMessage( pdu );
    }

    public void execute() {
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

    protected LoggingService log ;
    protected PDUBuffer buffer ;
    protected RelayServerMTImpl impl ;
    protected FlushThread flushThread ;
}
