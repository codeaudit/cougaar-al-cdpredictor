package com.axiom.lib.awt;

import java.awt.* ;
import java.awt.event.* ;

/** Simple message box for OK/Cancel, Yes/No messages.
 */

public class MessageBox extends Dialog implements ActionListener {
  public MessageBox( Frame parent, String title, String message ) {
    this( parent, title, message, OKGroupPanel.OK_BUTTON, true );
  }
  
  public MessageBox( Frame parent, String title, String message, int group ) {
    this( parent, title, message, group, true ); 
  }

  public MessageBox( Frame parent, String title, String message, int group, boolean modal ) {
    super(parent, title, modal );  // Default is modal ;

    mainPanel = new Panel( new BorderLayout() );
    this.add( mainPanel );

    mainPanel.add( okGroupPanel, BorderLayout.SOUTH );
    okGroupPanel = new OKGroupPanel( this, group );

    dialogPanel = new Panel( new BorderLayout() );
    mainPanel.add( dialogPanel, BorderLayout.CENTER );

    Label labelMsg = new Label( message ) ;
    dialogPanel.add( labelMsg, BorderLayout.CENTER ) ;

    setSize( 300, 200 );
  }
  
  /** Overrides handling of ActionEvents. The default behavior is to 
   * dismiss the dialog box.
   */

  public void actionPerformed(ActionEvent e) {
    state = e.getActionCommand();
    setVisible( false );
  }

  /** Return the state after the MessageBox has been dismissed.
   */
  public String getState() {
    return state ;
  }

  Panel okGroupPanel ;
  Panel dialogPanel ;
  Panel mainPanel ;
  String state ;
}
