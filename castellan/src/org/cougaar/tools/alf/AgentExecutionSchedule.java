package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.pdu.UIDPDU;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Different observed schedule is maintained for each agent.  No global schedule is
 * maintained.
 */
public class AgentExecutionSchedule extends Schedule {

    static class TaskEvent {

    }

    static class IncomingTaskEvent extends TaskEvent {
        public IncomingTaskEvent(UIDPDU uid, UIDPDU parent, UIDPDU[] children) {
            this.uid = uid;
            this.parent = parent;
            this.children = children;
        }

        public UIDPDU getUid() {
            return uid;
        }

        public UIDPDU getParent() {
            return parent;
        }

        public UIDPDU[] getChildren() {
            return children;
        }

        /**
         * This task.
         */
        protected UIDPDU uid ;

        /**
         * Parent task from outside agent.
         */
        protected UIDPDU parent ;

        /**
         * All generated children going to the outside world or
         * allocated to assets.
         */
        protected UIDPDU[] children ;

        /**
         * Assets to which the outgoing tasks have been allocated. These
         * must be real non-fake assets.
         */
        protected UIDPDU[] assets ;
    }

    static class OutgoingTaskEvent extends TaskEvent {
        public OutgoingTaskEvent(UIDPDU uid, UIDPDU incoming, UIDPDU child) {
            this.uid = uid;
            this.incoming = incoming;
            this.child = child;
        }

        public UIDPDU getUid() {
            return uid;
        }

        public UIDPDU getIncoming() {
            return incoming;
        }

        public UIDPDU getChild() {
            return child;
        }

        /**
         * This task.
         */
        protected UIDPDU uid ;

        /**
         * Parent incoming task from this agent.
         */
        protected UIDPDU incoming ;

        /**
         * Direct child in target agent.
         */
        protected UIDPDU child ;
    }

    static class BatchedEvent extends AbstractPeriodEvent {
        public static final int INCOMING = 0 ;
        public static final int OUTGOING = 1 ;

        public BatchedEvent( long start, long end ) {
            super( start, end ) ;
        }

        public void addEvent( TaskEvent e ) {
            events.add( e ) ;
        }

        public Iterator getEvents() {
            return events.iterator();
        }

        /**
         * A set of events.
         */
        protected ArrayList events = new ArrayList(20) ;
    }

}