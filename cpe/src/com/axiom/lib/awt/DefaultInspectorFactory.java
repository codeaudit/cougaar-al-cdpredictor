package com.axiom.lib.awt ;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import java.util.* ;

/**
 *  The default inspector factory generates JDialogs for each component
 *  parented to a frame.  For different behavior, implement your own InspectorFactory
 *  classes.
 */
public class DefaultInspectorFactory implements InspectorFactory {
    static class ObjectRef {
        public ObjectRef( Object object ) {
            this.object = object ;
        }

        public boolean equals( Object o ) {
            if ( o instanceof ObjectRef ) {
                return (( ObjectRef ) o ).object == object ;
            }
            return false ;
        }

        public int hashCode() {
            return object.hashCode() ;
        }

        int mycount ;
        Object object ;
    }

    static class ObjectDialog extends JDialog {
        ObjectDialog( Frame f, String name, InspectorFactory factory, Inspector i ) {
            super( f, name, false ) ;
            this.object = object ;
            this.factory = factory ;
            this.inspector = i ;
            addWindowListener( new WindowAdapter() {
                    public void windowClosing( WindowEvent e ) {
                        doWindowClosing( e ) ;
                    }
                } ) ;

            if ( inspector instanceof Component ) {
                Component c = ( Component ) inspector ;
                getContentPane().setLayout( new BorderLayout() ) ;
                getContentPane().add( c, BorderLayout.CENTER ) ;
            }
        }

        public void doWindowClosing( WindowEvent e ) {
            factory.remove( inspector.getTarget() ) ;
        }

        InspectorFactory factory ;
        Inspector inspector ;
        Object object ;
        int mycount ;
    }

    /**
     *  @param frame Frame to which all inspector dialogs will be parented
     */
    public DefaultInspectorFactory( Frame frame ) {
        this.frame = frame ;
    }

    /**
     *  @return Frame from which all inspector dialogs will be parented
     */
    public Frame getFrame() {
        return frame ;
    }

    public Inspector add( Object o ) {
        ObjectRef ref = new ObjectRef( o ) ;

        Inspector tmp = ( Inspector ) inspectorTable.get( ref ) ;

        if ( tmp != null && tmp.getTarget() == o ) {
            if ( tmp instanceof Component ) {
                Component c = ( Component ) tmp ;
                while ( c != null && !( c instanceof Frame ) && !( c instanceof Dialog ) ) {
                    c = c.getParent() ;
                }

                // Bring the window to the front
                if ( c != null ) {
                    ( ( Window ) c ).toFront() ;
                    ( ( Window ) c ).requestFocus() ;
                }
            }
            return tmp ;
        }

        ObjectInspector oi = new ObjectInspector( this, o ) ;

        ObjectDialog dialog = new ObjectDialog( frame, "Inspector ", this, oi ) ;
        dialog.mycount = count++ ;
        dialog.setSize( 340, 400 ) ;
        dialog.setVisible( true ) ;

        inspectorTable.put( ref, oi ) ;
        return oi ;
    }

    /**
     *  Register an inspector to handle objects of particular classes.
     */
    public void registerInspector( Class c, Class t ) {

    }

    public void deregisterInspector( Class c, Class t ) {

    }

    public Inspector get( Object o ) {
        ObjectRef ref = new ObjectRef( o ) ;

        Inspector tmp = ( Inspector )inspectorTable.get( ref ) ;

        if ( tmp instanceof Component ) {
            Component c = ( Component ) tmp ;
            while ( c != null && !( c instanceof Frame ) && !( c instanceof Dialog ) ) {
                c = c.getParent() ;
            }

            // Bring the window to the front
            if ( c != null ) {
                ( ( Window ) c ).toFront() ;
                ( ( Window ) c ).requestFocus() ;
            }
        }

        return tmp ;
    }

    public void remove( Object o ) {
        ObjectRef ref = new ObjectRef( o ) ;

        Inspector tmp = ( Inspector ) inspectorTable.remove( ref ) ;

        if ( tmp instanceof Component && tmp.getTarget() == o ) {
            Component c = ( Component ) tmp ;
            while ( c != null && !( c instanceof Frame ) && !( c instanceof Dialog ) ) {
                c = c.getParent() ;
            }

            if ( c != null ) {
                ( ( Window ) c ).setVisible( false ) ;
            }
        }
    }

    protected Hashtable inspectorTable = new Hashtable() ;

    protected Frame frame ;

    static int count = 0 ;
}