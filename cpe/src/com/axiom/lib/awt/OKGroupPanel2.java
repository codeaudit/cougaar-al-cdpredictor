package com.axiom.lib.awt;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;

/** Class for generic OK/Cancel/Apply/Undo panel using Swing components.
 */

public class OKGroupPanel2 extends JPanel implements OptionGroup {
  
  public OKGroupPanel2( ActionListener parent )
  {
     this( parent, OK_CANCEL_GROUP ); 
  }
  /** Constructor for panel.
   */

  public OKGroupPanel2( ActionListener parent, int groupType ) {
     setLayout( new FlowLayout() ) ;
     JButton button ;
     if ( ( groupType & OK_BUTTON ) != 0 ) {
       button = new JButton( OK ) ;
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & CANCEL_BUTTON ) != 0 ) {
       button = new JButton( CANCEL );
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & APPLY_BUTTON ) != 0 ) {
       button = new JButton( APPLY );
       button.addActionListener( parent );
       this.add( button ); 
     }
     if ( ( groupType & UNDO_BUTTON ) != 0 ) {
       button = new JButton( UNDO);
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & YES_BUTTON ) != 0 ) {
       button = new JButton( YES );
       button.addActionListener( parent );
       this.add( button );
     }
     if ( ( groupType & NO_BUTTON ) != 0 ) {
       button = new JButton( NO );
       button.addActionListener( parent );
       this.add( button );
     }
  }

}
