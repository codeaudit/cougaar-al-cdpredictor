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
  *  3/15/02 Initial version  by IAI
  */

package org.cougaar.tools.castellan.server;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.ldm.*;
import org.cougaar.core.plugin.*;
import org.cougaar.core.service.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.util.*;

import java.util.*;

/**
 * This is a single threaded MT impl.
 */
public class ServerBlackboardMTImpl implements ServerMessageTransport
{

    /**
     * Extracts received log messages.
     */
    UnaryPredicate logMessagePredicate = new UnaryPredicate() {
        public boolean execute(Object o)
        {
            if ( !( o instanceof LogMessage) ) {
                return false ;
            }
            LogMessage lm = ( LogMessage ) o ;
            return !lm.isLocal() ;
        }
    } ;

    public ServerBlackboardMTImpl( ComponentPlugin plugIn, BlackboardService service ) {
        this.blackboardService = service ;
        unreadLogMessages= ( IncrementalSubscription ) service.subscribe( logMessagePredicate ) ;
    }

    /**
     * Call during parent plug-in's execute cycle.
     */
    public void execute() {

         // Read any incoming log messages.
         Collection c = unreadLogMessages.getAddedCollection() ;
         for ( Iterator iter = c.iterator(); iter.hasNext() ; ) {
             LogMessage lm = ( LogMessage ) iter.next() ;

             // DEBUG
             System.out.println( "ServerBlackboardMTImpl::Receiving message of type " + lm.getClass() );
             if ( lm instanceof WrappedPDUMessage ) {
                processPDU( (( WrappedPDUMessage ) lm ).getPDU() );
             }
             else if ( lm instanceof BatchMessage ) {
                 // Decompress each batch message.
                 for ( Iterator iter1= ( ( BatchMessage ) lm ).getIterator(); iter1.hasNext() ; ) {
                     PDU pdu = ( PDU ) iter1.next() ;
                     processPDU( pdu );
                 }
             }
             // Remove this from the BB immediately
             blackboardService.publishRemove( lm ) ;
         }
         // Remove them immediate from the BB.
    }

    protected boolean processMetaPDU( PDU pdu ) {
        // Process all declare PDUs.
        if ( pdu instanceof DeclarePDU ) {
            // Add as a client and record statistics

            return true ;
        }
        return false ;
    }

    protected void processPDU( PDU pdu ) {
        if ( !processMetaPDU(pdu) && sink != null ) {
            sink.processPDU( pdu );
        }
    }


    public void start()
    {
        isActive = true ;
    }

    public void stop()
    {
        isActive = false ;
    }

    public Vector getClients()
    {
        return null;
    }

    public void setPDUSink(PDUSink sink)
    {
        this.sink = sink ;
    }

    public void sendMessage(PDU pdu)
    {
        if ( pdu.getDestination() == null ) {
            throw new RuntimeException( "PDU " + pdu + " does not have destination address." ) ;
        }
        // No batching.  Just wrap and add to the blacboard.
        WrappedPDUMessage pm = new WrappedPDUMessage(pdu) ;
        pm.setDestination( new ClusterIdentifier( pdu.getDestination() ) );
        blackboardService.publishAdd( pm ) ;
    }

    IncrementalSubscription unreadLogMessages ;
    BlackboardService blackboardService ;
    ComponentPlugin plugIn ;
    PDUSink sink ;
    ArrayList clients = new ArrayList() ;
    boolean isActive = false ;
}
