package com.axiom.lib.math ;

public class OrderedCell extends MDSCell {
    /** Reference to cell which constitutes next and previous cell in this cell's
        ordered group (which is indexed by k) */
    public OrderedCell next, prev ;

    /** Change in dhat. */
    public double ddhat ;

    /** Used for debugging purposes. */
    public double olddhat ;

    public boolean epsilon = false ;

    public OrderedCell maxCell ;
}