package org.cougaar.cpe.mplan;

//import com.axiom.lib.util.OrderedMap;
import com.axiom.pspace.search.AbstractSearch;
import com.axiom.pspace.search.Strategy;
import com.axiom.pspace.search.GraphNode;

import java.util.*;
import java.io.Writer;
import java.io.PrintWriter;

public class BoundedBranchSearch extends AbstractSearch {

    class ComparatorPred implements Comparator {
        ComparatorPred( Comparator c ) {
            comparator = c ;
        }

        public int compare( Object o1, Object o2 ) {
            return comparator.compare( o1, o2 ) ;
        }

        Comparator comparator ;
    }

    public BoundedBranchSearch(Strategy s) {
        super(s);
        // openListByDepth.clear();
        closedList = new ArrayList();
    }

    public int getMaxBranchingFactorPerPly() {
        return maxBranchingFactorPerPly;
    }

    public void setMaxBranchingFactorPerPly(int maxBranchingFactorPerPly) {
        this.maxBranchingFactorPerPly = maxBranchingFactorPerPly;
    }

    public boolean addNode(GraphNode n) {
        // map.put( n, n ) ;
        return true ;
    }

    public void addNodes(GraphNode[] n) {
        for (int i=0;i<n.length;i++) {
           addNode( n[i] ) ;
        }
    }

    public void addToClosed(GraphNode n) {
        if ( n.getDepth() > maxClosedDepth ) {
            maxClosedDepth = n.getDepth() ;
        }
        closedList.add( n ) ;
    }

    public void addToOpen(GraphNode n) {
        int depth = n.getDepth() ;

        if ( depth >= openListByDepth.size() ) {
            for(int i=openListByDepth.size();i<=depth;i++) {
                openListByDepth.add( new ArrayList() ) ;
                branchFactorAtDepth.add( new Integer(0) ) ;
                isSorted.add( Boolean.FALSE ) ;
            }
        }
        ( ( ArrayList ) openListByDepth.get(depth) ).add( n ) ;
        if ( ( ( Boolean ) isSorted.get(depth)).booleanValue() ) {
            isSorted.set( depth, Boolean.FALSE ) ;
        }
        //openList.put( n, n ) ;
    }

    public void addToOpen(GraphNode[] n) {
        for (int i=0;i<n.length;i++) {
           addToOpen( n[i] ) ;
        }
    }

    public int getNumClosedNodes() {
        return closedList.size() ;
    }

    public ArrayList getOpenListByDepth() {
        return openListByDepth;
    }

    public ArrayList getClosedList() {
        return closedList;
    }

    public void dump() {
        System.out.println( "Open Nodes" ) ;
        System.out.println( openListByDepth.toString() );
        System.out.println( "Closed List" );
//        System.out.println( closedList.toString() );
//        Set s = openList.entrySet() ;
//        while (s.iterator().hasNext()) {
//            Object o = s.iterator().next() ;
//            if ( o instanceof OrderedMap.MapEntry ) {
//                OrderedMap.MapEntry mapEntry = (OrderedMap.MapEntry) o;
//                Iterator it = mapEntry.l.iterator() ;
//                while ( it.hasNext()) {
//                    Object ol = it.next();
//                    System.out.println( ol.toString() );
//                }
//            }
//            else {
//                System.out.println( o.toString() );
//            }
//        }
    }

    public GraphNode findNode(GraphNode n) {
        return null;
    }

    public Enumeration getClosedNodes() {
        return new IteratorEnumeration( closedList.iterator() ) ;
    }

    public int getBranchesAtDepth( int depth ) {
        if ( depth > branchFactorAtDepth.size() - 1 ) {
            return 0 ;
        }
        Integer bf = (Integer) branchFactorAtDepth.get(depth) ;
        return bf.intValue() ;
    }

    /**
     * Returns the first valid node for expansion, starting from the lowest depth.
     * It also increments the branch count at each depth layer.
     * @return The first node available (e.g. the lowest depth and the lowest branch factor.)
     */
    public GraphNode getFirstOpenNode() {

        for (int i = 0; i < openListByDepth.size(); i++) {

            // Do not expand any nodes which are at the maximum depth.  This means there are no more nodes.
            if ( i >= maxDepth ) {
                return null ;
            }

            List openListAtDepth = (ArrayList)openListByDepth.get(i);
            boolean sorted = ( ( Boolean ) isSorted.get(i) ).booleanValue() ;
            if ( getBranchesAtDepth( i ) < maxBranchingFactorPerPly && openListAtDepth.size() > 0 ) {
                branchFactorAtDepth.set( i,
                        new Integer( ( ( Integer ) branchFactorAtDepth.get(i) ).intValue() + 1 ) ) ;
                if ( !sorted ) {
                    // System.out.println("\nSort at depth " + i + " and size " + openListAtDepth.size() );
                    Collections.sort( openListAtDepth, getStrategy() );
                    isSorted.set(i,Boolean.TRUE) ;
                }


                GraphNode result = (GraphNode) openListAtDepth.remove(0) ;
                //System.out.print( "firstOpen=" + result + " " );
                if ( result == null ) {
                    throw new RuntimeException( "Exception: Unexpected null result." ) ;
                }
                return result ;
            }
        }
        return null ;
    }

    /**
     * Look at the closed list and find the node with the maximum depth with the
     * best value.
     *
     * @return
     */
    public GraphNode getBestGraphNode() {
        GraphNode best = null ;
        int maxOpenDepth = openListByDepth.size() - 1 ;
        int maxDepth = Math.max( maxOpenDepth, maxClosedDepth ) ;

        // Scan the closed list and the open list at maximum depth.
        for (int i = 0; i < closedList.size(); i++) {
            GraphNode graphNode = (GraphNode)closedList.get(i);
            if ( graphNode.getDepth() == maxDepth ) {
                if ( best == null ) {
                    best = graphNode ;
                }
                else if ( strategy.compare( graphNode, best ) < 0 ) {
                    best = graphNode ;
                }
            }
        }

        // Scan the open list at the maximum depth.
        if ( maxDepth == openListByDepth.size() - 1 ) {
            ArrayList list = (ArrayList) openListByDepth.get( openListByDepth.size() - 1 ) ;
            for (int i = 0; i < list.size(); i++) {
               GraphNode graphNode = (GraphNode)list.get(i);
                if ( graphNode.getDepth() == maxDepth ) {
                    if ( best == null ) {
                        best = graphNode ;
                    }
                    else if ( strategy.compare( graphNode, best ) < 0 ) {
                        best = graphNode ;
                    }
                }
            }
        }

        return best ;
    }

    public int getNumExpandedOpenNodes() {
        int result = 0 ;
        for (int i = 0; i < openListByDepth.size(); i++) {
          ArrayList orderedMap = (ArrayList)openListByDepth.get(i);
          result += orderedMap.size() ;
        }
        return result ;
    }

    /**
     * This returns the number of open nodes that can be expanded. (This may
     * be zero if there are no more nodes which can be expanded.)
     * @return
     */
    public int getNumOpenNodes() {
        int result = 0 ;
        for (int i = 0; i < openListByDepth.size(); i++) {
            int branchesAtDepth = getBranchesAtDepth( i ) ;
            if ( i >= maxDepth ) {
                break ;
            }

            if ( branchesAtDepth  < maxBranchingFactorPerPly ) {
                ArrayList orderedMap = (ArrayList) openListByDepth.get(i);
                result += Math.min( orderedMap.size(), maxBranchingFactorPerPly - branchesAtDepth ) ;
            }
        }
        return result ;
    }

    public static class IteratorEnumeration implements Enumeration {
        public IteratorEnumeration(Iterator iter) {
            this.iter = iter;
        }

        public boolean hasMoreElements() {
            return iter.hasNext();
        }

        public Object nextElement() {
            return iter.next() ;
        }

        Iterator iter ;
    }

    public Enumeration getOpenNodes() {
        return null ;
        // return new IteratorEnumeration( openList.values().iterator() ) ;
    }

    public void initDatabase() {
        openListByDepth.clear();
        closedList.clear();
    }

    public boolean removeFromOpen(GraphNode n) {
        return false;
    }

    /**
     * Release the resources used by search by moving the WorldStateNodes back to
     * the pool.
     */
    public void release() {
    }

    protected HashMap map = new HashMap() ;

    protected int maxClosedDepth = 0 ;
    protected ArrayList closedList ;

    protected int maxBranchingFactorPerPly = 500 ;

    /**
     * For each depth, maintain an open list.
     */
    protected ArrayList openListByDepth = new ArrayList();

    private ArrayList isSorted = new ArrayList();

    /**
     * Number of branches at each depth (starting from zero)
     */
    protected ArrayList branchFactorAtDepth = new ArrayList() ;

    /**
     * A preallocated set of wsn and WorldStateModel instances.
     */
    protected ArrayList nodeAndWorldStatePool = new ArrayList( 100000 ) ;

}
