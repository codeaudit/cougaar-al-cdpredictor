package org.cougaar.tools.techspecs.qos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A MeasurementPoint is a persistent, named object associated with a particular
 * type of measurement.  It maintains a history of measurements.
 */
public abstract class MeasurementPoint implements java.io.Serializable
{
    public MeasurementPoint(String name) {
        this.name = name;
    }

    public String getName() {
        return name ;
    }

    public void clearMeasurements() {
        history.clear();
    }

    public Iterator getMeasurements() {
        return history.iterator() ;
    }
    
    public Iterator getMeasurements(int lastN)
    {
		//LinkedList recentHistory = new LinkedList();
		LinkedList recentHistory=(LinkedList) history.clone();
		if ( recentHistory.size() > lastN ) {
					while ( recentHistory.size() > lastN ) {
						recentHistory.removeFirst() ;
					}
				}
		return recentHistory.iterator();	
    }

    public synchronized void addMeasurement( Record m ) {
        history.add( m ) ;
        if ( history.size() > maxSize ) {
            history.removeFirst() ;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[") ;
        toString( buf );
        buf.append( "]") ;
        return buf.toString() ;
    }

    public synchronized void toString( StringBuffer buf ) {
        String className = getClass().getName() ;
        buf.append( className.substring( className.lastIndexOf('.')+1)) ;
        buf.append( " name=\"").append( name ).append( "\"") ;
        buf.append( ",size=" ).append( getHistorySize() ) ;
        buf.append( ",maxSize=").append( getMaximumHistorySize() ) ;
        buf.append( ",last=").append( getLastMeasurement() ) ;
    }

    public boolean isValidMeasurement( Record m ) {
        return true ;
    }

    public int getHistorySize() {
        return history.size() ;
    }

    public int getMaximumHistorySize() {
        return maxSize ;
    }

    public void setMaximumHistorySize(int maxSize) {
        this.maxSize = maxSize;
        if ( history.size() > maxSize ) {
            while ( history.size() > maxSize ) {
                history.removeFirst() ;
            }
        }
    }

    public Record getFirstMeasurement() {
        if ( history.size() == 0 ) {
            return null ;
        }
        return (Record) history.getFirst() ;
    }

    public Record getLastMeasurement() {
        if ( history.size() == 0 ) {
            return null ;
        }
        return (Record) history.getLast() ;
    }
    private int maxSize = 3000 ;
    protected LinkedList history = new LinkedList() ;
    private String name;

}
