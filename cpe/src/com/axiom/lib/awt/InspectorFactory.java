package com.axiom.lib.awt ;

/**
 *  ObjectInspectorFactory creates and maintains a list of objects and their associated
 *  inspector windows.  It insures that there is only one instance of the inspector windows
 *  for each object.  Additionally, it controls visual placement of new object inspectors.
 */
public interface InspectorFactory {

    /** Make an inspector and return it if it does not already exist.  Otherwise, just
     *  return the exisitng inspector.
     */
    Inspector add( Object o ) ;

    /**
     *  Get the inspect associated with this class.
     */
    Inspector get( Object o ) ;

    void remove( Object o ) ;

    /**
     *  Add a inspector class to handle class c.
     */
    void registerInspector( Class c, Class i ) ;

    /**
     *  Remove a inspector class to handle class c.
     */
    void deregisterInspector( Class c, Class i ) ;
}