package org.hydra.metrics;
import org.cougaar.util.* ;
import java.util.* ;

/**  LogDatabase.
 */
public abstract class LogDatabase {

   /** A ClassGenerator defines an equivalence class between plan elements.
    *  Each leaf node is assumed to define either a node or an arc.  A class
    *  here is defined as a set of logically/semantically equivalent elements" .
    *
    *  <p>The class generator defines a predicate tree is used for sequential or
    *  hierarchical filtering operations.
    *  Each vertex in the tree has one or more children which represent either leaves
    *  or subtrees.  Each vertex may be marked as disjoint, indicating that the
    *  classes defined by its children are disjoint and hence the test only has to be
    *  performed until a true is returned.
    */
   static abstract class ClassGeneratorNode {

       /** Generate vertices.
        */
       public static final int TYPE_VERTEX = 0 ;
       public static final int TYPE_DIRECTED_EDGE = 1 ;
       public static final int TYPE_EDGE = 2 ;

       ClassGeneratorNode( int classType ) {
       }

       /** Generate a node from this class.
        */
       public boolean isClassGenerator() { return false ; }

       /** This node has disjoint children.
        */
       public boolean isDisjoint() { return true ; }

       public Iterator getChildren() { return null ; }

       public void addChild( UnaryPredicate p ) {
       }

       public UnaryPredicate getChild(int index) { return null ; }
   }

   //public void processElement( Log element ) {
   //}

   /** Defines a ``root" of a single graph of interest.
    */
   static class Graph {
   }

   //public void registerClassGenerator( ClassGenerator p, Vector children ) ;

   // Timebase protocol. REQ/ACK delay time average will be used.  Also, causality
   // will be enforced.
}