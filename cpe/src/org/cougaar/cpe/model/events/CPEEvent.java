package org.cougaar.cpe.model.events;

import java.io.Serializable;

public abstract class CPEEvent implements Serializable
{

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( '[' ) ;
        String s = getClass().getName() ;
        buf.append( s.substring( s.lastIndexOf( '.' ) + 1, s.length() ) ) ;
        outputParamString( buf ) ;
        buf.append( ']' );
        return buf.toString() ;
    }

    protected void outputParamString( StringBuffer buf ) {
    }

    protected CPEEvent(long time)
    {
        this.time = time;
    }

    public long getTime()
    {
        return time;
    }

    long time ;
}
