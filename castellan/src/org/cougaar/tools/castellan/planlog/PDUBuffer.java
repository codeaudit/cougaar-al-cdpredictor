package org.cougaar.tools.castellan.planlog;

import org.cougaar.tools.castellan.pdu.PDU;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Buffer up incoming PDUs on the blackboard.  Allow plug-ins to
 * handle them as they please.
 */
public class PDUBuffer implements java.io.Serializable
{
    public int getSize() {
        return list.size() ;
    }

    public void addPDU( PDU pdu ) {
        list.add( pdu ) ;
    }

    public Iterator iterator() {
        return list.iterator() ;
    }

    public void clear() {
        list.clear();
    }

    ArrayList list = new ArrayList( 1000 ) ;
}
