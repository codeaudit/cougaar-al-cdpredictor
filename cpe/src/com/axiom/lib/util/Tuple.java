package com.axiom.lib.util ;

public class Tuple {
    public Tuple( int size ) {
        objects = new Object[size] ;
    }

    public Tuple( Object o1 ) {
        this( 1 ) ;
        objects[0] = o1 ;
    }

    public Tuple( Object o1, Object o2 ) {
        this( 2 ) ;
        objects[0] = o1 ;
        objects[1] = o2 ;
    }

    public Tuple( Object o1, Object o2, Object o3 ) {
        this( 3 ) ;
        objects[0] = o1 ; objects[1] = o2; objects[2] = o3 ;
    }

    public Tuple( Object o1, Object o2, Object o3, Object o4 ) {
        this( 3 ) ;
        objects[0] = o1 ; objects[1] = o2; objects[2] = o3 ;
        objects[3] = o4 ;
    }

    public Tuple( Object[] objects ) {
        this.objects = ( Object[] ) objects.clone() ;
    }

    public Object first() { return objects[0] ; }

    public Object second() { return objects[1] ; }

    public Object third() { return objects[2] ; }

    public Object fourth() { return objects[3] ; }

    public int getSize() { return objects.length ; }

    private Object[] objects ;
}