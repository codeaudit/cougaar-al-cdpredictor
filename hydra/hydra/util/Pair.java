package org.hydra.util ;

public class Pair implements java.io.Serializable {
    public Pair( Object o1, Object o2 ) {
        this.o1 = o1 ; this.o2 = o2 ;
    }

    public Object first() {
        return o1 ;
    }

    public Object second() {
        return o2 ;
    }

    public String toString() { return "[" + o1.toString() + "," + o2.toString() + "]" ; }

    Object o1, o2 ;

    static final long serialVersionUID = -766477934506294402L;
}