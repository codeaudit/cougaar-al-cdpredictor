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
* This class provides a tool for controlling a certain amount of CPU usage for a given JVM.
* It is a singleton class that spawns a singleton high priority thread that "consumes" CPU
* time based on a control message, CPUStressorControl. The thread is set up to continuously
* cycle through a TimeON period and a TimeOFF period. During the TimeON period, the thread
* will keep control of the CPU.  During the TimeOFF period, the thread will sleep for a
* given amount of time before attempting to regain control of the CPU.  There is no certainty
* as to when the thread actually gains control of the CPU once it enters its "ready" state
* from its "sleep" state.
*/
public class CPUStressor implements Runnable{

   // CLASS MEMBER VARIABLES:
   private static Thread myThread;
   private static final CPUStressor mySingletonInstance = new CPUStressor();
   private static boolean isThreadActive = false;
   private static long myTimeON;
   private static long myTimeOFF;


   // CLASS METHODS:
   /**
   * This method provides the singleton instance of the class.
   */
   public static final CPUStressor getSingletonInstance(){
      return mySingletonInstance;
   }
   /*
   * This constructor ensures that the default constructor to this singleton
   * class cannot be called by any other classes.
   */
   protected CPUStressor(){
   }

   // IMPLEMENTATION OF METHODS IN Runnable INTERFACE:

   /**
   * This method is the run method for the thread. The thread is set up to continuously
   * cycle through a TimeON period and a TimeOFF period.  When the thread should "go away"
   * myThread will no longer point to this thread and execution will drop through the loop.
   * This is the basic CPUStressor tool.
   */
   public void run(){

      // Variables used to control TimeON
      long start, delta;
      // When this thread is to continue execution, myThread will point to this thread.
      // When this thread is to complete execution (stop), myThread will be set to null.
      // Check that this thread should still execute.
      Thread me = Thread.currentThread();
      // Only the running thread should/can set the isActive state of the CPUStressor.
      // The fact that the thread is in its run routine implies this state is active.
      isThreadActive = true;  // I am not sure if I need to ck mythread = me here also.

      while( myThread == me ){

         // Reset the varaibles used to control TimeON
         start = System.currentTimeMillis();
         delta = 0;

         // Remain in the following loop for the amount of time specified in TimeON (ms).
         while( delta < myTimeON ){
            delta = System.currentTimeMillis() - start;
         }

         // The thread should then sleep for the amount of time specified in TimeOFF (ms).
            try {
               delta = System.currentTimeMillis();
               Thread.currentThread().sleep(myTimeOFF);
            }
            catch (InterruptedException e) {
               System.out.println(e);
               isThreadActive = false;
            }
      }
      isThreadActive = false;
   }

   // OTHER MEMBER METHODS:
   /**
   * This method starts the thread.
   */
   void startTool(){
      myThread = new Thread(this);
      myThread.setPriority( Thread.NORM_PRIORITY +1 );
      myThread.start();
   }
   /**
   * This method stops the thread.
   */
   void stopTool(){
      myThread = null;
   }
   /**
   * This method updates the thread based on control parameters passed through the message.
   */
   void update( CPUStressorControl theMessage ){

      // Check if the tool should be turned off.
      if( !theMessage.isToolON() ){
         stopTool();
         return;
      }

      // Update control parameters provided by theMessage.
      myTimeON = theMessage.getTimeON();
      myTimeOFF = theMessage.getTimeOFF();

      // Check if the tool should be turned on. Once you get here you already know
      // that the tool should be turned on if it is not on already.
      // i.e. theMessage.isToolON == true.
      if( !isThreadActive ){
         // Start a new thread.
         startTool();
      }
   }
}
