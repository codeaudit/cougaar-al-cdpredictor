package com.axiom.lib.util;

public interface ProgressListener extends java.util.EventListener {
    public void progressStarted( ProgressEvent e ) ;
 
    public void progressOccurred( ProgressEvent e ) ;
    
    public void progressAborted( ProgressEvent e ) ;
    
    public void progressHalted( ProgressEvent e ) ;
    
    public void progressCompleted( ProgressEvent e );
    
    public void progressResumed( ProgressEvent e ) ;
}