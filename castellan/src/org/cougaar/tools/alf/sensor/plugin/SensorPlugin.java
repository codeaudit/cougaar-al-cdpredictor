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

// Hong : Begin
package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.tools.alf.sensor.TheSensor;
import org.cougaar.core.adaptivity.InterAgentOperatingMode;
//import org.cougaar.core.agent.ClusterIdentifier; Changed by Himanshu
import org.cougaar.core.agent.*; //Added by Himanshu
import org.cougaar.core.mts.MessageAddress;//Added by Himanshu
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.tools.castellan.planlog.DBEventLog;
//import org.cougaar.core.adaptivity.SensorCondition;
// Hong : End

import org.cougaar.core.plugin.*;
import org.cougaar.core.domain.*;
import org.cougaar.core.component.*;
import org.cougaar.core.service.*;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.util.*;
import org.cougaar.tools.castellan.ldm.*;
import org.cougaar.tools.castellan.server.ServerBlackboardMTImpl;
import org.cougaar.tools.castellan.planlog.PDUBuffer;
import org.cougaar.tools.castellan.planlog.InMemoryEventLog;
import org.cougaar.tools.castellan.planlog.PersistentEventLog;
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.EventPDU;


import java.util.*;

public class SensorPlugin extends ComponentPlugin
{

    /**
     * Thread used to periodically flush the buffer.
     */
    class FlushThread extends Thread {

        public void run ()
        {
            while ( true ) {
                try {
                    Thread.sleep( 2000 );
                }
                catch ( InterruptedException e ) {
					e.printStackTrace() ; 
                }

                flushBuffer() ;
            }
        }

        long delay = 2000 ;
    }

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
			// Hong : Begin
			getBlackboardService().openTransaction();
			// Hong : End
            flushBuffer();
			// Hong : Begin
			getBlackboardService().closeTransaction();
			// Hong : End
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
    protected UnaryPredicate logMessagePredicate = new UnaryPredicate()
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
    protected UnaryPredicate pduBufferPredicate = new UnaryPredicate()
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

		// Hong : Begin
		String fbtype = "LookupTable";
        bs = getBlackboardService();
		// Hong : End

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
            } // Hong : Begin
			else if ( count == 5 ) {  // Specify type of falling behind sensor, "LookupTable" or "SVs" 
				fbtype = s;
            } // Hong : End

            count++;
        }

		props.put( "dbpath", "jdbc:mysql://localhost/" ) ;
        props.put( "user", "") ;
        props.put( "password", "" ) ;

        // Create event logs.
        log.shout("SensorPlugin:: Hostname=" + props.getProperty("dbpath") + ",username=" + props.getProperty("user") ) ;
        if (logToMemory)
        {
		  // Hong : Begin
          // instantiate a falling behind sensor
          log.shout( "SensorPlugin instantiate sensor " + fbtype);
   		  sensor = new TheSensor(this, fbtype);	
		  // Hong : End
        }

        if (logToDatabase)
        {
            String databaseName = getDatabaseName();
            try
            {
				// Hong : Begin
                DBLog = new DBEventLog(databaseName, props);
				// Hong : End
            }
            catch (Exception e)
            {
                if (log.isErrorEnabled())
                {
                    log.error("Could not open \"" + databaseName + "\" persistent database for writing.");
                }
                log.shout("Could not open database for writing.");
//                log.shout(e);
                e.printStackTrace();
            }
            log.info( "Opened " + databaseName + " for writing." );
            log.shout( "SensorPlugin::Opened " + databaseName + " for writing." );
        }

        //AlarmService as = getAlarmService() ;
        //as.addAlarm( new TriggerFlushAlarm( currentTimeMillis() + 1000 ) ) ;
        flushThread = new FlushThread() ;
        flushThread.start();
    }

    protected String getDatabaseName()
    {

        Date d = new Date( System.currentTimeMillis() ) ;
        // Hard code EST for now.
        String[] ids = TimeZone.getAvailableIDs(-5 * 60 * 60 * 1000);
        SimpleTimeZone est = new SimpleTimeZone(-5 * 60 * 60 * 1000, ids[0]);
        GregorianCalendar calendar = new GregorianCalendar( est ) ;
        calendar.setTime( d );
        int dom = calendar.get( Calendar.DAY_OF_MONTH ) ;
        int moy = calendar.get( Calendar.MONTH ) ;
        int y = calendar.get( Calendar.YEAR ) ;
        int hod = calendar.get( Calendar.HOUR_OF_DAY ) ;
        int min = calendar.get( Calendar.MINUTE ) ;

        StringBuffer buf = new StringBuffer() ;
        if ( moy < 10 ) {
            buf.append( '0' ) ;
        }
        buf.append( moy ) ;
        if ( dom < 10 ) {
            buf.append( '0' ) ;
        }
        buf.append( dom ) ;
        buf.append( y ) ;
        buf.append( '_' ) ;
        buf.append( hod ) ;
        buf.append( min ) ;

        return ((AgentIdentificationService) getBindingSite().getServiceBroker().getService(
                this, AgentIdentificationService.class, null)).getName() + buf.toString();

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
                if (buffer.getIncomingSize() > 0)
                {
                    if (logToMemory)
                    {
                        for (Iterator iter = buffer.getIncoming() ; iter.hasNext() ;)
                        {
							// Hong : Begin
                            sensor.add((PDU) iter.next());		// Falling behind sensor 2 by Hong
                        }
                    }
                    if (logToDatabase)
                    {
                        if (DBLog != null)
                        {
                            for (Iterator iter = buffer.getIncoming() ; iter.hasNext() ;)
                            {
								// Hong : Begin
                                DBLog.add((PDU) iter.next());
                            }
                        }
                    }
                }
                buffer.clearIncoming();
            }
        }

    }

	// Hong : Begin
    public void publishAdd(InterAgentOperatingMode a) {

		bs.publishAdd(a);

    }

    public void publishChange(InterAgentOperatingMode a) {


		bs.openTransaction();  // July 13, 2002

		bs.publishChange(a);

		bs.closeTransaction(); // July 13, 2002
    
	}

    public UIDService getUIDService() {

		return (UIDService) getServiceBroker().getService(this, UIDService.class, null);

    }

	public ConfigFinder obtainConfigFinder() {

		return (ConfigFinder) getConfigFinder();

	}

	BlackboardService bs;
    DBEventLog DBLog;
	TheSensor sensor;
	// Hong : End

    protected FlushThread flushThread ;
    protected String databaseName = null;
    protected boolean logToMemory = true, logToDatabase = true;
    protected InMemoryEventLog memoryLog;
    protected PersistentEventLog persistentLog;
    protected PDUBuffer buffer;
    protected LoggingService log ;
    protected IncrementalSubscription pduBufferSubscription;
}
