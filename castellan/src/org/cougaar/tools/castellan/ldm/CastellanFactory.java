/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
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

package org.cougaar.tools.castellan.ldm;

import org.cougaar.core.domain.*;
import org.cougaar.core.service.*;
import org.cougaar.core.agent.*;
import org.cougaar.planning.ldm.*; //Himanshu
import org.cougaar.core.mts.*;

public class CastellanFactory implements Factory
{
    //protected ClusterIdentifier selfClusterId;   //Himanshu
    protected MessageAddress selfClusterId;   //Himanshu
    protected UIDServer myUIDServer;         //Himanshu

    /**
     * @see #getNumberOfEvents()
     */
    protected long nEvents;

    /**
     * Constructor for use by domain specific Factories
     * extending this class
     */
    public CastellanFactory() { }

    public CastellanFactory(LDMServesPlugin ldm) {
      //RootFactory rf = ldm.getFactory(); // Himanshu
        PlanningFactory rf = (PlanningFactory) ldm.getFactory(); // Himanshu
      //rf.addAssetFactory(
      //    new org.cougaar.tools.csmart.runtime.ldm.asset.AssetFactory());
      //rf.addPropertyGroupFactory(
      //    new org.cougaar.tools.csmart.runtime.ldm.asset.PropertyGroupFactory());

        //ClusterServesPlugin cspi = (ClusterServesPlugin)ldm;
        //selfClusterId = cspi.getClusterIdentifier();    //Himanshu
        selfClusterId = ldm.getMessageAddress();    //Himanshu
        //myUIDServer = ((ClusterContext)ldm).getUIDServer();    //Himanshu
        myUIDServer = ldm.getUIDServer();  //Himanshu
    }
}
