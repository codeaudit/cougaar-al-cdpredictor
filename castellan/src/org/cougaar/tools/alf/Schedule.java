package org.cougaar.tools.alf;

import org.cougaar.tools.castellan.util.MultiTreeSet;

import java.util.*;

/**
 * Generic schedule of time events based on long.
 */
public class Schedule {

    public Schedule() {
    }

    public Schedule( boolean allowOverlaps ) {
        this.allowOverlaps = allowOverlaps ;
    }

    public int getSize() {
        return startMap.size() ;
    }

    /**
     * @return Does this schedule allow overlapping items?
     */
    public boolean isAllowOverlaps() {
        return allowOverlaps;
    }

    /**
     * A list of scheduled items which have any part between start and end.
     */
    public Iterator getScheduledItemsBetween( long start, long end ) {

        return null ;
    }

    public Iterator getScheduledItemsIntersecting( long start, long end ) {
        Iterator sub1 = startMap.subMap( new Long( start ), new Long( end ) ) ;
        Iterator sub2 = endMap.subMap( new Long( start ), new Long( end ) ) ;
        return null ;
    }

    /**
     * Return a set of boundaries starting with start and ending with end.
     * All overlapping schedule elements will be broken up into segments here.
     * @return An array of at least two elements s.t. the first element is start
     * and the last element is end.
     */
    public long[] findSegments( long start, long end ) {
        ArrayList list = new ArrayList() ;
        HashMap times = new HashMap() ;
        Long ss = new Long( start ) ;
        times.put( ss, ss ) ;
        Long st = new Long( end ) ;
        times.put( st, st ) ;
        Iterator iter = startMap.subMap( new Long( start ), new Long( end ) ) ;
        while ( iter.hasNext() ) {
            Schedulable s = ( Schedulable ) iter.next() ;
            Long l = new Long( s.getStartTime() ) ;
            times.put( l, l ) ;
        }
        iter = endMap.subMap( new Long( start ), new Long( end ) ) ;
        while ( iter.hasNext() ) {
            Schedulable s = ( Schedulable ) iter.next() ;
            Long l = new Long( s.getStartTime() ) ;
            times.put( l, l ) ;
        }

        for ( Iterator i2 = times.values().iterator() ; i2.hasNext();) {
            list.add( i2.next() ) ;
        }

        Collections.sort( list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Long l1 = ( Long ) o1, l2 = ( Long ) o2 ;
                return l1.compareTo( l2 ) ;
            }
        });

        long[] results = new long[ list.size() ] ;
        for (int i=0;i<list.size();i++) {
            results[i] = ( ( Long ) list.get(i) ).longValue() ;
        }
        return results ;
    }

    public long getFirstTime() {
        if ( startMap.size() == 0 ) {
            throw new RuntimeException( "Schedule is empty." ) ;
        }
        return ( ( Long ) startMap.firstKey() ).longValue() ;
    }

    public long getLastTime() {
        if ( endMap.size() == 0 ) {
            throw new RuntimeException( "Schedule is empty." ) ;
        }
        return ( ( Long ) endMap.lastKey() ).longValue() ;
    }

    /**
     * Are there any overlaps inclusize of start and end times.
     */
    public boolean hasOverlap( long start, long end ) {
        if ( end < start && start > 0 ) {
            throw new IllegalArgumentException( "End( " + end + ") is before than start(" + start + ")" ) ;
        }
        if ( start > end && start < 0 ) {
            throw new IllegalArgumentException( "End( " + end + ") is before than start(" + start + ")" ) ;
        }
        if ( getSize() == 0 ) {
            return false ;
        }

        Iterator iter = startMap.subMap( new Long( start ) , new Long( end ) ) ;
        if ( iter.hasNext() ) {
            return true ;
        }
        iter = endMap.subMap( new Long( start ), new Long( end ) ) ;
        if ( iter.hasNext() ) {
            return true ;
        }

        // Third case contains  |  | | | type overlaps
        iter = startMap.subMap( new Long( Long.MIN_VALUE ), new Long( start ) ) ;
        while ( iter.hasNext() ) {
            Schedulable s = ( Schedulable ) iter.next() ;
            if ( s.getEndTime() >= start ) {
                return true ;
            }
        }
        return false ;
    }

    public boolean addScheduledItem( Schedulable s ) {

        // Reject any schedulable items overlapping with existing scheduled items.
        if ( !isAllowOverlaps() ) {
            if ( hasOverlap( s.getStartTime(), s.getEndTime() ) ) {
                return false ;
            }
        }

        startMap.put( new Long( s.getStartTime() ), s ) ;
        endMap.put( new Long( s.getEndTime() ), s ) ;
        return true ;
    }

    public static void main( String[] args ) {

        Schedule s = new Schedule() ;

        class Item implements Schedulable {
            public Item(long startTime, long endTime) {
                this.startTime = startTime;
                this.endTime = endTime;
            }

            public boolean isInstantaneous() {
                return false;
            }

            public long getStartTime() {
                return startTime;
            }

            public long getEndTime() {
                return endTime;
            }

            long startTime, endTime ;
        }

        s.addScheduledItem( new Item( 40, 100 ) );
        s.addScheduledItem( new Item( 50, 200 ) );
        s.addScheduledItem( new Item( 60, 80 ) );
        s.addScheduledItem( new Item( 70, 160 ) ) ;
        s.addScheduledItem( new Item( 220, 300 ) ) ;

        long[] segs = s.findSegments( s.getFirstTime(), 160 ) ;
        System.out.print("[");
        for (int i=0;i<segs.length;i++) {
            System.out.print( segs[i] );
            if ( i < segs.length - 1 ) {
                System.out.print(",");
            }
        }
        System.out.print("]");

        System.out.println("Has overlap (201,219)" + s.hasOverlap(201,219));
        System.out.println("Has overlap (40, 50)" + s.hasOverlap(40,50));
        System.out.println("Has overlap (20, 400)" + s.hasOverlap(20,400));
    }


    protected boolean allowOverlaps = true ;
    protected MultiTreeSet startMap = new MultiTreeSet(), endMap = new MultiTreeSet() ;
}
