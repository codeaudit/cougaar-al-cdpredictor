package com.axiom.lib.awt ;
import java.io.* ;
import com.axiom.lib.util.*;

class LoaderThread extends Thread {
    public LoaderThread( ObjectInputStream ois ) {

        this.ois = ois ;
    }

    public Object getResult() { return object ; }

    public Exception getError() { return e ; }

    public void run() {

        try {
            object = ois.readObject();
        }
        catch ( ClassNotFoundException e ) {
            this.e  = e ;
        }
        catch ( Exception e ) {
            this.e = e ;
        }

    }

    Exception e ;
    ObjectInputStream ois ;
    Object object ;
}

public class ObjectLoaderThread extends ProgressableThread {
   public static final int MODE_AUTO = 0 ;
   public static final int MODE_MONITOR = 1 ;

   public ObjectLoaderThread( InputStream fis ) {
      this.fis = fis ;
      try {
        this.ois = new ObjectInputStream( fis ) ;
        total = fis.available() ;
      }
      catch ( IOException e ) {
      }

      if ( total == 0 ) {
         mode = MODE_AUTO ;
      }
      else
         mode = MODE_MONITOR ;
   }

   public void run() {
      if ( mode == MODE_MONITOR ) {
        notifyStart( 0, 0, total, "Loading..." ) ;
      }
      
      thread = new LoaderThread( ois ) ;
      thread.setPriority( Thread.NORM_PRIORITY - 1 );
      thread.start();

      if ( mode == MODE_MONITOR ) {
        while ( true ) {
           if ( thread.isAlive() ) {
              try {
              int available = fis.available() ;
              notifyProgress( total - available, "Loading..." ) ;
              sleep( updateRate ) ;
              }
              catch ( Exception e ) {}
           }
           else {
              break ;
           }
        }
      }
      else {
         try {
         thread.wait();
         }
         catch ( Exception e ) {}
      }

      result = thread.getResult() ;
      notifyComplete();
   }

   public Exception getError() {
        if (thread == null ) return null ;
        return thread.getError() ;
   }

   public Object getResult() { return result ; }

   public void requestAbort() {
      //abort = true ;
      // stop();
   }

   public boolean isAbortable() {
      return false ;
   }

   LoaderThread thread ;
   ObjectInputStream ois ;
   InputStream fis ;
   int total = 0 ;
   private Object result = null ;
   int mode = -1 ;
   private int updateRate = 250 ;  // Update every 250 ms
}
