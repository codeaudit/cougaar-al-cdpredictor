/*
 * <copyright>
 *  Copyright 2004 Intelligent Automation, Inc.
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

package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.UnitEntity;
import org.cougaar.cpe.model.Plan;
import org.cougaar.tools.techspecs.events.MessageEvent;

/**
 * User: wpeng
 * Date: Apr 14, 2003
 * Time: 12:40:37 PM
 */
public class ManueverPlanMessage extends MessageEvent {

    public ManueverPlanMessage( String entityName, org.cougaar.cpe.model.Plan plan ) {
        setValue( plan );
        this.entityName = entityName ;
    }

    public org.cougaar.cpe.model.Plan getPlan() {
        return (org.cougaar.cpe.model.Plan) getValue() ;
    }

    public String getEntityName() {
        return entityName;
    }

    protected String entityName ;
}
