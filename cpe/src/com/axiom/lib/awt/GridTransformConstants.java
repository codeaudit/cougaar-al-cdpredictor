package com.axiom.lib.awt ;

/**
 *  A set of constants which enumerate different possible transformations
 *  from a pair of ordinal values (a,b) to a (visual) coordinate system.
 */

public interface GridTransformConstants {

    /**
     *  Specifies that appearance on screen is
     *  <pre>
     *       ^
     *   row |
     *       ---->
     *         column
     *  </pre>
     *  where row is the first index and column is the second index.
     *  Useful for displaying images.
     */
    public static final int ROW_COLUMN_IMAGE_TRANSFORM = 0 ;

    /**
     *  The appearance on screen is given by
     *  <pre>
     *      column
     *     ------>
     *     |
     *  row|
     *     |
     *    \/
     *  </pre>
     *  This is useful for representing standard matrices.
     */
    public static final int ROW_COLUMN_TRANSFORM = 1;
    
    /**
     *  Specifies that the appearance on screen is
     *  <pre>
     *     /\
     *   y |
     *     |
     *      ---->
     *        x
     *  </pre>
     *
     *  Where x is the first coordinate and y is the second coordinate.
     */
    public static final int RIGHTHAND_XY_TRANSFORM = 2 ;
    
    /**
     *  Specifies that the appearance on screen is
     *  <pre>
     *     x
     *   ------>
     *   |
     *  y|
     *   |
     *   \/
     *  </pre>
     */
    public static final int LEFTHAND_XY_TRANSFORM = 3 ;

}
