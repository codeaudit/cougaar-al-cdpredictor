package org.cougaar.tools.techspecs.qos;

import org.cougaar.core.mts.MessageAddress;

import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

import com.axiom.lib.util.ArrayMath;

/**
 * User: wpeng
 * Date: May 30, 2003
 * Time: 10:59:27 AM
 */
public class HorizonMeasurementPoint extends MeasurementPoint {

    public HorizonMeasurementPoint(String name) {
        super(name);
    }

    public boolean isValidMeasurement(Measurement m) {
        return m instanceof DelayMeasurement ;
    }

    /**
     * Here, a delay measurement shows the absolute horizon (local time value) at the event time (timestamp value)
     * @param m
     */
    public void addMeasurement(Measurement m) {
        if ( !isValidMeasurement(m) ) {
            throw new IllegalArgumentException( "Measurement " + m + "is not valid." ) ;
        }

        DelayMeasurement dm = (DelayMeasurement) m ;
        DelayMeasurement last = (DelayMeasurement) getLastMeasurement() ;
        if ( last != null && dm.getLocalTime() < last.getLocalTime() ) {
            throw new IllegalArgumentException( "Measurement " + m +
                    " cannot have local timestamp <=" + last ) ;
        }
        super.addMeasurement( m );
    }

    public long getFirstLocalTime() {
        return ( ( DelayMeasurement ) getFirstMeasurement()).getLocalTime() ;
    }

    public long getLastLocalTime() {
        return ( (DelayMeasurement) getLastMeasurement() ).getLocalTime() ;
    }

    public long[][] getInterpolatedAndPreciseHorizon( long startTime, long endTime, long sampleRate ) {
        DelayMeasurement first = (DelayMeasurement) getFirstMeasurement() ;
        if ( endTime <= startTime ) {
            throw new IllegalArgumentException( "End time " + endTime + " precedes start time " + startTime ) ;
        }

        if ( endTime < first.getLocalTime() ) {
            throw new IllegalArgumentException( " End Time " + endTime + " is less than first local time " + first.getLocalTime() ) ;
        }

        long[][] result = new long[2][ (int) Math.ceil( ( endTime - startTime ) / sampleRate ) + 1 + getHistorySize() ] ;

        ArrayList measurements = new ArrayList( history ) ;
        long time = startTime ;
        int index = 0, tindex = 0 ;
        while ( time <= endTime && index < measurements.size() ) {
            DelayMeasurement curr = null, next = null ;

            if ( index < measurements.size() ) {
                curr = (DelayMeasurement) measurements.get(index ) ;
            }
            if ( index + 1 < measurements.size() ) {
                next = (DelayMeasurement) measurements.get(index+1) ;
            }

            if ( curr != null && next != null ) {
                // Increment to the next delay.
                if ( time >= next.getLocalTime() ) {
                    index++ ;
                }
                else {
                    result[0][tindex] = time ;
                    result[1][tindex] = time - curr.getTimestamp() ;
                    tindex ++ ;
                    if ( time + sampleRate > next.getLocalTime() ) {
                        time = next.getLocalTime() ;
                    }
                    else if ( ( time - startTime ) % sampleRate != 0 ) {
                        time += ( sampleRate - ( time - startTime ) % sampleRate ) ;
                    }
                    else
                    {
                        time += sampleRate ;
                    }
                }
            }
            else if ( next == null ) {
                result[0][tindex] = time ;
                result[1][tindex] = time - curr.getTimestamp() ;
                tindex ++ ;
                time += sampleRate ;
            }

        }

        while ( time <= endTime ) {
            DelayMeasurement last = (DelayMeasurement) getLastMeasurement() ;
            result[0][tindex] = time ;
            result[1][tindex] = last.getLocalTime() - time ;
            tindex ++ ;
            time += sampleRate ;
        }

        long[][] temp = new long[2][tindex] ;
        System.arraycopy( result[0], 0, temp[0], 0, tindex );
        System.arraycopy( result[1], 0, temp[1], 0, tindex );

        return temp ;
    }

    /**
     * Return a time series of delays from startTime to endTime.
     * @param startTime
     * @param endTime
     * @return
     */
    public long[] getInterpolatedDelays( long startTime, long endTime, long sampleRate ) {

        DelayMeasurement first = (DelayMeasurement) getFirstMeasurement() ;
        if ( endTime <= startTime ) {
            throw new IllegalArgumentException( "End time " + endTime + " precedes start time " + startTime ) ;
        }

        if ( endTime < first.getLocalTime() ) {
            throw new IllegalArgumentException( " End Time " + endTime + " is less than first local time " + first.getLocalTime() ) ;
        }

        long[] result = new long[ (int) Math.ceil( ( endTime - startTime ) / sampleRate ) + 1] ;

        ArrayList measurements = new ArrayList( history ) ;
        long time = startTime ;
        int index = 0, tindex = 0 ;
        while ( time <= endTime && index < measurements.size() ) {
            DelayMeasurement curr = null, next = null ;

            if ( index < measurements.size() ) {
                curr = (DelayMeasurement) measurements.get(index ) ;
            }
            if ( index + 1 < measurements.size() ) {
                next = (DelayMeasurement) measurements.get(index+1) ;
            }

            if ( curr != null && next != null ) {
                // Increment to the next delay.
                if ( time >= next.getLocalTime() ) {
                    index++ ;
                }
                else {
                    result[tindex] = time - curr.getTimestamp() ;
                    tindex ++ ;
                    time += sampleRate ;
                }
            }
            else if ( next == null ) {
                result[tindex] = time - curr.getTimestamp() ;
                tindex ++ ;
                time += sampleRate ;
            }

        }

        while ( time <= endTime ) {
            DelayMeasurement last = (DelayMeasurement) getLastMeasurement() ;
            result[tindex] = time - last.getTimestamp() ;
            tindex ++ ;
            time += sampleRate ;
        }


        return result ;
    }

    /**
     * Get the minimum delay associated with a specific time t.
     * For example, given a series of MeasurementChains, the
     * delay at time t can be calculated from the local time, the timestamp
     * and the currentTime.
     *
     * @param currentTime The delay at the "current" time. (The current time may be
     * in the past so long as it is within the history of the DelayMeasurementPoint.)
     * @return
     */
    public long getDelay( long currentTime ) {
        Iterator iter = getMeasurements() ;
        DelayMeasurement previousMeasurement = null ;

        if ( getHistorySize() == 0 ) {
            throw new IllegalArgumentException( "Delay cannot be determined from zero element history." ) ;
        }
        DelayMeasurement first = (DelayMeasurement) getFirstMeasurement() ;
        if ( first.getTimestamp() >= currentTime ) {
            throw new IllegalArgumentException( "Delay cannot be determined for current time <" + first ) ;
        }

        while (iter.hasNext()) {
            DelayMeasurement delayMeasurement = (DelayMeasurement) iter.next();
            if ( previousMeasurement != null ) {
                if ( previousMeasurement.getLocalTime() <= currentTime && delayMeasurement.getLocalTime() > currentTime ) {
                    return currentTime - previousMeasurement.getTimestamp() ;
                }
            }
            previousMeasurement = delayMeasurement ;
        }

        DelayMeasurement last = (DelayMeasurement) getLastMeasurement() ;
        if ( currentTime >= last.getLocalTime() ) {
            return currentTime - last.getLocalTime() ;
        }

        throw new RuntimeException( "Unexpected condition reached.  No valid delay found." ) ;
    }


    public static final void main( String[] args ) {
        HorizonMeasurementPoint mp = new HorizonMeasurementPoint( "Moose" ) ;
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 1000, 2000 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 2000, 3000 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 2200, 3200 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 3000, 5000 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 4000, 5300 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 5500, 5500 ) );
        mp.addMeasurement( new DelayMeasurement( "A", "B", MessageAddress.getMessageAddress("Moose"), 10000, 13000 ) );

        long[] delays = mp.getInterpolatedDelays( mp.getFirstLocalTime(), 15000, 500 ) ;
        System.out.println("Interpolated Delays=" );
        System.out.println( ArrayMath.toString( delays ) ) ;

        long[][] results = mp.getInterpolatedAndPreciseHorizon( mp.getFirstLocalTime(), 15000, 500 ) ;
        System.out.print("Precise & Interpolated Delays=[");
        for (int i = 0; i < results[0].length; i++) {
            long time = results[0][i];
            long value = results[1][i] ;
            System.out.print( "(" + time + "," + value + ')' ) ;
            if ( i < results.length - 1 ) {
                System.out.print(",");
            }
        }
        System.out.println("]");
    }
}
