package com.axiom.lib.awt.geom ;

/**
 *   A set of constants for specifying transforms from a matrix or
 *   array into a target coordinate space.  The target coordinate space
 *   is assumed to be a left handed system with y increasing down the page.
 *
 *   The matrix consists of ordinal pairs (a,b), which are interpreted as either
 *   (row,column) or (x,y) pairs.
 *
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
     *  where row is the first index into the grid shape and column is the second index.
     *  This style applies only to layers with grid shape.
     */
    public static final int ROW_COLUMN_TRANSFORM = 0 ;

    /**
     *  Coordinate system is given by
     *  <pre>
     *      column
     *     ------>
     *     |
     *  row|
     *     |
     *    \/
     *  </pre>
     */
    public static final int MATRIX_TRANSFORM = 1;

    /**
     *  Specifies that the appearance on screen is
     *
     *  <pre>
     *     ^
     *   y |
     *     |
     *      ---->
     *        x
     *  </pre>
     *
     *  Where x is the first coordinate and y is the second coordinate. (Right handed)
     *  Applies to layers with grid shape.
     */
    public static final int RIGHTHAND_XY_TRANSFORM = 2 ;

    /**
     *  <pre>
     *     x
     *   ------>
     *   |
     *  y|
     *   |
     *   \/
     *  </pre>
     *
     *  Applies to layers with grid shape.
     */
    public static final int LEFTHAND_XY_TRANSFORM = 3 ;

}