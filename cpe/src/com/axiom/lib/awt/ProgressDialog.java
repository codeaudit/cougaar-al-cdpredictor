package com.axiom.lib.awt;
import java.awt.* ;
import java.awt.event.*;
import com.axiom.lib.util.*;
import javax.swing.*;
import java.lang.reflect.* ;

/** A dialog that displays progress meter and supervises a <code>ProgressableThread</code>
 *  which it represents.
 *
 *  <p> The motivation for the ProgressListener/ProgressableThread model is that modal
 *  dialogs block the thread which makes the dialog visible.  A seperate thread
 *  is needed to perform the task and make callbacks to the dialog to display and update
 *  results.
 *
 *  <p> <code>ProgressDialog</code> relies on the Progressable object to notify it of
 *  any changed in state, e.g. when the client thread starts, halts, or aborts.
 *
 *  @author Wilbur Peng
 */

public class ProgressDialog extends JDialog implements ActionListener, ProgressListener {
    
    /** Progress has been completed.
     */
    public static final int PROGRESS_COMPLETED = 0;
    
    /** Thread has aborted due to internal error condition.
     */
    public static final int PROGRESS_ABORTED = 1;
    
    /** Thread is in initial state and has not yet been run.
     */
    public static final int PROGRESS_INIT = 2;
    
    /** The progressable thread is running.
     */
    public static final int PROGRESS_RUNNING = 3;
    
    /** The thread has been cancelled externally.
     */
    public static final int PROGRESS_CANCELLED = 4;
    
    /** Progress has been paused externally.
     */
    public static final int PROGRESS_HALTED = 5;
    
    /** Default constructor for progress dialog with no buttons.
     */
    public ProgressDialog( Frame parent, String title, boolean modal ) {
        super( parent, title, modal ) ;
        
        addWindowListener( new WindowAdapter() {
           public void windowClosing( ActionEvent e ) {
              System.exit(0) ;
           } } );
           
        createPanels() ;
    }
    
    protected void createPanels() {
       
       JPanel progressPanel = new JPanel() {
            public Insets getInsets() {
                return new Insets(40,30,20,30);       
            }
       };
       
       progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
 	   Dimension d = new Dimension(400, 20);

       getContentPane().setLayout( new BorderLayout() ); 
	   getContentPane().add(progressPanel, BorderLayout.CENTER );

       // Add the progress label 
       progressPanel.add(Box.createGlue());

       progressLabel = new JLabel("                  ");
       progressLabel.setAlignmentX(CENTER_ALIGNMENT) ;
       progressLabel.setMaximumSize( d );
       progressLabel.setPreferredSize( d );
	   progressPanel.add(progressLabel);
	   progressPanel.add(Box.createRigidArea(new Dimension(1,20)));
 
       // Add the progress bar
	   progressBar = new JProgressBar();
	   progressBar.setAlignmentX( CENTER_ALIGNMENT ) ;
	   progressLabel.setLabelFor(progressBar);
        
	   progressPanel.add(progressBar);
	   progressPanel.add(Box.createGlue());
	   progressPanel.add( new OKGroupPanel( this, OKGroupPanel.CANCEL_GROUP ) );
	   progressPanel.add(Box.createGlue());	   
	   setSize( 460, 275 );
       pack() ;
    }
    
    /** Start the associated interactive thread and set the dialog to become visible.
     *  If it is modal, inputs to other windows will be blocked.
     */
    
    public void start() {
       if ( thread != null ) {
         thread.start();
         setVisible( true );
       }
       else
         System.out.println("Could not start thread.");
    }
    
    public void progressStarted( ProgressEvent e ) {
       state = PROGRESS_RUNNING ;
       progressBar.setMinimum( e.getMinimum() );
       progressBar.setMaximum( e.getMaximum() );
       progressLabel.setText( ( String ) e.getMessage() );
       progressLabel.setSize( progressLabel.getPreferredSize() );       
    }

    public void progressHalted( ProgressEvent e ) {
       state = PROGRESS_HALTED ;
    }

       class ProgressOccurredRunnable implements Runnable {
            ProgressOccurredRunnable( ProgressEvent e ) {
                this.e = e ;
            }
            public void run() {
                progressBar.setValue( e.getValue() ) ;
                progressLabel.setText( ( String ) e.getMessage() );
                progressLabel.setSize( progressLabel.getPreferredSize() );
                validate() ;
            }

            ProgressEvent e ;
       }
    
    
    /** Notification whenever the Progressable object indicates that progress has
     * ocurred.
     */
    public void progressOccurred( ProgressEvent e ) {
       //progressBar.setMinimum( e.getMinimum() );
       //progressBar.setMaximum( e.getMaximum() );

       try {
       SwingUtilities.invokeAndWait( new ProgressOccurredRunnable( e ) ) ;
        }
        catch ( Throwable t ) {
        }
    }

    /** Implements progress listener. Dismisses this dialog if progress has been aborted.
     */
    public void progressAborted( ProgressEvent e ) {
       state = PROGRESS_ABORTED ;
       message = e.getMessage();
       // setVisible( false );

       try {
       SwingUtilities.invokeAndWait(
         new Runnable() {
            public void run() {
                try {
                    Thread.sleep(150);
                }
                catch ( InterruptedException except ) {
                }
                setVisible( false ) ;
                dispose() ;
            }
         } ) ;
       }
       catch ( InvocationTargetException exception ) {
            System.out.println( exception ) ;
            exception.printStackTrace() ;
       }
       catch ( InterruptedException exception ) {
       }

       // thread = null ;  // Approved manner of terminating thread (rather than calling thread.stop
    }
    
    public void progressCompleted( ProgressEvent e ) {
        
       try {
       SwingUtilities.invokeAndWait( 
         new Runnable() {
            public void run() {
                setState( ProgressDialog.PROGRESS_COMPLETED ) ;
                progressBar.setValue( progressBar.getMaximum() ) ;
                repaint();
                try {
                    Thread.sleep(200);
                }
                catch ( InterruptedException except ) {
                }
                setVisible( false );
                dispose() ;
            }
         } ) ;
       }
       catch ( InvocationTargetException exception ) {
            System.out.println( exception ) ;
            exception.printStackTrace() ;
       }
       catch ( InterruptedException exception ) {
        
       }
    }
    
    public void progressResumed( ProgressEvent e ) {
        state = PROGRESS_RUNNING ;
    }
    
    public void actionPerformed(ActionEvent e) {
        if ( e.getActionCommand() == OKGroupPanel.CANCEL ) 
        {
            thread.requestAbort();  // Request the thread to abort, but it's up to the thread to do anything
                                    // about it!
        }
    }
        
    public void setText( String text ) {
        progressLabel.setText( text ) ;
    }
    
    public void setMinimum( int lower ) {
        progressBar.setMinimum( lower );
    }
    
    public void setMaximum( int upper ) {
        progressBar.setMaximum( upper );
    }
    
    public int getValue() {
        return progressBar.getValue() ;   
    }
    
    public void setValue( int value ) {
        progressBar.setValue( value );
    }
    
    public void setThread( ProgressableThread thread ) {
        this.thread = thread ;
        this.thread.addProgressListener( this );
    }
    
    public ProgressableThread getThread() {
        return thread ;
    }
    
    public JLabel getLabel() {
        return progressLabel;
    }
    
    public static void main( String[] argv ) {
       
        Frame parent = new JFrame("Test frame" );
        parent.setSize( 640, 480 );
        parent.addWindowListener( new WindowAdapter() {
            public void windowClosing( ActionEvent e ) {
                System.exit(0) ;
            } } );
        parent.setVisible( true );
  
        ProgressDialog progressDialog = new ProgressDialog( parent, "Timer", true ) ;
        progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        TimerThread thread = new TimerThread() ;
        progressDialog.setThread( thread );
        progressDialog.start();
       
    }
    
    /**
     *  Get the last message sent to the dialog by the ProgressableThread. 
     */
    public Object getMessage() { return message ; }
    
    public int getState() { return state ; }
    
    public void setState( int state ) { this.state = state ; }
        
    private ProgressableThread thread ;
    private JProgressBar progressBar ;
    private JLabel progressLabel ;
    
    private Object message = null ;
    
    int state = PROGRESS_INIT;
}

/** Test class to demonstrate usage of progress dialog.
 */

class TimerThread extends ProgressableThread {
   TimerThread() {
   }
   
   public void run() {
      int count = 0;
      notifyStart(0, 0, delayUnits, "Starting count" );
      
      for (int i=0;i<delayUnits;i++) {
         if ( abort == true )
            break ;
         try {
           sleep(250);
           notifyProgress( count, "Count: " + count );
           System.out.println( "" + count++ );
         }
         catch ( InterruptedException e ) {
            
         }
      }
      if ( !abort ) {
          notifyComplete();
      }
      else {
          notifyAbort();  // Notify any listeners that we are aborting sucessfully    
      }
      // stop();
   }
      
   public void requestAbort() {
      abort = true ;
      // stop();
   }
   
   public boolean isAbortable() {
      return true ;
   }

   boolean abort = false ;
   int delayUnits = 30 ;
}