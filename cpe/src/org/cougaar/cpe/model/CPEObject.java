package org.cougaar.cpe.model;

import java.io.Serializable;

public abstract class CPEObject implements Serializable
{
    /**
     * A unique id for each entity.
     */
    protected String worldId ;

    public CPEObject(String worldId)
    {
        this.worldId = worldId;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[" ) ;
        toString( buf );
        buf.append( "]" ) ;
        return buf.toString() ;
    }

    public void toString( StringBuffer buf ) {
        buf.append( "type=") ;
        String name = getClass().getName() ;
        buf.append(  name.substring( name.lastIndexOf('.') + 1, name.length() ) ) ;
        buf.append( ",id=" ) ;
        buf.append( worldId ) ;
    }

    public String getId() {
        return worldId;
    }

}
