package com.axiom.lib.util ;
import java.util.* ;

/** Progressable threads represent transient threads that are able provide "progress" feedback to
 *  interested observers.  Additionally, requests for halting or abortng can be forwarded
 *  to the progressabel thread. For example, downloading a file is a thread which
 *  may be halted by the user (by pressing the "cancel" button, for example), but 
 *  it should be done so in some well-defined manner (e.g. cleaning up temporary files, etc.)
 *  <code>ProgressableThread</code> provides an interface between such threads and their
 *  other threads which are observers.
 *  <p>
 *  An interactive thread may have one or more of "progress" listeners,
 *  each of which professes some interest in the current state of the
 *  thread (e.g. a progress bar on a dialog box.)
 *  <p>
 *  The particular implementation of <code>Progress</code> depends on the 
 *  needs of the application.  Some derived classes of <code>ProgressableThread</code>
 *  will be able to cleanly halt or abort, but others may not.  As a last resort,
 *  the thread may be terminated using the standard thread methods.  
 *  However, in such cases, the registered progress listeners will 
 *  not be appropriately notified.
 */

public abstract class ProgressableThread extends Thread implements Progressable {

    /** Whether this thread will attempt to abort if so requested.
     *  This could change based on the current internal state of the
     *  thread; hence, it may be advisable to <code>halt</code> the thread before
     *  querying this method to insure consistent results.
     *  Moreover, before entering an nonabortable state, the implementation
     *  of this thread should check to see whether any abort requests are pending.
     */
    public boolean isAbortable() {
       return false ;   
    }

    /** Request this thread to abort whatever it is doing and
     *  rollback to the initial state, if possible.  Naturally, if
     *  the thread is not running, it will not attempt to abort until
     *  resumed.  This method must return immediately, without blocking.  By
     *  default, all such requests are ignored.
     */
    public void requestAbort() { }

    public boolean isHaltable() { return false ; }

    /** Request that this thread halt cleanly.  The implementation must
     *  return without blocking.  By default, all such requests are ignored.
     */
    public void requestHalt() { }
    
    /** Am I halted? It is not the same as suspended!  A thread is halted if
     *  it is some consistent state _and_ suspended.  For some
     *  threads, halted may be equivalent to suspension, but not in general.
     */
    public boolean isHalted() { return false ; }

    /** Add a listener interested in ProgressEvents
     */
    public synchronized void addProgressListener( ProgressListener l ) {
        listeners.addElement( l );
    }
    
    public synchronized void removeProgressListener( ProgressListener l ) {
        listeners.removeElement( l );
    }
    
    // Protected methods used by implementations of this thread to
    // notify any listeners.
    //
    
    protected void notifyStart(int value, int lower, int upper, Object message) {
        ProgressEvent p =  new ProgressEvent( this, ProgressEvent.PROGRESS_START, value, lower, upper, message );
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressStarted(p) ;
        }
    }

    /** Send <code>PROGRESS_OCCURRED</code> message to all listeners.
     */
    protected void notifyProgress(int value, Object message) {
        ProgressEvent p = new ProgressEvent( this, value, message );
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressOccurred(p) ;
        }        
    }
        
    /** Send <code>progressAborted</code> message to all listeners.  This is called
     * by the thread itself after it aborts but before it executes stop.
     */
    protected void notifyAbort() {
        ProgressEvent p = new ProgressEvent( this, ProgressEvent.PROGRESS_ABORT ) ;
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressAborted(p) ;
        }        
    }
    
    /** Notify the observer that the thread has encountered an error condition and
        has or is about to terminate. */
    protected void notifyAbort( Object message ) {
        ProgressEvent p = new ProgressEvent( this, ProgressEvent.PROGRESS_ABORT ) ;
        p.setMessage( message );
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressAborted(p) ;
        }                
    }
    
    /** Send <code>progressHalted</code> message to all listeners.
     */
    protected void notifyHalt() {
        ProgressEvent p = new ProgressEvent( this, ProgressEvent.PROGRESS_HALT ) ;
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressHalted(p) ;
        }        
    }
    
    /** Send <code>progressResumed</code> message to all listeners.
     */
    protected void notifyResume() {
        ProgressEvent p = new ProgressEvent( this, ProgressEvent.PROGRESS_RESUME ) ;
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressResumed(p) ;
        }        
    }
    
    /** Send <code>progressCompleted</code> message to all listeners. 
     *  Notifies progress listeners that this thread is about to stop, having
     *  completed its task.
     */
    protected void notifyComplete() {
        ProgressEvent p = new ProgressEvent( this, ProgressEvent.PROGRESS_COMPLETE );
        for (Enumeration e=listeners.elements();e.hasMoreElements();) {
           (( ProgressListener ) e.nextElement() ).progressCompleted(p) ;
        }        
    }
    
    private Vector listeners = new Vector();
}
