package org.cougaar.cpe.relay;

import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;

import java.util.ArrayList;

/**
 * User: wpeng
 * Date: Sep 15, 2003
 * Time: 12:19:30 PM
 */
public class DummyBufferedRelay implements java.io.Serializable, Relay
{
    private UID uid;

    public UID getUID()
    {
        return uid ;
    }

    public void setUID(UID uid)
    {
        this.uid = uid ;
    }

    public void addMessage( Object msg ) {
        list.add( msg ) ;
    }

    public ArrayList clearList()
    {
        ArrayList result = list ;
        list = new ArrayList( list.size() ) ;
        return result ;
    }

    public boolean isChanged()
    {
        return isChanged;
    }

    public void setChanged(boolean changed)
    {
        isChanged = changed;
    }

    private boolean isChanged = false ;
    private ArrayList list = new ArrayList();
}
