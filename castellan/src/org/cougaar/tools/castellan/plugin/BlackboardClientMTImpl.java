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
  *  6/12/01 Initial version  by IAI
  */

package org.cougaar.tools.castellan.plugin;

import org.cougaar.tools.castellan.pdu.*;
import org.cougaar.tools.castellan.ldm.*;
import org.cougaar.tools.castellan.server.*;
import org.cougaar.core.service.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.util.*;
import org.w3c.dom.*;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Implements client side transport through the blackboard.
 */

public class BlackboardClientMTImpl implements ClientMessageTransport {
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
            return ( !lm.isLocal() && !lm.isRead() ) ;
        }
    } ;

    /**
     * Periodically scans the blackboard for incoming or outgoing messages.
     */
    class MTThread extends Thread {
        public void run()
        {
            try {
                sleep( 5000 ) ;
                BlackboardService bs = plugIn.getBlackboardService() ;
                // Find new unread LogMessages, put them in the sink.
                bs.openTransaction();
                Collection c = bs.query( logMessagePredicate ) ;
                // Now, go through the collection, decompress and
                for ( Iterator iter = c.iterator();iter.hasNext();) {
                     LogMessage lm = ( LogMessage ) iter.next() ;

                     // Turn these back into PDUs and add them to the sink in this thread
                     //
                     if ( !lm.isLocal() && !lm.isRead() ) {
                         if ( sink != null ) {
                             if ( lm instanceof WrappedPDUMessage ) {
                                sink.processPDU( (( WrappedPDUMessage ) lm ).getPDU() );
                             }
                             else if ( lm instanceof BatchMessage ) {
                                 // Decompress batch message.
                                 for ( Iterator iter1= ( ( BatchMessage ) lm ).getIterator(); iter1.hasNext() ; ) {
                                     PDU pdu = ( PDU ) iter1.next() ;
                                     sink.processPDU( pdu );
                                 }
                             }
                         }
                         lm.setRead( true );
                         bs.publishRemove( lm ) ;
                     }
                }
                bs.closeTransaction();

            }
            catch ( InterruptedException e ) {

            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public class FlushTimerThread extends Thread {
        public void run()
        {
            while ( !stop ) {
                try {
                    sleep( 1500 ) ;
                    if ( ( plugIn.currentTimeMillis() - lastAddTime ) > maximumDelay ) {
                       synchflush();
                    }
                }
                catch (InterruptedException e ) {

                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }

        boolean stop = false ;
    }

    public BlackboardClientMTImpl(PlanMonitorPlugIn plugIn, ConfigFinder finder )
    {
        this.finder = finder ;
        this.plugIn = plugIn;
        try
        {
            bas = new ByteArrayOutputStream( maxBatchSize + 10000 );
            oos = new ObjectOutputStream( bas );
        } catch (Exception e)
        {

        }

        targetCluster = getAddressFromConfig() ;
        if ( targetCluster == null ) {
            // DEBUG -- replace by approprate log4j call
            System.out.println( "PlanMonitorPlugIn:: Warning: Event logging is disabled, no target cluster can be found." ) ;
        }
        // DEBUG
        System.out.println("PlanMonitorPlugIn::Logging to cluster " + targetCluster.toString());  //Modified by Himanshu

        // Use a thread
        //thread = new MTThread() ;
        // thread.start() ;

        // Use execute to deliver incoming messages.
        newLogMessageSubscription = ( IncrementalSubscription )
                plugIn.getBlackboardService().subscribe( logMessagePredicate ) ;


    }

    FlushTimerThread timerThread ;
    IncrementalSubscription newLogMessageSubscription ;

    public void setPreferences(Map prefs)
    {
    }

    public void execute()
    {
        Collection c = newLogMessageSubscription.getAddedCollection() ;
        for ( Iterator iter = c.iterator(); iter.hasNext() ; ) {
            LogMessage lm = ( LogMessage ) iter.next() ;
            System.out.println( "ServerBlackboardMTImpl::Receiving message of type " + lm.getClass() + " from " + lm.getSource() );
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

            // Remove this from the BB
            plugIn.getBlackboardService().publishRemove( lm ) ;
        }
    }

    private void processPDU( PDU pdu ) {
        // Process PDU.
    }

    /**
     * This is slightly misleading, because it does not wait to see whether or not any acknowledgement
     * of the connect is done.
     */
    public boolean connect()
    {
        timerThread = new FlushTimerThread() ;
        timerThread.start() ;
        sendMessage( new DeclarePDU( plugIn.getIdentifier().toString() ));
        return true ;
    }

    public boolean isConnected()
    {
        return true ;
    }

    public void stop()
    {
        timerThread.stop = true ;
    }

    public Object getServer()
    {
        return null;
    }

    public void setPDUSink(PDUSink sink)
    {
        this.sink = sink ;
    }

    public synchronized void sendMessage(PDU pdu)
    {
        if ( pdu instanceof DeclarePDU ) {
            flush() ;
            sendWrappedMessage( pdu ) ;
        }
        else {
            try
            {
                oos.writeObject(pdu);
                if (bas.size() > maxBatchSize)
                {
                    flush();
                }
            }
            catch (IOException e)
            {

            }
        }

    }

    private MessageAddress getAddressFromConfig() {      //Modified by Himanshu
        Collection params = plugIn.getParameters() ;
        Vector paramVector = new Vector( params ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        // DEBUG
        // System.out.printlbn( "Configuring PlanEventLogPlugIn from " + fileName ) ;

        //ClusterIdentifier clusterId = null ;
        MessageAddress clusterId = null;

        try {
            String clusterName = null ;
            if ( fileName != null && finder != null ) {


                File f = finder.locateFile( fileName ) ;

                // DEBUG -- Replace by call to log4j
	            System.out.println( "Configuring PlanMonitorPlugIn from " + f ) ;

                if ( f != null && f.exists() ) {
                    //
                    // Now, parse the config file
                    //

                    Document doc = null ;
                    try {
                        doc = finder.parseXMLConfigFile(fileName);
                    }
                    catch ( Exception e ) {
                        System.out.println( e ) ;
                    }

                    if ( doc != null ) {
                        try {
                            Node root = doc.getDocumentElement() ;
                            if( root.getNodeName().equals( "plpconfig" ) ) {
                                NodeList nodes = doc.getElementsByTagName( "PlanLogAgent" );
                                for (int i=0;i<nodes.getLength();i++) {
                                    Node n = nodes.item(i) ;
                                    clusterName = n.getAttributes().getNamedItem( "identifier" ).getNodeValue() ;
                                }
                            }
                            else {
                                // DEBUG -- replace with log4j
                                System.out.println( "Warning:: Plan log config file is invalid." ) ;
                            }
                        }
                        catch ( Exception e ) {
                            System.out.println( e ) ;
                        }
                    }

                }

            }

            if ( clusterName != null ) {
                //clusterId = new ClusterIdentifier(clusterName) ;
                clusterId = MessageAddress.getMessageAddress(clusterName); //Himanshu
            }

        } catch ( Exception e ) {
            e.printStackTrace() ;
        }

        return clusterId ;
    }

    protected void sendWrappedMessage( PDU pdu ) {
        BlackboardService bs = plugIn.getBlackboardService() ;
        WrappedPDUMessage msg = new WrappedPDUMessage( pdu ) ;
        msg.setDestination( targetCluster );
        if ( bs != null ) {
           flush() ;
           bs.publishAdd( msg ) ;
        }
        else {
           outList.add( msg ) ;
        }
    }

    public synchronized void synchflush() {
        plugIn.getBlackboardService().openTransaction();
        flush() ;
        plugIn.getBlackboardService().closeTransaction();
    }

    /**
     * Flush.  This may be asynchronous since BlackboardClientMTImpl has a period
     * flush client.
     */

    public void flush()
    {
        lastAddTime = plugIn.currentTimeMillis() ;
        if ( bas.size() == 0 ) {
            return ;
        }

        try {
            // Make a copy of the byte array.
            byte[] ba = bas.toByteArray();
            bas.reset();
            oos.reset();

            BatchMessage bm = new BatchMessage(ba, BatchMessage.SERIALIZED, false);
            bm.setDestination( targetCluster );
            BlackboardService bs = plugIn.getBlackboardService() ;
            if ( bs != null ) {
                // Clear the outlist.
                if ( outList.size() > 0 )
                {
                    for (int i=0;i<outList.size();i++) {
                        bs.publishAdd( outList.get(i) ) ;
                    }
                    outList.clear();
                }
                bs.publishAdd( bm ) ;
            }
            else {
                outList.add( bm ) ;
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    ConfigFinder finder ;
    PlanMonitorPlugIn plugIn ;
    PDUSink sink ;
    //ClusterIdentifier targetCluster ;   //Himanshu
    MessageAddress targetCluster; //Himanshu

    long lastAddTime = 0L ;

    /**
     * Maximum delay in seconds before messages are flushed. Default is 10 seconds.
     */
    long maximumDelay = 10000;

    /**
     * Maximum batch size in bytes. Default is 100kb.
     */
    int maxBatchSize = 102400;
    ObjectOutputStream oos;
    ByteArrayOutputStream bas ;

    /**
     * Temporary buffer for outgoing LogMessages.  Really should not be filled unless
     * the blackboard service is unavailable.
     */
    ArrayList outList = new ArrayList( 7 ) ;

}