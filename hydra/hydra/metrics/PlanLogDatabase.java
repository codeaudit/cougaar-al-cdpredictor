package org.hydra.metrics;
import org.hydra.pdu.* ;
import java.util.* ;
import org.cougaar.core.society.* ;

public class PlanLogDatabase {
    
    public synchronized void addLog( UniqueObjectLog log ) {
        //if ( log.getUID() == null ) {
        //    throw new RuntimeException( "Log does not have UID." ) ; 
        //}
        
        logTable.put( log.getUID(), log ) ;
        if ( log instanceof TaskLog ) {
            taskTable.put( log.getUID(), log ) ;   
        }
        
        if ( log instanceof PlanElementLog ) {
            PlanElementLog pel = ( PlanElementLog ) log ;
            if ( pel.getParent() != null ) {
                peForTask.put( pel.getParent(), pel ) ;   
            }
        }        
        
        if ( log instanceof AssetLog ) {
            assetsTable.put( log.getUID(), log ) ;
        }
    }
    
    public Iterator getAssets() { return assetsTable.values().iterator() ; }
    
    public synchronized UniqueObjectLog getLog( UIDPDU uid ) {
        return ( UniqueObjectLog ) logTable.get( uid ) ;
    }
    
    public synchronized boolean removeLog( UniqueObjectLog log ) {
        return removeLog( log.getUID() ) ;   
    }
    
    public synchronized boolean removeLog( UIDPDU UID ) {
        UniqueObjectLog l = ( UniqueObjectLog ) logTable.remove( UID ) ;
        if ( l == null ) { return false ; }
        if ( l instanceof TaskLog ) {
            taskTable.remove( l ) ;
        }
        return true ;
    }
    
    public synchronized PlanElementLog getPlanElementLogForTask( UIDPDU taskUID ) {
        return ( PlanElementLog ) peForTask.get( taskUID ) ;   
    }
    
    public synchronized void clear() {
        logTable.clear() ;
        taskTable.clear() ;
        peForTask.clear() ;
        assetsTable.clear() ;
    }
    
    //public synchronized Enumeration getPlanElementForTask( String taskUID ) {       
    //}
    
    //public void registerNewType( TypePredicate e ) {
    //}
    
    public Collection getTasks() { return taskTable.values() ; }
    
    public Iterator iterator() { return logTable.values().iterator() ; }
    
    HashMap logTable = new HashMap() ;
    HashMap taskTable = new HashMap() ;
    HashMap peForTask = new HashMap() ;
    HashMap assetsTable = new HashMap() ;
//    public static final boolean GLOBAL_DEBUG = true ;
}
