/*
 * <copyright>
 *  Copyright 2003-2004 Intelligent Automation, Inc.
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.tools.cpustressor;

import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.cpe.util.CPUConsumer;
import org.cougaar.util.UnaryPredicate;

import java.util.Collection;
import java.util.Iterator;

public class CPUStressor2Plugin extends ComponentPlugin {
    public static final String CPU_LOAD_FACTOR = "CpuLoadFactor" ;
    private OperatingModeCondition cpuLoadFactor;
    private IncrementalSubscription opModeSubscription ;
    private ConsumerThread consumerThread;

    protected void execute() {
        Collection c = opModeSubscription.getChangedCollection() ;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            OperatingModeCondition condition = (OperatingModeCondition) iterator.next();
            if ( condition == cpuLoadFactor ) {
                Comparable value = condition.getValue() ;
                if ( value instanceof Number ) {
                    Number n = (Number) value ;
                    if ( n.longValue() > 0 ) {
                        double on = n.longValue() ;
                        double off = 100 - n.longValue() ;
//                        double off=( 100.0 - n.longValue() ) / n.longValue() ;
//                        if ( Math.abs( on / off - on / Math.round( off ) ) > 0.05 ) {
//                            on *= 2 ;
//                            off *= 2 ;
//                        }
                        onMillis = (long) on ;
                        offMillis = (long) Math.round( off ) ;
                        System.out.println("On=" + onMillis + ",off=" + offMillis );
                    }
                    else {
                        onMillis = 0 ;
                        offMillis = 0 ;
                    }
                    consumerThread.interrupt();
                }
            }
        }
    }

    protected void setupSubscriptions() {
        opModeSubscription = (IncrementalSubscription) getBlackboardService().subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof OperatingModeCondition ;
            }
        }) ;

        cpuLoadFactor = new OperatingModeCondition( CPU_LOAD_FACTOR, new OMCRangeList( 0, 100 ) ) ;
        getBlackboardService().publishAdd( cpuLoadFactor ) ;

        consumerThread = new ConsumerThread() ;
        consumerThread.start();
    }

    class ConsumerThread extends Thread {
        public void run() {
            int i = 0 ;

            while ( true ) {
                if ( onMillis > 0 ) {
//                    System.out.println("Starting CPU cycle for " + onMillis + ", off=" + offMillis );
                    long startTime = System.currentTimeMillis() ;

                    // Consume CPU until the current time exceedis the start time
                    while ( System.currentTimeMillis() < startTime + onMillis ) {
                        for (int j=0;j<100;j++) {
                            i += j ;
                        }
                        //CPUConsumer.consumeCPU( 1 );
                    }

                    try {
                        sleep( offMillis ) ;
                    } catch (InterruptedException e) {
                    }
                }
                else {
                    try {
                        sleep( 1000 ) ;
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    protected long baseMillis = 1 ;
    protected long onMillis ;
    protected long offMillis ;
}
