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
package org.cougaar.cpe.util;

import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.blackboard.Subscription;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.tools.techspecs.qos.OMCMeasurementPoint;
import org.cougaar.tools.techspecs.qos.OMCMeasurement;
import org.cougaar.util.UnaryPredicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;


/**
 * Manage operating modes and record their values to the measurement points of interest.
 */
public class OMCMPManager {
    private IncrementalSubscription opModeSubscription;

    public OMCMPManager(BlackboardService bs) {
        this.bs = bs;
        opModeSubscription = (IncrementalSubscription) bs.subscribe( new UnaryPredicate() {
            public boolean execute(Object o) {
                return o instanceof OperatingModeCondition ;
            }
        }) ;
    }

    public void execute( long simTime ) {
        Collection c1 = opModeSubscription.getCollection() ;
        if ( !c1.isEmpty() ) {
           setOperatingModes( c1 );
        }
        update( simTime, System.currentTimeMillis() );
    }

    public void setOperatingModes(Collection opModes) {
        for (Iterator iterator = opModes.iterator(); iterator.hasNext();) {
            OperatingModeCondition condition = (OperatingModeCondition) iterator.next();
            if (nameToOMCMap.get(condition.getName()) == null) {
                nameToOMCMap.put(condition.getName(), condition);
                OMCMeasurementPoint omp ;
                nameToMeasurementPoints.put(condition.getName(), omp = new OMCMeasurementPoint(condition.getName(),
                        condition.getAllowedValues()));
                bs.publishAdd( omp );
            }
        }
    }

    public void update(long simulationTime, long realTime) {
        for (Iterator iterator = nameToOMCMap.values().iterator(); iterator.hasNext();) {
            OperatingModeCondition condition = (OperatingModeCondition) iterator.next();
            Comparable value = (Comparable) nameToOMValueMap.get(condition.getName());
            OMCMeasurementPoint omcMP = (OMCMeasurementPoint) nameToMeasurementPoints.get(condition.getName());

            // Record the current value iff it is not equal to the old condition.
            if (value == null || value.compareTo(condition.getValue()) != 0) {
                value = condition.getValue();
                nameToOMValueMap.put(condition.getName(), value);
                omcMP.addMeasurement(new OMCMeasurement(null, null, null, value, simulationTime, realTime));
            }
        }
    }

    public ArrayList getMeasurementPoints() {
        return new ArrayList(nameToMeasurementPoints.values());
    }

    BlackboardService bs;
    ArrayList operatingModeConditions = new ArrayList();
    HashMap nameToOMCMap = new HashMap();
    HashMap nameToOMValueMap = new HashMap();
    HashMap nameToMeasurementPoints = new HashMap();
}
