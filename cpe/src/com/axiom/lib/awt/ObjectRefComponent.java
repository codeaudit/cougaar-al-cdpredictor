package com.axiom.lib.awt;
import java.awt.* ;
import javax.swing.* ;
import javax.swing.border.* ;

public class ObjectRefComponent extends JPanel {

    public ObjectRefComponent( Object object ) {
        setLayout( new BorderLayout() ) ;

        add( label = new JLabel(), BorderLayout.CENTER ) ;
        setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
        setObject( object );
    }

    public void setObject( Object object ) {
        this.object = object ;
        if ( object != null )
            label.setText( object.toString() ) ;
        else
            label.setText( "null" ) ;
    }

    public Object getObject() {
        return object ;
    }

    JLabel label ;
    Object object ;
}
