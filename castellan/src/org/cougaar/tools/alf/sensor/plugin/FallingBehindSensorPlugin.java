/*
 * This plugin is used to log PDUs in BlackBoard and pass them to FallingBehindSensor.
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
import org.cougaar.tools.castellan.pdu.PDU;
import org.cougaar.tools.castellan.pdu.EventPDU;
// import falling behind sensor class.
import org.cougaar.tools.alf.sensor.FallingBehindSensor;
import java.util.Iterator;
import java.util.Collection;
import java.util.Properties;

public class FallingBehindSensorPlugin extends ComponentPlugin
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
            flushBuffer();
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

        BlackboardService bs = getBlackboardService();
        pduBufferSubscription = (IncrementalSubscription) bs.subscribe(pduBufferPredicate);

        // Create a falling behind sensor.
        fbsensor = new FallingBehindSensor() ;

        AlarmService as = getAlarmService() ;
        as.addAlarm( new TriggerFlushAlarm( currentTimeMillis() + 1000 ) ) ;
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
                // Now, flush out the buffer into an FallingBehindSensor object
                if (buffer.getSize() > 0)
                {
                    PDU pdu;
                    for (Iterator iter = buffer.getIncoming() ; iter.hasNext() ;)
                    {
                         pdu = (PDU)iter.next();
                         // Add PDU to Falling Behind Sensor.  
                         fbsensor.add(pdu);
                    }
                    // Update sensor status after PDUs in buffer are all added.
                    fbsensor.update();
                }
                buffer.clearIncoming();
            }
        }
    }

    PDUBuffer buffer;
    LoggingService log ;
    IncrementalSubscription pduBufferSubscription;
    // Declare Falling Behind Seensor.
    FallingBehindSensor fbsensor;
}
