/*
 * AppComponent.java
 *
 * Created on October 2, 2001, 11:41 AM
 */

package org.cougaar.tools.castellan.server.ui;
import org.cougaar.tools.castellan.server.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public interface AppComponent {
    
    /** Set the current application. */
    public void setApp( ServerApp app ) ;
  
    /** Refresh this component. */
    public void update() ;
}

