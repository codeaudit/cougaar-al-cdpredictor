/*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  * CHANGE RECORD
  */
package org.cougaar.tools.alf;

/**
 * A schedule with fixed size slots.  Useful for doing projections over time.
 */
public class SlotSchedule {

    public SlotSchedule( long startTime, long endTime, long slotSize ) {
        if ( endTime < startTime ) {
            throw new IllegalArgumentException( "End time is before start time." ) ;
        }
        if ( slotSize <= 0 ) {
            throw new IllegalArgumentException( "Slotsize " + slotSize + " is invalid." ) ;
        }

        this.startTime = startTime ;
        this.slotSize = slotSize ;
        numSlots = ( int ) ( ( ( double ) ( endTime - startTime ) ) / ( ( double ) slotSize ) ) ;
        slots = new Object[ numSlots ] ;
    }

    public int getNumSlots() {
        return numSlots;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Get the object at slot.
     */
    public Object getSlot( int index ) {
        return slots[index] ;
    }

    /**
     * Slot 0 starts from startTime to [startTime,slotSize - 1] inclusize, and the
     * rest follow in sequence.
     */
    public int findNearestSlotIndex( long time ) {
        if  ( time < 0 ) {
            throw new IllegalArgumentException( "Time " + time + " is less than 0." ) ;
        }
        return ( int ) Math.floor( ( ( double ) time - startTime ) / ( double ) slotSize ) ;
    }

    /**
     * Remove slots from the front of the schedule, up to and including slot index.
     * For example,
     */
    public void removeSlots( int index ) {
        if ( index > numSlots - 1 || index < 0 ) {
            throw new IllegalArgumentException( "No slot at index " + index ) ;
        }
        Object[] newSlots = new Object[ numSlots - 1 - index ] ;
        for (int i=index+1;i<numSlots;i++) {
            newSlots[i-index-1]= slots[i] ;
        }
        numSlots = numSlots - 1 - index ;
        slots = newSlots ;
    }

    /**
     * Add new slots to the tail of the schedule, up to
     * and including index.
     */
    public void addSlots( int index ) {
        if ( index > numSlots - 1 ) {
            Object[] newSlots = new Object[ index + 1 ] ;
            System.arraycopy(slots,0,newSlots,0,slots.length );
        }
        numSlots = index + 1 ;
    }

    protected long startTime = 0L ;

    protected int numSlots = 0 ;

    protected Object[] slots = null ;
    /**
     * Granularity per slot.
     */
    protected long slotSize ;
    //public void addItem( long time ) ;

    public static final void main( String[] args ) {
        long start = System.currentTimeMillis() ;
        SlotSchedule ss = new SlotSchedule( start , start + 3600000, 5000 ) ;

        System.out.println("Number of slots=" + ss.getNumSlots() );
        System.out.println("Slot index for " + start + "=" + ss.findNearestSlotIndex( start ) );
        System.out.println("Slot index for " + ( start + 5000 ) + "=" + ss.findNearestSlotIndex( start + 5000 ) );
    }
}
