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
package org.cougaar.cpe.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class EntityHistory implements Serializable
{
    public static final class TrajectoryRecord {

        public TrajectoryRecord(long time, double x, double y, double dx, double dy, double strength )
        {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        long time ;
        double x, y, dx, dy ;
        double ex, ey, edx, edy ;
        boolean isSuppressed ;
        double strength ;
    }

    public ArrayList getTrajectory() {
        return trajectory ;
    }

    public void addTrajectoryRecord( long time, double x, double y, double dx, double dy, double strength ) {
        trajectory.add( new TargetContact( null, time, x, y, dx, dy, 0, 0, strength ) ) ;
    }

    ArrayList trajectory = new ArrayList() ;
}
