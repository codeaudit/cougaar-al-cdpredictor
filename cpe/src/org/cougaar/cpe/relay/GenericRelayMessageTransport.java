package org.cougaar.cpe.relay;

import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.AlarmService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.agent.service.alarm.Alarm;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.techspecs.events.MessageEvent;
import org.cougaar.tools.techspecs.events.ActionEvent;
import org.cougaar.tools.techspecs.events.TimerEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Funnels messages from relays to appropriate plugins.  Manages all sending and receiving of messages through
 * buffered relays.
 *
 */
public class GenericRelayMessageTransport {
    private IncrementalSubscription relaySubscription;
    private BlackboardService bb;
    private ServiceBroker broker;
    private ComponentPlugin component;
    private ArrayList bufferedMessages = new ArrayList() ;
    private LoggingService ls ;

    /**
     * A list of messages sent to this agent.
     */
    private DummyBufferedRelay selfMessages ;
    private HashMap alarms = new HashMap();

    public GenericRelayMessageTransport( ComponentPlugin component, ServiceBroker broker, MessageAddress sourceAgent,
                                         MessageSink sink, BlackboardService bb) {
        this.parentAgent = sourceAgent;
        this.component = component ;
        this.broker = broker ;

        relaySubscription = (IncrementalSubscription) bb.subscribe(new UnaryPredicate() {
            public boolean execute(Object o) {
                if (o instanceof TargetBufferRelay || o instanceof SourceBufferRelay || o instanceof DummyBufferedRelay ) {
                    return true;
                }
                return false;
            }
        });
        this.sink = sink;
        this.bb = bb;

        ls = (LoggingService) broker.getService( component, LoggingService.class, null ) ;

        // Subscribe to a relay to myself. This is a dummy relay that is bypassed under the unified message queue scheme.
        selfMessages = new DummyBufferedRelay() ;
        UIDService service = (UIDService) broker.getService( this, UIDService.class, null ) ;
        selfMessages.setUID( service.nextUID() );
        bb.publishAdd( selfMessages );
    }

    public SourceBufferRelay addRelay( MessageAddress address ) {
        if ( mapToRelay.get( address.getAddress() ) != null ) {
            throw new RuntimeException( "Relay for " + address + " already exists." ) ;
        }
        else {
            UIDService uidService = (UIDService) broker.getService( component, UIDService.class, null ) ;
            SourceBufferRelay result = new SourceBufferRelay( uidService.nextUID(), address, parentAgent ) ;
            mapToRelay.put( address, result ) ;
            bb.publishAdd( result );
            return result ;
        }
    }

    public void addMessage( Object msg, MessageAddress ma ) {
        synchronized ( bufferedMessages ) {
            if (msg instanceof MessageEvent) {
                MessageEvent cmsg = (MessageEvent) msg;
                cmsg.setSource( ma ) ;
            }

            if ( msg instanceof ActionEvent ) {
                ActionEvent ae = (ActionEvent) msg ;
                if ( ae.getPriority() > ActionEvent.PRIORITY_NORMAL ) {

                    // Insert high priority messages. This is inefficient, but we don't expect many messages.
                    for (int i=0;i<bufferedMessages.size();i++) {
                        ActionEvent evt = (ActionEvent) bufferedMessages.get(i) ;
                        if ( evt.getPriority() < ae.getPriority() ) {
                            bufferedMessages.add( i, ae );
                            return ;
                        }
                    }
                    // Just add at the end.
                    bufferedMessages.add( ae ) ;
                }
                else {
                    bufferedMessages.add( ae ) ;
                }
            }
            else {
                bufferedMessages.add( msg ) ;
            }
        }
    }

    public void setAlarm( String callbackName, String alarmId, long delay, boolean isPeriodic )
    {
        if ( alarmId == null ) {
           throw new IllegalArgumentException( "AlarmId is null" ) ;
        }

        if ( alarms.get(alarmId) != null ) {
            throw new IllegalArgumentException( "Alarm Id " + alarmId + " already exists." ) ;
        }

        if ( callbackName == null ) {
            throw new IllegalArgumentException( "Callback name is null" ) ;
        }

        Class handlerClass = component.getClass() ;

        Method m = null ;
        try
        {
            System.out.println("Set alarm for component " + handlerClass.getName() );
            m = handlerClass.getMethod( callbackName, new Class[] { TimerEvent.class } ) ;
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException( "No public method " + callbackName + "(TimerEvent m) found in " + handlerClass.getName() ) ;
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }

        QueuedAlarm alarm = new QueuedAlarm( this, alarmId, callbackName, delay, isPeriodic ) ;
        alarm.setCallbackMethod( m ) ;

        alarms.put( alarm.getAlarmId(), alarm ) ;

        // Now, get the alarm service and add it.
        AlarmService alarmService = (AlarmService) broker.getService( component, AlarmService.class, null ) ;
        alarmService.addRealTimeAlarm( alarm );

        //component.getAlarmService().addRealTimeAlarm( alarm );
    }

    public void clearAlarm( String alarmId ) {
        if ( alarmId == null ) {
            throw new IllegalArgumentException( "AlarmId is null." ) ;
        }

        QueuedAlarm a = (QueuedAlarm) alarms.get( alarmId ) ;

        if ( a != null ) {
            alarms.remove( alarmId ) ;
            a.cancel() ;
            a.setStopped( true ) ;
        }
    }

    /**
     *
     * @return The parent agent
     */
    public MessageAddress getParentAgent()
    {
        return parentAgent;
    }

    public Relay getRelay( MessageAddress address ) {
        return getRelay( address.getAddress() ) ;
    }

    public Relay getRelay( String address ) {
        return (Relay) mapToRelay.get( address ) ;
    }

    /**
     * This is a thread safe way of adding messages to the message transport.
     *
     * @param address
     * @param message
     */
    public void sendMessage(MessageAddress address, Object message) {

        boolean wasClosed = false ;
        if ( !bb.isTransactionOpen() ) {
            bb.openTransaction();
            wasClosed = true ;
        }

        try {

            // Send a message to myself.
            if ( address.equals(parentAgent) ) {

                // Self messages have been overriden by the buffer.
                //selfMessages.addMessage( message );
                if ( !selfMessages.isChanged() ) {
                    this.bb.publishChange( selfMessages );
                    selfMessages.setChanged( true );
                }

                // Add to the local message queue with the following source.
                addMessage( message, parentAgent ) ;
                return ;
            }

            Relay relay = (Relay) mapToRelay.get(address.getAddress());
            if (relay instanceof TargetBufferRelay) {
                TargetBufferRelay tbr = (TargetBufferRelay) relay;
                boolean wasChanged = tbr.isResponseChanged();
                tbr.addResponse((Serializable) message);
                if (!wasChanged) {
                    //System.out.println("MT:: Sending " + message + " to " + address);
                    this.bb.publishChange(relay);
                }
            } else if (relay instanceof SourceBufferRelay) {
                SourceBufferRelay sbr = (SourceBufferRelay) relay;
                boolean wasChanged = sbr.isOutgoingChanged();
                sbr.addOutgoing((Serializable) message);
                if (!wasChanged) {
                    // System.out.println("MT:: Sending " + message + " to " + address);
                    this.bb.publishChange(relay);
                }
            } else {
                throw new RuntimeException("Relay to message address " + address + " could not be found for agent " + parentAgent.getAddress() );
            }
        }
        finally {
            if ( wasClosed ) {
               bb.closeTransaction();
            }
        }

    }

    public void execute(BlackboardService bb)
    {

        // Handle any new relays showing up on the BB.
        Collection c = relaySubscription.getAddedCollection();
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            relays.add(o);
            if (o instanceof TargetBufferRelay) {
                TargetBufferRelay tbr = (TargetBufferRelay) o;
                Relay relay = (TargetBufferRelay) mapToRelay.get(tbr.getSource().getAddress());
                if (relay != null) {
                    ls.warn("GenericRelayMT:: WARNING, found duplicate TargetBufferRelay to " + tbr.getSource());
                    continue;
                }
                tbr.setRelayManager( this );
                mapToRelay.put(tbr.getSource().getAddress(), tbr);
            } else if (o instanceof SourceBufferRelay) {
                SourceBufferRelay sbr = (SourceBufferRelay) o;
                Relay relay = (Relay) mapToRelay.get(sbr.getTarget().getAddress());
                if (relay != null) {
                    ls.warn("GenericRelayMT:: WARNING, found duplicate SourceBufferRelay to " + sbr.getSource());
                    continue;
                }
                mapToRelay.put(sbr.getTarget().getAddress(), sbr);
                sbr.setRelayManager( this );
            }
        }

        // Process inbound messages that have already been buffered.
        for (int i=0;i<bufferedMessages.size();i++) {
            Object o = bufferedMessages.get(i) ;

            // Handle timer messages specially by invoking their callback.
            if ( o instanceof TimerMessage ) {
                TimerMessage tm = (TimerMessage) o ;
                QueuedAlarm qa = tm.getQueuedAlarm() ;
                Method m = qa.getCallbackMethod() ;
                System.out.print("*");
                //System.out.println("GMRT::Firing alarm=" + qa);

                // Now, do the invocation of the alarm within this plugin.
                try
                {
                    m.invoke( component, new Object[] { tm } ) ;
                }
                catch ( Exception e)
                {
                    e.printStackTrace();
                }

                // Reschedule the timer if it hasn't been stopped yet.
                if ( qa.isPeriodic() && !qa.isStopped() ) {
                    //System.out.println("GMRT::Resetting periodic timer.");
                    AlarmService as = (AlarmService) broker.getService( component, AlarmService.class, null ) ;
                    qa = (QueuedAlarm) qa.clone() ;
                    alarms.put( qa.getAlarmId(), qa ) ;
                    qa.reset( System.currentTimeMillis() );
                    as.addRealTimeAlarm( qa );
                }
            }
            else {
                System.out.print("#");
                try {
                    sink.processMessage(o);
                }
                catch ( Exception e ) {
                    System.err.println( "Error processing message." ) ;
                    e.printStackTrace();
                }
            }
        }
        bufferedMessages.clear();

//        boolean wasOpen = false ;
//        if ( bb.isTransactionOpen() ) {
//            wasOpen = true ;
//            bb.closeTransaction();
//        }

        // Process messages from myself. This is now been deprecated since
        // messages to myself go directly into the buffer.
        ArrayList list = selfMessages.clearList() ;
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i) ;
            if ( o instanceof MessageEvent ) {
                MessageEvent cmsg = (MessageEvent) o ;
                cmsg.setSource( parentAgent );
            }
            try {
                sink.processMessage(o);
            }
            catch ( Exception e ) {
                System.err.println("Error processing message:  Relay to SELF ERROR PROCESSING MESSAGE " + o ) ;
                e.printStackTrace();
            }
        }
        selfMessages.setChanged( false );

        /**
         * Since relays are duplex, we have to process both of TargetBufferRelays and SourceBufferRelays.
         */
        for (int i = 0; i < relays.size(); i++) {
            Relay relay = (Relay) relays.get(i);
            if (relay instanceof TargetBufferRelay) {
                TargetBufferRelay tbr = (TargetBufferRelay) relay;
                Object[] msgs = tbr.clearIncoming();
                for (int j = 0; j < msgs.length; j++) {
                    Object msg = msgs[j];
                    if (msg instanceof MessageEvent) {
                        MessageEvent cmsg = (MessageEvent) msg;
                        cmsg.setSource(tbr.getSource());
                    }
                    try {
                        sink.processMessage(msg);
                    }
                    catch ( Exception e ) {
                        System.err.println("GenericRelayMessageTransport: Relay from " + tbr.getSource() + " ERROR PROCESSING MESSAGE " + msg );
                        e.printStackTrace();
                    }
                }
            } else if (relay instanceof SourceBufferRelay) {
                SourceBufferRelay sbr = (SourceBufferRelay) relay;
                Object[] msgs = sbr.clearReponses();
                for (int j = 0; j < msgs.length; j++) {
                    Object msg = msgs[j];
                    if (msg instanceof MessageEvent) {
                        MessageEvent cmsg = (MessageEvent) msg;
                        cmsg.setSource(sbr.getTarget());
                    }
                    try {
                        sink.processMessage(msg);
                    }
                    catch ( Exception e ) {
                        System.err.println("Error processing message:  Relay to " + sbr.getSource() + " ERROR PROCESSING MESSAGE " + msg ) ;
                        e.printStackTrace();
                    }
                }
            }
        }

//        if ( wasOpen ) {
//            bb.openTransaction();
//        }

    }

    MessageSink sink;
    HashMap mapToRelay = new HashMap();
    ArrayList relays = new ArrayList();
    MessageAddress parentAgent;
}
