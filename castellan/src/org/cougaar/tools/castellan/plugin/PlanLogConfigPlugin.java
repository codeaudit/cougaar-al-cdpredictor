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
  *  8/15/01 Initial version  by IAI
  */

package org.cougaar.tools.castellan.plugin;

import org.cougaar.core.plugin.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.agent.service.alarm.*;
import org.cougaar.core.blackboard.*;
import org.cougaar.core.service.*;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.node.NodeIdentificationService;
import org.cougaar.util.*;
import org.w3c.dom.*;
import org.cougaar.tools.castellan.planlog.*;
import org.cougaar.tools.castellan.ldm.*;
import org.cougaar.tools.castellan.pdu.EventPDU;

import java.util.*;
import java.io.*;

/**
 * Loads logging configuration information on a per agent basis and publishes this
 * to the blackboard.
 */
public class PlanLogConfigPlugin extends ComponentPlugin
{
    class FlushAlarm implements PeriodicAlarm {
        public FlushAlarm( long expTime )
        {
            this.expTime = expTime;
        }

        public void reset( long currentTime )
        {
            expTime = currentTime + delay ;
            expired = false ;
        }

        public long getExpirationTime()
        {
            return expTime ;
        }

        public void expire()
        {
            expired = true ;
            BlackboardService bs = getBlackboardService() ;
            bs.openTransaction();
            FlushObject fo = null ;
            if ( flushedObject == null ) {
                flushedObject = new FlushObject( System.currentTimeMillis() ) ;
                bs.publishAdd( flushedObject ) ;
            }
            flushedObject.setTime( System.currentTimeMillis() );
            bs.publishChange( flushedObject ) ;
            bs.closeTransaction();
        }

        public boolean hasExpired()
        {
            return expired ;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired=true;
            return was;
        }

        FlushObject flushedObject ;
        boolean stop = false ;
        boolean expired = false ;
        long expTime ;
        long delay = 5000L ;
    }


    class TimerThread extends Thread {
        public void run()
        {
            while ( !stop ) {
                try {
                sleep( flushInterval ) ;
                }
                catch ( Exception e ) {
                }
                BlackboardService bs = getBlackboardService() ;
                bs.openTransaction();
                FlushObject fo = null ;
                bs.publishAdd( fo = new FlushObject( currentTimeMillis() ) ) ;
                if ( previousFlushedObject != null ) {
                    bs.publishRemove( previousFlushedObject ) ;
                }
                previousFlushedObject = fo ;
                bs.closeTransaction();
            }
        }

        FlushObject previousFlushedObject ;
        boolean stop = false ;
    }

    public void setupSubscriptions()
    {
        ServiceBroker broker = getServiceBroker() ;
        log = ( LoggingService ) broker.getService( this, LoggingService.class, null ) ;

        //BlackboardTimestampService bts =
        //        ( BlackboardTimestampService ) broker.getService( this, BlackboardTimestampService.class, null ) ;

        //if ( bts == null ) {
        //     System.out.println("BlackboardTimestampService does not exist.");
        //}

        // Publish configuration information.
        config = getConfigInfo() ;

        if ( config != null ) {
            BlackboardService b = getBlackboardService() ;
            System.out.println( "PlanLogConfigPlugIn:: Publishing configuration file for cluster " + getClusterIdentifier() );
            b.publishAdd( config ) ;
        }
        else {
            System.out.println( "PlanLogConfigPlugIn:: No plan log configuration file available." );
        }

        // Disable batching
        // Publish periodic flush messages to the LP
        //Thread t = new TimerThread() ;
        //t.start();
        // Set up a timer to fire every 5 seconds.
        getAlarmService().addRealTimeAlarm( new FlushAlarm( System.currentTimeMillis() + 5000 ) ) ;
    }

    protected PlanLogConfig getConfigInfo() {
        Collection params = getParameters() ;
        Vector paramVector = new Vector( params ) ;

        String fileName = null ;
        if ( paramVector.size() > 0 ) {
            fileName = ( String ) paramVector.elementAt(0) ;
        }

        // DEBUG
        // System.out.printlbn( "Configuring PlanEventLogPlugIn from " + fileName ) ;

        ConfigFinder finder = getConfigFinder() ;
        ClusterIdentifier clusterId = null ;
        PlanLogConfig config = new PlanLogConfig() ;

        ServiceBroker sb = getServiceBroker() ;
        NodeIdentificationService nis = ( NodeIdentificationService )
                sb.getService( this, NodeIdentificationService.class, null ) ;
        config.setNodeIdentifier( nis.getNodeIdentifier().cleanToString() ) ;

        try {
            String clusterName = null ;
            if ( fileName != null && finder != null ) {


                File f = finder.locateFile( fileName ) ;

                // DEBUG -- Replace by call to log4j
                if ( log != null && log.isInfoEnabled() ) {
                    log.info( "PlanLogConfigPlugin:: Configuring PlanLogConfig from " + f );
                }
                System.out.println( "PlanLogConfigPlugin:: Configuring PlanLogConfig from " + f ) ;

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

                                // Get target plan log
                                for (int i=0;i<nodes.getLength();i++) {
                                    Node n = nodes.item(i) ;
                                    clusterName = n.getAttributes().getNamedItem( "identifier" ).getNodeValue() ;
                                    System.out.println( "Found identifier=" + clusterName );
                                }
                                if ( clusterName != null ) {
                                    config.setLogCluster( clusterName );
                                }

                                // Get logging level
                                //nodes = doc.getElementsByTagName( PlanLogConstants.AR_LOG_DETAIL ) ;


                                // Get whether to log epochs
                                //nodes = doc.getElementsByTagName( PlanLogConstants.LOG_EPOCHS ) ;

                                // Get whether to log trigger executes on plug-ins.
                            }
                            else {
                                // DEBUG -- replace with log4j
                                System.out.println( "Warning:: Plan log config file is invalid, no root node \"plpconfig\"" ) ;
                            }
                        }
                        catch ( Exception e ) {
                            if ( log.isErrorEnabled() ) {
                                log.error( "Exception thrown parsing configuration file " + f );
                            }
                            System.out.println( e ) ;
                        }
                    }

                }

            }

        } catch ( Exception e ) {
            e.printStackTrace() ;
        }

        if ( config.getLogCluster() == null ) {
            System.out.println( "Warning:: No configuration information found for " + getClusterIdentifier() ) ;
        }
        else {
            if ( config.getLogCluster().equals( getClusterIdentifier().toString() ) ) {
            // DEBUG
                System.out.println( "Setting " + getClusterIdentifier() + " as server." );
                config.setServer( true );
            }
        }

        return config ;

    }

    public void execute()
    {
    }

    protected transient LoggingService log ;
    protected PlanLogConfig config ;

    protected long flushInterval = 7500L ;
}
