package com.axiom.pspace.search ;
import java.util.* ;

/**
 *  Abstract class for implementing GRAPHSEARCH.  The abstract methods can be overriden to
 *  implement storage classes, or to forward them to a <code>Database</code> object.
 *
 *  <p>  In this implementation, all successor nodes are generated at once.
 *  Variations of this algorithm could defer generating successor nodes.
 *
 *  <p>  By default, as nodes are expanded, they are checked against the existing
 *  database to see whether or not they already exist.  Optionally, the mplan may
 *  be allowed not to check agains the database, hence possibly generating
 *  duplicate nodes.  In some cases, it may be sufficiently expensive to check
 *  for duplicate such that generating duplicates would be preferred.
 *
 */
 public abstract class AbstractSearch implements GraphSearch {

     public AbstractSearch( Strategy s ) {
        strategy = s ;
     }

    /**
     *  Constructor using a strategy and initial node.
     *
     *  @param s A strategy for expanding nodes.
     *  @param initial A graph node initializing the mplan.
     */
    public AbstractSearch( Strategy s, GraphNode initial ) {
       this( s );
       init( initial ) ;
    }

    public Strategy getStrategy() { return strategy ; }

    /**
     *  Dump nodes for debugging.  Override to provide useful functionality.
     */
    public void dump() {
    }

    /**
     *  Set the maximum number of expanded nodes.  To remove
     *  any limitation, set to -1.
     */
    public void setMaxExpandedNodes( int num ) {
       maxExpandedNodes = num ;
    }

    /**
     *  Reinitialize the algorithm using a single start node.
     */
    public void init( GraphNode n ) {
       initDatabase() ; // Reinitialize the database
       addToOpen( n ) ; // Add to open list
       addNode( n ) ;   // Add to database
       strategy.initNode( n ) ;
       goal = null ;
    }

    /**
     *  Return the resultant goal node, if one has been found.  From this node,
     *  trace backwards to the start node via the parent pointers to find the
     *  solution.
     */
    public GraphNode getGoalNode() {
       return goal ;
    }

    /**
     *  Get the current state of the algorithm.
     */
    public int getState() {
        return state ;
    }

    public int getMaxDepth() { return maxDepth ; }

    public void setMaxDepth( int newMaxDepth ) { maxDepth = newMaxDepth ; }

    public void setRetainAllPredecessors( boolean value ) {
        this.retainAllPredecessors = value ;
    }

    public boolean getRetainAllPredecessors() {
        return retainAllPredecessors ;
    }

    /**
     *  Get an enumeration of all open nodes.
     */
    public abstract Enumeration getOpenNodes() ;

    /**
     *  Get closed nodes.
     */
    public abstract Enumeration getClosedNodes() ;

    /**
     * Initialize state, e.g. clear database of all elements.
     */
    public abstract void initDatabase() ;

    // Expand at most maxNodes nodes.
    // @return Current state
    // public int run( int maxNodes ) ;

    /**
     *  Implements a standard graph mplan algorithm.
     */
    public void run() {
        int count = 1 ;
        // Choose the first
        while ( true ) {
            // Dump for debugging purposes
            //if ( count % 15 == 0 ) {
            //   System.out.println( " After " + count + " iterations." ) ;
            //   dump() ;
            //   System.out.println() ;
            //}

            int numNodes = getNumOpenNodes() ;
            if ( numNodes == 0 ) {
                // No more nodes reachable
                state = GraphSearch.NO_MORE_NODES ;
                break ;
            }

            if ( maxExpandedNodes > 0 && count >= maxExpandedNodes ) {
                state = GraphSearch.MAX_NODES_EXPANDED ;
                break ;
            }

            // Get the first node on the open list and expand
            GraphNode n = getFirstOpenNode() ;
            if ( n == null ) {
                dump();
                throw new RuntimeException( "First open node is unexpectedly null!" ) ;
            }

            count++ ;

            if ( strategy.isGoalNode( n ) ) {
               goal = n ;
               state = GraphSearch.GOAL_FOUND ;
               break ;
            }

            if ( n.getDepth() >= maxDepth ) {
                break ;
            }

            //
            // Delegate node expansion to the strategy.
            // The strategy must correctly place the newly expanded successors into the
            // the node n.  REVISIT:  Is it also responsible for setting the parent of the
            //  list?
            //
            strategy.expand( n ) ;

            addToClosed( n ) ;  // Add n to closed list.  Does not have to do anything, since
                                // if a node is not on open it is by definition closed.

            // Check to see whether or not the new successor nodes are on
            // either the open or closed lists.

            int numSuccessors = n.getNumSuccessors() ;
            int newDepth = n.getDepth() + 1 ;

            for (int i=0;i<numSuccessors;i++) {
                GraphNode successor = n.getSuccessor(i) ;

                // REVISIT:  Who is responsible for setting the parent, the strategy or
                // the mplan object?
                successor.setParent( n );

                // Update the depth value of the node.
                successor.setDepth( newDepth ) ;

                // Check to see if the ith successor node already exists in the database
                GraphNode temp = findNode( successor ) ;

                // Check whether or not to redirect existing node back to n.
                // This may result in modifying the node's depth and/or
                // value
                if ( temp != null ) {
                    // Update depth. If temp's depth < successor's depth
                    // update found node's depth.
                    if ( temp.getDepth() > successor.getDepth() ) {
                        temp.setDepth(successor.getDepth());
                    }

                    if ( getRetainAllPredecessors() ) {
                        temp.addPredecessor( n );
                    }

                    if ( strategy.compare( temp, successor ) <= 0 )
                       ;  // Do nothing.  Temp is not better than the already expanded node.
                    else {
                       // Update the node, setting the parent to n
                       // Checks to see if temp is on open.  (If it isn't, it is actually on closed)
                       boolean result = removeFromOpen( temp ) ;
                       temp.setParent( n ) ;
                       strategy.updateParent( temp, successor ) ;  // Recalculate cost estimate if neccessary
                       if ( result ) {
                          addToOpen( temp ) ;  // Add back to the open list but with recalculated
                                               // heuristic value
                       }
                    }
                    // Substitute the existing node in the successors of n
                    // since it already exists
                    n.replaceSuccessor( i, temp ) ;
                    // successors[i] = temp ;
                    continue ;
                }
                else {
                    // Check to see whether the maximum depth is acheived.
                    // This should really be moved outside the loop, but trust
                    // clever optimizers to optimize this away
                    if ( newDepth > maxDepth ) {
                        continue ;
                    }

                    // Node is not in the database, add it to the open list and the general
                    // database
                    addNode( successor ) ;
                    addToOpen( successor ) ;
                }
            }
        }
    }

    /**
     *  Returns the number of nodes on the open list.  If the list reaches empty
     *  before any goal node is found, the mplan has not been able to locate any
     *  paths to the goal.
     *
     *  @return The number of nodes on the open list.
     */
    public abstract int getNumOpenNodes();

    /**
     *  Get the best node as determined by the current Strategy's comparator
     *  method and heuristics.  It is neccessary that this function be
     *  implemented correctly; otherwise, the mplan algorithm may have suboptimal
     *  results.
     */
    public abstract GraphNode getFirstOpenNode();

    /**
     *  Check to see if there are any identical nodes already in the database.
     *  Implement to avoid generation of multiple redundant nodes in the database.
     *
     *  Note: If any hashing is done, the subclass of GraphNode MUST implement
     *  an appropriate hash function.
     */
    public abstract GraphNode findNode( GraphNode n );

    /**
     *  Add a node to the database. If it already exists, false is returned.  If
     *  we don't need to check for duplicate nodes, this may be implemented as a no-op.
     *  Other implementations may choose whether or not to actually add the node based on
     *  various criteria.
     *
     *  @param n Node to be added.
     */
    public abstract boolean addNode( GraphNode n ) ;

    /**
     *  Add a newly-created (or reevaluated) node to the open list.
     *  This method must provide useful functionality.
     */
    public abstract void addToOpen( GraphNode n ) ;

    /**
     *  Remove from open list.  This is used in case a node's
     *  location in the open list is related to its heuristic value f(n).
     *  In these cases, the mplan algorithm may remove n from the open list
     *  and reintroduce it later.
     *
     *  @param If n is on the open list, return true; false otherwise.
     */
    public abstract boolean removeFromOpen( GraphNode n );

    /**
     *  Add a node to the closed list.  Must always be implemented.
     */
    public abstract void addToClosed( GraphNode n ) ;


    // Optional method, not yet implemented
    // removeFromClosed() ;

    /**
     *  Add a set of nodes to the open list.
     */
    public abstract void addToOpen( GraphNode[] n ) ;

    /** Whether predeccessors are all saved. */
    protected boolean retainAllPredecessors = false ;

    /**
     *  A strategy for expanding and evaluating nodes.
     */
    protected Strategy strategy ;

    protected GraphNode goal ;

    /** The current state of the mplan.
     */
    protected int state = START ;

    protected int maxExpandedNodes = -1 ;

    protected int maxDepth = Integer.MAX_VALUE ;
 }