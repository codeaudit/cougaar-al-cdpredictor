package org.cougaar.tools.castellan.analysis;

import org.cougaar.tools.castellan.pdu.* ;

/**
 * Log for expansions.
 */

public class ExpansionLog extends PlanElementLog {

  public ExpansionLog( UIDPDU parent, UIDPDU[] children, UIDPDU uid, String cluster,
                       long createdTime, long createdExecutionTime )
  {
      super( uid, parent, cluster, createdTime, createdExecutionTime ) ;
      this.children = children ;
      if ( children == null ) {
        for (int i=0;i<children.length;i++) {
          children[i] = children[i] ;  // Don't bother internalizing the strings anyway  
        }
      }
  }

  public void outputParamString(StringBuffer buf) {
      super.outputParamString( buf ) ;
      buf.append( ",children=" ) ;
      if ( children == null ) {
         buf.append( "null" ) ;
      }
      else {
         buf.append( "#=" ).append( children.length ).append(",") ;
         for (int i=0;i<children.length;i++) {
            buf.append( children[i] );
            if ( i < children.length - 1 ) {
                buf.append(",") ;
            }
         }
      }
  }

  public UIDPDU[] getChildren() { return children ; }

  public void setChildren( UIDPDU[] children ) { this.children = children ; }

  UIDPDU[] children ;
}
