/*
 * Test_GetTaskBatchingStats.java
 *
 * Created on June 1, 2002, 8:25 AM
 */

package org.cougaar.tools.castellan.tests;

import java.util.*;
import java.lang.* ;
import java.io.*;

import org.cougaar.tools.castellan.batching.* ;
import org.cougaar.tools.castellan.server.* ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.planlog.* ;
import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.alf.* ;

/**
 *
 * @author  bbowles
 */
public class Test_GetTaskBatchingStats {
   // ATTRIBUTES
   PersistentEventLog myEventLog;
   PlanLogDatabase myPlanLog;
   ArrayList myTaskLogs;
   
   /** Creates a new instance of Test_GetTaskBatchingStats */
   public Test_GetTaskBatchingStats() {
   }
   public void printHeader( String text ){
      System.out.println();
      System.out.println( "_____________________________________" );
      System.out.println( text );
      System.out.println( "_____________________________________" );
      System.out.println();
   }
   public String getKeyboardInput( String entryText ){
      BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
      System.out.println( entryText );
      String answer = null;
      try{
         answer = keyboard.readLine().trim();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
      System.out.println( " ANSWER WAS: " + answer );
      return answer;
   }
   public Collection getTaskLogs( PlanLogDatabase thePlanLog ){
      Collection taskLogs =  thePlanLog.getTasks();
      if( ( taskLogs == null ) || ( taskLogs.size() == 0 ) ){
         System.out.println( "main: No taskLogs were collected." );
         return null;
      }
      return taskLogs;
   }
   private PlanLogDatabase buildPlanLogDb( EventLog theEventLog ){
        PlanLogDatabase pld = new PlanLogDatabase() ;
        AgentWorkflowLogPlanBuilder builder = new AgentWorkflowLogPlanBuilder( pld ) ;
        SocietyDesc sd = new SocietyDesc() ;
        AgentLoadObserver observer = new AgentLoadObserver( pld, sd ) ;

        ArrayList dependencies = new ArrayList() ;
        ArrayList arPDUs = new ArrayList( theEventLog.getNumEvents() / 20 ) ;
        for (Iterator iter = theEventLog.getEvents(); iter.hasNext(); ) {
            PDU pdu = ( PDU ) iter.next() ;
            builder.processPDU( pdu );

            // Process DeclarePDUs.
            //
            if ( pdu instanceof DeclarePDU ) {
                observer.processPDU( pdu, dependencies ) ;
            }
            // Track all allocation results.
            else if ( pdu instanceof AllocationResultPDU ) {
                arPDUs.add( pdu ) ;
            }
        }

        // Second pass for allocation results.
        for (int i=0;i<arPDUs.size();i++) {
            builder.processARPDU( ( AllocationResultPDU ) arPDUs.get(i) );
        }

        System.out.println("BUILDING WORKFLOW GRAPH...");
        try {
            observer.buildGraph();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        } 
        return pld;
   }

   public void processSociety( EventLog eventLog, 
         Test_GetTaskBatchingStats test, Collection taskLogs, 
         PlanLogDatabase planLog ){
      try{
         test.printHeader(  "TESTING: ANALYZE ENTIRE SOCIETY" );
         boolean processAnother = true;
         GetTaskBatchingStats myCommand = 
             new GetTaskBatchingStats( eventLog, taskLogs, planLog );
         test.printHeader( "STATISTICS FOR SOCIETY " );
         if( myCommand.getNumStats() == 0){
            System.out.println( "NO STATS WERE COLLECTED, SOMETHING WENT AMUCK!" );
            System.out.println( "Tested on Vector.size() = 0" );
            return;
         }
         else{
            System.out.println( myCommand.toString() );
         }
         test.printHeader( "END OF TEST FOR SOCIETY " );
         return;
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
   public void processByAgent( EventLog eventLog, 
         Test_GetTaskBatchingStats test, Collection taskLogs, 
         PlanLogDatabase planLog ){
      try{
         test.printHeader(  "TESTING: ANALYZE BY AGENT" );
         boolean processAnother = true;
         while( processAnother ){
            // Get and display a list of agents to choose from.
            Collection agents = eventLog.getAgents();
            Iterator agentIter = agents.iterator();
            System.out.println("AGENTS TO CHOOSE FROM:");
            while ( agentIter.hasNext() ) {
               System.out.println( agentIter.next() );
            }
            String agentName = test.getKeyboardInput("Enter agent name:");
            if ( agentName.length() == 0 ){
                throw new IllegalArgumentException( "Agent name must be entered!" );
            }
            GetTaskBatchingStats myCommand = 
                new GetTaskBatchingStats( eventLog, taskLogs, planLog, agentName );
            test.printHeader( "STATISTICS FOR AGENT " +agentName );
            Iterator statIter = myCommand.getStats();
            AgentStats as = null;
            while( statIter.hasNext() ){
               as = (AgentStats)statIter.next();
               System.out.println( as.toString() );
            }
            test.printHeader( "END OF TEST FOR AGENT " + agentName );

            // Repeat for another Agent
            String answer = test.getKeyboardInput("Process another Agent? (y/n):");
            if( answer.equalsIgnoreCase("n") ){
               return;
            }
         }
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
   public EventLog logOnDatabase( Test_GetTaskBatchingStats test ){
         test.printHeader( "Get properties for accessing the database" );
         // Get database user properties
         Properties props = new Properties() ;
           String userName = "cougaaruser";
           System.out.println( " USING: " + userName );
           String userPassword = "cougaarpass";
           System.out.println( " USING: " + userPassword );
           String dbPath = "jdbc:mysql://localhost/";
           System.out.println( " USING: " + dbPath );
           String databaseName = "PlanLogAgent";
           System.out.println( " USING: " + databaseName );
        // Access the database
        props.put( "user", userName ) ;
        props.put( "password", userPassword ) ;
        props.put( "dbpath", dbPath ) ;

        return new PersistentEventLog( databaseName, props, true );
         
        /** 
        //String userName = test.getKeyboardInput("Enter user name (or use default):");
        // if ( userName.length() == 0 ){
           String userName = "cougaaruser";
           System.out.println( " USING: " + userName );

         //}
         //String userPassword = test.getKeyboardInput("Enter user password (or use default):");
         //if ( userPassword.length() == 0 ){
           String userPassword = "cougaarpass";
           System.out.println( " USING: " + userPassword );
         //}
         //String dbPath = test.getKeyboardInput("Enter database path (or use jdbc:mysql://localhost/)");
 //        if ( dbPath.length() == 0 ){
           String dbPath = "jdbc:mysql://localhost/PlanLogAgent";
           System.out.println( " USING: " + dbPath );
         //}
         // Get access to the database server.
      //  test.printHeader( "Accessing the database" );
        //ArrayList result = PersistentEventLog.getValidDatabases( dbPath, userName,  userPassword );

        // Get and display a list of databases to choose from.
      //  System.out.println( "DATABASES TO CHOOSE FROM:" );
      //  for (int i=0; i<result.size(); i++) {
       //     System.out.println( result.get(i) ) ;
      //  }
      //  String databaseName = test.getKeyboardInput( "Choose which database to get results from:" );
     //   if ( databaseName.length() == 0 ){
      //     throw new IllegalArgumentException( "Database name must be entered!" );
     //   }
        // Access the database
        props.put( "user", userName ) ;
        props.put( "password", userPassword ) ;
        props.put( "dbpath", dbPath ) ;

        return new PersistentEventLog( databaseName, props, true );
         */
   }
         
   /**
    * main - Test the correlation of an agents plugin execution and events generated.
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      Test_GetTaskBatchingStats test = new Test_GetTaskBatchingStats();
      EventLog eventLog;
      PlanLogDatabase planLog;
      Collection taskLogs;

      try{
         test.printHeader(  "TESTING: GetTaskBatchingStats" );
         System.out.println(" test = " + test );
         eventLog = test.logOnDatabase( test );
         // Assemble TaskLogs needed for the following processing.
         planLog = test.buildPlanLogDb( eventLog );
         taskLogs = test.getTaskLogs( planLog );
         if( taskLogs == null ){
             System.out.println( "main: No tasklogs, no analysis - shut down." );
             eventLog.close();
             return;
         }
        
         String answer = "y";
         answer = test.getKeyboardInput( 
            "Do you want to do analysis on entire society? (y/n)" );
         if( answer.equalsIgnoreCase("y") ){
            test.processSociety(  eventLog, test, taskLogs, planLog );
         }
         answer = "n";
         answer = test.getKeyboardInput( 
            "Do you want to do analysis on individual agent? (y/n)" );
         if( answer.equalsIgnoreCase("y") ){
            test.processByAgent( eventLog, test, taskLogs, planLog );
         }
         test.printHeader( "***** END OF TEST: GetPluginExecutionStatistics *****");
         eventLog.close();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }   
}
