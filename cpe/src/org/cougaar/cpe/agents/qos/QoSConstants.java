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

package org.cougaar.cpe.agents.qos;

public class QoSConstants {

    public static final String TIMER_ACTION = "Timer" ;

    public static final String REPLAN_TIMER = "ReplanManueverTimer" ;

    public static final String UNIT_STATUS_UPDATE_TIMER = "UnitStatusUpdateTimer" ;

    public static final String UPDATE_STATUS_ACTION = "UpdateStatus" ;

    public static final String FORWARD_MESSAGE_ACTION = "ForwardMessage" ;

    public static final String SEND_ACTION = "SendAction" ;

    public static final String RECEIVE_ACTION = "ReceiveAction" ;

    public static final String PLAN_MANUEVER_ACTION = "PlanManueverAction" ;

    public static final String PLAN_SUSTAINMENT_ACTION = "PlanSustainmentAction" ;

    /**
     * This is a action name to merge the existing manuver plan with a new
     * manuever plan.
     */
    public static final String UPDATE_MANUEVER_PLAN_ACTION = "UpdateManueverPlan" ;
}
