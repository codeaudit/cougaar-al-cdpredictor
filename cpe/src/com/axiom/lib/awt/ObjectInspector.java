package com.axiom.lib.awt ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;

class TestObject {

    public String getMoose() {
       return moose ;
    }

    String moose = "Moose" ;
}

public class ObjectInspector extends JPanel implements Inspector {

    public ObjectInspector( InspectorFactory factory, Object o ) {
        this.factory = factory ;
        setTarget( o ) ;
    }

    public void setTarget( Object o ) {
        target = o ;
        setLayout( new BorderLayout() ) ;

        if ( target != null ) {

            if ( target.getClass().isArray() ) {
                ArrayInspector ai = new ArrayInspector( factory, o ) ;
                add( ai, BorderLayout.CENTER ) ;
            }
            else if ( target instanceof java.util.Vector ) {
                VectorInspector vi = new VectorInspector( factory, o ) ;
                add( vi, BorderLayout.CENTER ) ;
            }
            else {
                PropertyInspector pi = new PropertyInspector( factory ) ;
                pi.setObject( target ) ;
                add( new JScrollPane(pi), BorderLayout.CENTER ) ;
            }
        }
    }

    public Object getTarget() {
        return target ;
    }

    public void notifyWindowClosing() {
        if ( factory != null ) {
            factory.remove( target ) ;
        }
    }

    public static void main( String[] args ) {

        JFrame frame = new JFrame( "Test" ) ;
        frame.setSize( 640, 480 ) ;
        frame.setVisible( true ) ;

        InspectorFactory df = new DefaultInspectorFactory( frame ) ;

        df.add( new TestObject() ) ;
    }

    Frame frame ;
    InspectorFactory factory ;
    Object target ;
}
