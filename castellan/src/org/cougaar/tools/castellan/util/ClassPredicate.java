/*
 * ClassPredicate.java
 *
 * Created on May 14, 2001, 2:16 PM
 */

package org.cougaar.tools.castellan.util;

/**
 * A class is a set of objects which satisfies some logical predicat.e
 * A class is consistent but its membership possibly varies over time.  For example,
 * the class of unallocated tasks changes as tasks arise and are allocate.
 * @author  wpeng
 * @version 
 */
public abstract class ClassPredicate implements java.io.Serializable {

    /** Returns a unique string for each type of class. */
    public abstract String getClassName() ;

    /** Tests to see whether a particular object (PlanElement,etc) is
     *  an instance of this class.
     */
    public abstract boolean isInstanceOf( Object o ) ;
    
    /** Make an object whose hashCode can be used to index to this
     *  Class.
     */
    public abstract Object makeHashableKey( Object o ) ;
}
