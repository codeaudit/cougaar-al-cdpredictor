/*
 * Test_GetPluginExecutionStatistics.java
 *
 * Created on June 1, 2002, 8:25 AM
 */

package org.cougaar.tools.castellan.tests;

import java.util.*;
import java.lang.* ;
import java.io.*;

import org.cougaar.tools.castellan.statistics.* ;
import org.cougaar.tools.castellan.server.* ;
import org.cougaar.tools.castellan.pdu.* ;
import org.cougaar.tools.castellan.planlog.* ;

/**
 *
 * @author  bbowles
 */
public class Test_GetPluginExecutionStatistics {

   /** Creates a new instance of Test_GetPluginExecutionStatistics */
   public Test_GetPluginExecutionStatistics() {
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
      return answer;
   }
   public void processSociety( PersistentEventLog log, Test_GetPluginExecutionStatistics test ){
      try{
         test.printHeader(  "TESTING: ANALYZE ENTIRE SOCIETY" );
         boolean processAnother = true;
         GetPluginExecutionStatistics myCommand = 
             new GetPluginExecutionStatistics( log );
         myCommand.collectStats();
         test.printHeader( "STATISTICS FOR SOCIETY " );
         if( myCommand.getNumStats() == 0){
            System.out.println( "NO STATS WERE COLLECTED, SOMETHING WENT AMUCK!" );
            System.out.println( "Tested on Vector.size() = 0" );
            return;
         }
         Iterator statIter = myCommand.getStats();
         if( statIter != null ){
            AgentStats as = null;
            Object obj = null;
            int agentstatcount = 0;
            while( statIter.hasNext() ){
               obj = statIter.next();
               agentstatcount++;
               if( obj != null ){
                  as = (AgentStats)obj;
                  System.out.println( as.toString() );
               }
            }
            test.printHeader( "END OF TEST FOR SOCIETY " );
         }
         else{
            System.out.println( "NO STATS WERE COLLECTED, SOMETHING WENT AMUCK!" );
            System.out.println( "Tested on Vector == null." );
            return;
         }
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
   public void processByAgent( PersistentEventLog log, Test_GetPluginExecutionStatistics test ){
      try{
         test.printHeader(  "TESTING: ANALYZE BY AGENT" );
         boolean processAnother = true;
         while( processAnother ){
            // Get and display a list of agents to choose from.
            Collection agents = log.getAgents();
            Iterator agentIter = agents.iterator();
            System.out.println("AGENTS TO CHOOSE FROM:");
            while ( agentIter.hasNext() ) {
               System.out.println( agentIter.next() );
            }
            String agentName = test.getKeyboardInput("Enter agent name:");
            if ( agentName.length() == 0 ){
                throw new IllegalArgumentException( "Agent name must be entered!" );
            }
            GetPluginExecutionStatistics myCommand = 
                new GetPluginExecutionStatistics( log );
            myCommand.collectStats( agentName );

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
   public PersistentEventLog logOnDatabase( Test_GetPluginExecutionStatistics test ){
         test.printHeader( "Get properties for accessing the database" );
         // Get database user properties
         Properties props = new Properties() ;
         String userName = test.getKeyboardInput("Enter user name (or use default):");
         if ( userName.length() == 0 ){
           userName = "cougaaruser";
         }
         String userPassword = test.getKeyboardInput("Enter user password (or use default):");
         if ( userPassword.length() == 0 ){
           userPassword = "cougaarpass";
         }
         String dbPath = test.getKeyboardInput("Enter database path (or use jdbc:mysql://localhost/)");
         if ( dbPath.length() == 0 ){
            dbPath = "jdbc:mysql://localhost/";
         }
         // Get access to the database server.
        test.printHeader( "Accessing the database" );
        ArrayList result = PersistentEventLog.getValidDatabases( dbPath, userName,  userPassword );

        // Get and display a list of databases to choose from.
        System.out.println( "DATABASES TO CHOOSE FROM:" );
        for (int i=0; i<result.size(); i++) {
            System.out.println( result.get(i) ) ;
        }
        String databaseName = test.getKeyboardInput( "Choose which database to get results from:" );
        if ( databaseName.length() == 0 ){
           throw new IllegalArgumentException( "Database name must be entered!" );
        }
        // Access the database
        props.put( "user", userName ) ;
        props.put( "password", userPassword ) ;
        props.put( "dbpath", dbPath ) ;

        return new PersistentEventLog( databaseName, props, true );
   }
      
      
      
   
   /**
    * main - Test the correlation of an agents plugin execution and events generated.
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      Test_GetPluginExecutionStatistics test = new Test_GetPluginExecutionStatistics();
      try{
         test.printHeader(  "TESTING: GetPluginExecutionStatistics" );
         System.out.println(" test = " + test );
         PersistentEventLog log = test.logOnDatabase( test );
         String answer = "y";
         answer = test.getKeyboardInput( 
            "Do you want to do analysis on entire society? (y/n)" );
         if( answer.equalsIgnoreCase("y") ){
            test.processSociety( log, test );
         }
         answer = "n";
         answer = test.getKeyboardInput( 
            "Do you want to do analysis on individual agent? (y/n)" );
         if( answer.equalsIgnoreCase("y") ){
            test.processByAgent( log, test );
         }
         test.printHeader( "***** END OF TEST: GetPluginExecutionStatistics *****");
         log.close();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }   
}
