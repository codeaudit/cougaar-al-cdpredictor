/*
 * <copyright>
 *  Copyright 2002 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */


package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.*;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.util.*;
import org.cougaar.tools.castellan.ldm.*;
import org.cougaar.tools.castellan.server.ServerBlackboardMTImpl;
import org.cougaar.tools.castellan.planlog.PDUBuffer;
import org.cougaar.tools.castellan.planlog.InMemoryEventLog;
import org.cougaar.tools.castellan.planlog.DBEventLog;
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.EventPDU;
import org.cougaar.tools.alf.sensor.TheSensor;
import org.cougaar.core.adaptivity.InterAgentOperatingMode;
import org.cougaar.core.agent.ClusterIdentifier;
import org.cougaar.core.adaptivity.OMCRangeList;

import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;

/**
 *
 * @author  Yunho Hong
 * @version
 */

public class SensorPlugin extends ComponentPlugin
{
    class TriggerFlushAlarm implements PeriodicAlarm
    {
        public TriggerFlushAlarm(long expTime)
        {
            this.expTime = expTime;
        }

        public void reset(long currentTime)
        {
            expTime = currentTime + delay;
            expired = false;
        }

        public long getExpirationTime()
        {
            return expTime;
        }

        public void expire()
        {
            expired = true;
		getBlackboardService().openTransaction();
            flushBuffer();
		getBlackboardService().closeTransaction();
        }

        public boolean hasExpired()
        {
            return expired;
        }

        public boolean cancel()
        {
            boolean was = expired;
            expired = true;
            return was;
        }

        boolean expired = false;
        long expTime;
        long delay = 1000;
    }


    /**
     * Extracts received log messages.
     */
    UnaryPredicate logMessagePredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            if (!( o instanceof LogMessage ))
            {
                return false;
            }
            LogMessage lm = (LogMessage) o;
            return ( !lm.isLocal() && !lm.isRead() );
        }
    };

    /**
     * Extracts received log messages.
     */
    UnaryPredicate pduBufferPredicate = new UnaryPredicate()
    {
        public boolean execute(Object o)
        {
            return o instanceof PDUBuffer;
        }
    };

    public void setupSubscriptions()
    {
        ServiceBroker broker = getServiceBroker();
        log = (LoggingService) broker.getService(this, LoggingService.class, null);

        bs = getBlackboardService();
        pduBufferSubscription = (IncrementalSubscription) bs.subscribe(pduBufferPredicate);
        Collection c = getParameters();

        Properties props = new Properties() ;
        // Iterate through the parameters
        int count = 0;
        for (Iterator iter = c.iterator() ; iter.hasNext() ;)
        {
            String s = (String) iter.next();
            if ( count == 0 && s.equals("true") )
            {
                log.info("Logging to memory event log");
                logToMemory = true;
            }
            else if ( count == 1 && s.equals("true") )
            {
                log.info("Logging to database event log" ) ;
                logToDatabase = true;
            }
            else if ( count == 2 ) {
                props.put( "dbpath", s ) ;
            }
            else if ( count == 3 ) {
                props.put( "user", s ) ;
            }
            else if ( count == 4 ) {
                props.put( "password", s ) ;
            }
            count++;
        }

        // Create event logs.
        System.out.println("LogServerPlugin:: Hostname=" + props.getProperty("dbpath") + ",username=" + props.getProperty("user") ) ;
        if (logToMemory)
        {
//            memoryLog = new InMemoryEventLog();
//            getBlackboardService().publishAdd(memoryLog);
   		  sensor = new TheSensor(this);			// Hong
        }

        if (logToDatabase)
        {
            String databaseName = getDatabaseName();
            try
            {
                DBLog = new DBEventLog(databaseName, props);
            }
            catch (Exception e)
            {
                if (log.isWarnEnabled())
                {
                    log.error("Could not open \"" + databaseName + "\" persistent database for writing.");
                }
                System.out.println(e);
                e.printStackTrace();
            }
            log.info( "Opened " + databaseName + " for writing." );
            System.out.println( "LogServerPlugin::Opened " + databaseName + " for writing." );
        }

        AlarmService as = getAlarmService() ;
        as.addAlarm( new TriggerFlushAlarm( currentTimeMillis() + 1000 ) ) ;

    }

    protected String getDatabaseName()
    {
        return getClusterIdentifier().cleanToString() + currentTimeMillis();
    }

    public void execute()
    {
        if (buffer == null)
        {
            for (Iterator iter = pduBufferSubscription.getAddedCollection().iterator() ; iter.hasNext() ;)
            {
                Object o = iter.next();
                if (o instanceof PDUBuffer)
                {
                    buffer = (PDUBuffer) o;
                    break;
                }
            }
        }

        // Immediately flush the buffer.
        flushBuffer();
    }

    private void flushBuffer()
    {
        if (buffer != null)
        {
            synchronized (buffer)
            {
                // Now, flush out the buffer into an InMemoryEventLog object
                if (buffer.getSize() > 0)
                {
                    if (logToMemory)
                    {
                        for (Iterator iter = buffer.getIncoming() ; iter.hasNext() ;)
                        {
  				        sensor.add((PDU) iter.next());		// Hong
//                                fbsensor.add(pdu);
				}
			  // Update sensor status after PDUs in buffer are all added.
//                    fbsensor.update();
//			  for (i=0; i<=fbsensor.clusters-1; i++) {
//				if (status[i]!=fbsensor.state[i]) {
//				    status[i]=fbsensor.state[i]; 
//				    psu_fb[i].setValue(new Integer(status[i]));
//				    bs.publishChange(psu_fb[i]);
//				}
//			  }

                    }
                    if (logToDatabase)
                    {
                        if (DBLog != null)
                        {
                            for (Iterator iter = buffer.getIncoming() ; iter.hasNext() ;)
                            {
                                DBLog.add((PDU) iter.next());
                            }
                        }
                    }
                }
                buffer.clearIncoming();
            }
        }
    }

    public void publishAdd(InterAgentOperatingMode a) {

		bs.publishAdd(a);

    }

    public void publishChange(InterAgentOperatingMode a) {

		bs.publishChange(a);
    }

    public UIDService getUIDService() {

	 return (UIDService) getServiceBroker().getService(this, UIDService.class, null);
    }

    String databaseName = null;
    boolean logToMemory = true, logToDatabase = false;

    DBEventLog DBLog;
    PDUBuffer buffer;
    LoggingService log ;
    IncrementalSubscription pduBufferSubscription;

// Feedback
// Yunho and Hari's Loadforecaster and Falling behind sensor
   TheSensor sensor;		
   InterAgentOperatingMode[] psu_fb;
   InterAgentOperatingMode[] psu_lf3;
   int[] prev_state;
   BlackboardService bs;

// Seokcheon's Loadforecaster and Falling behind sensor
//    FallingBehindSensor fbsensor;
//    InterAgentOperatingMode[] psu_fb;
//    int[] status;
}
