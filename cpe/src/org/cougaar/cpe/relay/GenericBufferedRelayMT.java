package org.cougaar.cpe.relay;

import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.core.mts.MessageAddress;

import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

/**
 * A unified single message queue for agents.
 */
public class GenericBufferedRelayMT
{
    static class UnifiedSourceBufferRelay implements Relay.Source {
        private MessageAddress target;

        public UnifiedSourceBufferRelay(UID uid, MessageAddress target, MessageAddress source) {
            targets = Collections.singleton(target);
            this.source = source;
            this.target = target ;
            this.uid = uid;
        }

        public String toString() {
            return ("< Source Buffer, target=" + getTargets() +
                    ", source= " + source + ", #outgoing=" + outgoing.size() + "> " );
        }

        public Set getTargets() {
            return targets;
        }

        public UID getUID() {
            return uid;
        }

        public MessageAddress getSource() {
            return source;
        }

        public MessageAddress getTarget() {
            return target ;
        }

        public boolean isOutgoingChanged() {
            return isOutgoingChanged;
        }

        public Object getContent() {
            // System.out.println("SourceBufferRelay::getContent() called on " + this );
            Object[] c = outgoing.toArray() ;
            outgoing.clear();
            isOutgoingChanged = false ;
            return c ;
        }

        public void setUID(UID uid) {
            throw new RuntimeException("Trying to set uid on existing relay.");
        }

        public Relay.TargetFactory getTargetFactory() {
            return UnifiedSourceBufferRelay.SimpleRelayFactory.INSTANCE;
        }

        static final Object[] nullArray = new Object[0] ;

        /**
         * Get responses.
         */
        public synchronized Object[] clearReponses() {
            if ( responses.size() == 0 ) {
                return nullArray ;
            }

            Object[] result = new Object[responses.size()];
            for (int i = 0 ; i < responses.size() ; i++) {
                result[i] = responses.get(i);
            }
            responses.clear();
            return result;
        }

        public void addOutgoing( Serializable o ) {
            outgoing.add( o ) ;
            isOutgoingChanged = true ;
        }

        protected void processReponse(Object[] resp) {
            for (int i = 0 ; i < resp.length ; i++) {
                parent.buffer.addMessage( resp[i] );
            }
        }

        public int updateResponse(MessageAddress address, Object o) {
            processReponse((Object[]) o);
            return Relay.RESPONSE_CHANGE ;
        }

        private static final class SimpleRelayFactory implements TargetFactory, Serializable {
            public static final UnifiedSourceBufferRelay.SimpleRelayFactory INSTANCE =
                    new UnifiedSourceBufferRelay.SimpleRelayFactory();

            private SimpleRelayFactory() {
            }

            public Relay.Target create(
                    UID uid,
                    MessageAddress source,
                    Object content,
                    Token token) {
                return new TargetBufferRelay(uid, source, content);
            }

            private Object readResolve() {
                return INSTANCE;
            }
        };

        protected boolean isOutgoingChanged = false ;
        protected ArrayList outgoing = new ArrayList( 50 );
        protected ArrayList responses = new ArrayList( 50 );
        protected UID uid;
        protected MessageAddress source;
        protected Set targets;

        GenericBufferedRelayMT parent ;
    }


    DummyBufferedRelay buffer = new DummyBufferedRelay() ;
}
