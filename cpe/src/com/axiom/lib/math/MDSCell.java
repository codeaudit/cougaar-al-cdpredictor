package com.axiom.lib.math ;

/**
 *  Stores data associated with a single dissimilarity pair (i,j).
 */
public class MDSCell {
    final static int DIST = 0 ;
    final static int DHAT = 1 ;
    final static int DISSIM = 2 ;

    /**
     *  Set a field to a value.
     *  @param field.  One of DIST, DHAT, DISSIM parameters to select respective fields.
     *  @param value.  Value to assign.
     */
     public void set( int field, double value ) {
        switch ( field ) {
            case DIST :
                dist = value ;
            break ;
            case DHAT :
                dhat = value ;
            break ;
            case DISSIM :
                dissim = value ;
            break ;
            default :
                throw new RuntimeException( "Invalid field " + field + "." ) ;
        }
     }

     public int cellIndex ;

      /**
        *  Dissimilarity value of arbitrary type.  Can be "converted" into
        *  dissim value before fitting.
        */
       public Object dvalue ;

       /**
        *  Dissimilarity value.
        */
       public double dissim ;

       /**
        *  Index values associated with this cell, e.g. row i, column j.
        */
       public int i, j ;

       /**  Group index of row i associated with this cell.  This is used when
        *   there are one or more scaled subgroups associated with row i.
        */
       public int k ;

       /**
        *  Fitted dissimilarity value.
    */
    public double dhat ;

   /**
    *  Computed interpoint distances.
    */
    public double dist ;
}