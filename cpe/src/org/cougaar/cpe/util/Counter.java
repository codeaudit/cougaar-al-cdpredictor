package org.cougaar.cpe.util;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class Counter
{

    public Counter()
    {
        this( 10 ) ;
    }

    public Counter( int cap )
    {
        map = new HashMap( cap ) ;
    }

    public void add( Object key ) {
        Integer ivalue =  (Integer) map.get( key ) ;
        if ( ivalue == null ) {
            map.put( key, new Integer(1) ) ;
        }
        else {
            map.put( key, new Integer(ivalue.intValue()+1) ) ;
        }
    }

    public int getCount( Object key ) {
        Integer ivalue =  (Integer) map.get( key ) ;
        if ( ivalue == null ) {
            return 0 ;
        }
        return ivalue.intValue() ;
    }

    public int remove( Object key ) {
        Integer ivalue =  (Integer) map.get( key ) ;
        if ( ivalue == null ) {
            if ( allowNegativeCounts ) {
                map.put( key, new Integer(-1) ) ;
                return -1 ;
            }
            else {
                throw new NoSuchElementException( "Cannot remove count for " + key ) ;
            }
        }
        else {
            if ( ivalue.intValue() == 1 ) {
                map.remove( key ) ;
                return 0 ;
            }
            else {
                map.put( key, new Integer(ivalue.intValue()-1) ) ;
                return ivalue.intValue() - 1 ;
            }
        }
    }

    public void clear() { map.clear(); }

    boolean allowNegativeCounts ;
    HashMap map  ;
}
