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
  *  6/24/02 Initial version  by IAI
  */

package org.cougaar.tools.cpustressor;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.util.UnaryPredicate;

import java.util.Enumeration;


/**
 * This UnaryPredicate matches all CPUStressorControlImpl objects
 */
class myPredicate implements UnaryPredicate{
   public boolean execute(Object o) {
      return (o instanceof CPUStressorControlImpl);
   }
}

/**
 * This COUGAAR Plugin subscribes to CPUStressorControlImpl objects and sends
 * them to the CPUStressor tool. It holds a singleton instance of the
 * CPUStressor tool.  The idea is that only one of these tools should be
 * available per JVM.
 **/
public class CPUStressorPlugin extends ComponentPlugin {

   // MEMBER VARIABLES:
   long myTimeON, myTimeOFF;
   boolean isActive = false;

   // Holds the Singleton CPUStressor which the plugin supplies control messages to.
   final static private CPUStressor myTool = CPUStressor.getSingletonInstance();

   // Holds my subscription for ControlMsg objects (matching myPredicate class)
   private IncrementalSubscription messages;

   // IMPLEMENTATION OF COMPONENT PLUGIN ABSTRACT METHODS:
   /**
   * Called when the Plugin is loaded.  Establish the subscription for
   * CPUStressorControlImpl objects.
   */
   protected void setupSubscriptions() {
      messages = (IncrementalSubscription)getBlackboardService().subscribe(new myPredicate());
   }
   /**
   * Called when there is a change on my subscription(s).  Sends new message(s) to the
   * CPUStressor tool.
   */
   protected void execute () {
      Enumeration new_messages = messages.getAddedList();
      while (new_messages.hasMoreElements()) {

         CPUStressorControl aMsg = (CPUStressorControl)new_messages.nextElement();
         myTool.update( aMsg );
      }
   }
}