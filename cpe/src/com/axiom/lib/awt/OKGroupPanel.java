package com.axiom.lib.awt;

import java.awt.* ;
import java.awt.event.* ;

/** Class for generic OK/Cancel/Apply/Undo panel.
 */

public class OKGroupPanel extends Panel implements OptionGroup {
    
  public OKGroupPanel( ActionListener parent )
  {
     this( parent, OK_CANCEL_GROUP ); 
  }
  /** Constructor for panel.
   */

  public OKGroupPanel( ActionListener parent, int groupType ) {
     setLayout( new FlowLayout() ) ;
     Button button ;
     if ( ( groupType & OK_BUTTON ) != 0 ) {
       button = new Button( OK ) ;
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & CANCEL_BUTTON ) != 0 ) {
       button = new Button( CANCEL );
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & APPLY_BUTTON ) != 0 ) {
       button = new Button( APPLY );
       button.addActionListener( parent );
       this.add( button ); 
     }
     if ( ( groupType & UNDO_BUTTON ) != 0 ) {
       button = new Button( UNDO);
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & YES_BUTTON ) != 0 ) {
       button = new Button( YES );
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & NO_BUTTON ) != 0 ) {
       button = new Button( NO );
       button.addActionListener( parent );
       this.add( button );
     }
  }

}
