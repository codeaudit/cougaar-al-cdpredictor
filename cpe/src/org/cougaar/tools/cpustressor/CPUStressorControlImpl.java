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

/**
* This class provides the implementation of the control message to the
* CPUConsumer tool. It controls:
*  - activating/deactivatiing the tool
*  - setting parameters for the tool to use
* The tool is set up to cycle through a TimeON period and a TimeOFF period.
*/
public class CPUStressorControlImpl implements CPUStressorControl, java.io.Serializable, Cloneable{

   // MEMBER VARIABLES:
   private long myTimeON, myTimeOFF;
   private boolean isActive = false;

   // IMPLEMENTATION OF METHODS IN CPUStressorControl INTERFACE

   /**
   * This is a getter method which identifies (in milliseconds) the
   * time the CPUStressor tool will keep control of the CPU in a cycle.
   */
   public long getTimeON(){
      return myTimeON;
   }
   /**
   * This is a setter method which specifies (in milliseconds) the
   * time the CPUStressor tool will keep control of the CPU in a cycle.
   */
   public void setTimeON( long theTimeON ){
      myTimeON = theTimeON;
   }
   /**
   * This is a getter method which identifies (in milliseconds) the
   * time the CPUStressor tool will sleep in its cycle before attempting
   * to regain control of the CPU.
   */
   public long getTimeOFF(){
      return myTimeOFF;
   }
   /**
   * This is a setter method which specifies (in milliseconds) the
   * time the CPUStressor tool will sleep in its cycle before attempting
   * to regain control of the CPU.
   */
   public void setTimeOFF( long theTimeOFF ){
      myTimeOFF = theTimeOFF;
   }
   /**
   * This is a method which specifies that the CPUStressor tool should be
   * activated.
   */
   public void turnToolON(){
      isActive = true;
   }
   /**
   * This is a method which specifies that the CPUStressor tool should be
   * deactivated.
   */
   public void turnToolOFF(){
      isActive = false;
   }
   /**
   * This is a method which identifies if the CPUStressor tool is ON or OFF
   */
   public boolean isToolON(){
      return isActive;
   }

   // OVERIDES DEFAULT Object PROTECTED CLONE() METHOD. FROM CLONEABLE INTERFACE.
   /**
   * This method overides protected clone() method.
   */
   public Object clone() throws CloneNotSupportedException{
      return super.clone();
   }

   // OTHER MEMBER METHODS:

   public CPUStressorControlImpl(boolean theIsActive, long theTimeON, long theTimeOFF){

      //Initialize parameters
      myTimeON = theTimeON;
      myTimeOFF = theTimeOFF;
      isActive = theIsActive;
   }
   public CPUStressorControlImpl(){
   }

   public String toString() {
       String myState = super.toString() + "\n" +
                       "isActive: " + isActive + "\n" + "\n" +
                       "myTimeOFF: " + myTimeOFF + "\n" +
                       "myTimeON: " + myTimeON + "\n";
       return myState;
     }
}

