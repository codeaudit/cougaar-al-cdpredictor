package com.axiom.lib.plot ;
import java.util.Hashtable ;
import java.util.Enumeration ;

public abstract class AbstractPlot implements Plottable {

    public Enumeration getProperties() {
        return properties.elements() ;
    }

    public void setProperty( String key, Object o ) {
        properties.put( key, o ) ;
    }

    public Object removeProperty( String key ) {
        return properties.remove( key ) ;
    }

    public Object getProperty( String key ) {
        return properties.get( key ) ;
    }

    protected Hashtable properties = new Hashtable() ;

}