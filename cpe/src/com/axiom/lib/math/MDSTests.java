package com.axiom.lib.math ;
import com.axiom.lib.util.* ;
import java.util.* ;
import java.util.ArrayList ;
import java.util.Collections ;

public abstract class MDSTests {

    /**
     * Use X vectors and generate all cells.
     */
    public static MDSCell[] generateScaledData( double[][] X, ArrayList[] list ) {
        ArrayList tmp = new ArrayList() ;
        for (int i=0;i<X.length;i++) {
            for (int j=0;j<X.length;j++) {
                if ( i != j ) {
                    OrderedCell c = new OrderedCell() ;
                    c.i = i ; c.j = j ;
                    tmp.add( c ) ;
                }
            }
        }

        OrderedCell[] cells = new OrderedCell[ tmp.size() ];
        for (int i=0;i<tmp.size();i++) {
            cells[i] = ( OrderedCell ) tmp.get(i) ;
        }
        
        // Update dissim field with L2 norm distance
        MDS.updateValue( cells, X, 2, MDSCell.DISSIM ) ;

        // Create a bunch of lists, one for each row.
        for (int i=0;i<list.length;i++) {
            list[i] = new ArrayList() ;
        }

        // Generate non-unformly scaled rows indexing each cell by its row value i
        for (int i=0;i<cells.length;i++) {
            list[cells[i].i].add( cells[i] ) ;
        }

        for (int i=0;i<list.length;i++) {
            Collections.sort( list[i],
                                new java.util.Comparator() {
                                    public int compare( Object o1, Object o2 ) {
                                        OrderedCell c1 = ( OrderedCell ) o1 ;
                                        OrderedCell c2 = ( OrderedCell ) o2 ;
                                        double value = c1.dissim - c2.dissim ;
                                        if ( value < 0 ) return -1 ;
                                        else if ( value > 0 ) return 1 ;
                                        else return 0 ;
                                    }

                                    public boolean equals( Object o) {
                                        return false ;
                                    }
                                }
                            ) ;
        }

        // Generate pseudo scaled data in dissim
        for (int i=0;i<list.length;i++) {
            ArrayList l = list[i];
            double val = 0.02 ;
            double delta = 0.05 ;
            double dd = 0.02 ;
            for (int j=0;j<list[i].size();j++) {
                OrderedCell c = ( OrderedCell ) l.get(j) ;
                if ( j > 0 )
                    c.prev = ( OrderedCell ) l.get(j-1);
                if ( j < l.size() - 1 )
                    c.next = ( OrderedCell ) l.get(j+1) ;
                c.dhat = val ;
                val += delta ;
                delta += Math.random() * dd ;
                if ( delta < 0 ) delta = -delta ;
            }
        }

        MDSCell[] result = new MDSCell[ cells.length ];
        System.arraycopy( cells, 0, result, 0, cells.length ) ;
        return result ;
    }

    /**
     *  Update dhat by scaling each row of X with a random scale factor.  This is
     *  used to generate test data.
     *
     *  @param scale  An array of randomly generated scale parameters indexed by row.
     *                Only a single scale is generated and applied to all elements of the row.
     */
    public static void updateDissimRandomScale( double mins, double maxs,
                                       MDSCell[] cells, double[][] X, double[][] scale )
    {
        for ( int i=0;i<scale.length;i++) {
            double[] ts = { ( double ) ( Math.random() * ( maxs - mins ) + mins )} ;
            scale[i] = ts ;
        }

        for (int i=0;i<cells.length;i++) {
            MDSCell cell = cells[i] ;
            double[] xi = X[ cell.i] ;
            double[] xj = X[ cell.j] ;
            cell.k = 0 ; // Always in the zeroth group
            double dvalue = ArrayMath.dist( xi, xj ) ;
            cell.dvalue = new Double( dvalue );
            cell.dissim = dvalue * scale[cell.i][0];
        }
    }

    /**
     *  We compute the dhat values from the dissimilarity values by scaling each row
     *  by one or more scale factors, e.g dhat[i][j] = scale[i] * dissim[i][j].  This
     *  is justified by the observation that often the relative ordering(s) w.r.t. a
     *  particular object i is known, but not the absolute scalings minimizing stress.
     */
    public static void updateDhatFromScaledDissim( MDSCell[] cells, double[][] scale ) {
        for (int i=0;i<cells.length;i++) {
            MDSCell cell = cells[i] ;
            double sc = scale[ cell.i ][ cell.k ];
            cell.dhat = cell.dissim * sc ;
        }
    }

    /**
     *  Assuming a linear scaling function f, e.g. dhat = f(dissim,alpha) = alpha * dissim
     *  for each row, compute the gradient d Stress / d alpha.  Note that this can be
     *  generalized to a non-linear scale, e.g dhat = f( dissim ), where f is some
     *  non-linear function.
     */
    public static void computeScaleGradient( MDS.Stress stress,
                                             MDSCell[] cells, double[][] scale,
                                             double[][] grad )
    {
        for (int i=0;i<grad.length;i++) {
            ArrayMath.set( grad[i], 0 ) ;
        }

        double S = stress.S, T = stress.T ;

        for (int i=0;i<cells.length;i++) {
            MDSCell cell = cells[i] ;
            double tmp = (1 / ( S * T ) ) *
                        ( - cell.dist * cell.dissim +
                          cell.dissim * cell.dhat ) ;
            grad[cell.i][cell.k] += tmp ;
        }
    }

    /**
     *  Compute the gradient dS/ d dissim for each cell individually.
     */
    public static void computeCellDissimGradient( MDS.Stress stress,
                                                  MDSCell[] cells, double[] grad )
    {
        ArrayMath.set( grad, 0 ) ;
        double divisor = 1 / ( stress.S * stress.T  ) ;

        for (int i=0;i<cells.length;i++) {
            grad[i] = divisor * ( - cells[i].dist + cells[i].dhat  );
        }
    }

    /**
     *  Update the cells, preserving their relative ordering.
     */
    public static void updateOrderPreserving( MDSCell[] cells, double[] grad, double stepSize ) {

        for (int i=0;i<cells.length;i++) {
            MDSCell cell = cells[i];
            cell.dhat = - grad[i] * stepSize + cell.dhat ;
        }

        // Check to see cell's adjacencies are preserved.
        for (int i=0;i<cells.length;i++) {
            OrderedCell cell = ( OrderedCell ) cells[i];
            OrderedCell next = cell.next ;
            OrderedCell prev = cell.prev ;
            if ( prev != null && cell.dhat < prev.dhat )
                cell.dhat = prev.dhat ;
            if ( next != null && cell.dhat > next.dhat )
                cell.dhat = next.dhat ;
        }
    }

    /**
     *  Update vectors X based on gradient vector G and learning parameter S.
     */
    public static void test1() {
        int numVectors = 48 ;
        int numIters = 400 ;
        int dims = 4 ;

        double[][] X1 = new double[numVectors][dims] ;
        double[][] X2 = new double[numVectors][dims] ;
        double[][] G = new double[numVectors][dims] ;
        double[][] rscale = new double[numVectors][];

        for ( int i=0;i<X1.length;i++) {
           ArrayMath.rand( X1[i] ) ;
        }

        for ( int i=0;i<X1.length;i++) {
           ArrayMath.rand( X2[i] ) ;
        }

        int count = 0 ;
        MDSCell[] cells = new MDSCell[numVectors*numVectors];
        for (int i=0;i<numVectors;i++) {
            for (int j=0;j<numVectors;j++) {
                MDSCell cell = new MDSCell() ;
                cell.i = i ;
                cell.j = j ;
                cells[count++] = cell ;
            }
        }

        MDS mds = new MDS() ;
        //MDS.updateDhat( cells, X1 ) ;
        //Create a random scale for each row i and update the dhat values.
        //in cells.
        MDSTests.updateDissimRandomScale( 0.8f, 1.2f, cells, X1, rscale ) ;

        // Create a randomized dscale
        double[][] dscale = new double[numVectors][];
        double[][] sgrad = new double[numVectors][];
        for (int i=0;i<dscale.length;i++) {
            double[] tmp = new double[ rscale[i].length ] ;
            ArrayMath.rand( tmp ) ;
            ArrayMath.amult( tmp, 0.1f );
            ArrayMath.add( tmp, 1 ) ;
            dscale[i] = tmp ;
            sgrad[i] = new double[rscale[i].length] ;
        }
        MDSTests.updateDhatFromScaledDissim( cells, dscale ) ;

        MDS.Stress stress = new MDS.Stress() ;
        for (int i=0;i<numIters;i++) {
            MDS.updateDist( cells, X2 ) ;
            mds.computeStress( cells, stress ) ;
            if ( (i % 10)  == 0 )
              System.out.println( i + " Stress: " + stress.S ) ;
            MDS.computeGradient( cells, stress, X2, G ) ;
            MDS.updateVectors( X2, G, 0.5f, null ) ;
        }

        System.out.println( "\n Rescaling...\n" ) ;
        for (int i=0;i<numIters/4;i++) {
            mds.computeStress( cells, stress ) ;
            if ( ( i % 10 ) == 0 )
                System.out.println( i + "Stress : " + stress.S ) ;
            computeScaleGradient( stress, cells, dscale, sgrad ) ;
            MDS.updateVectors( dscale, sgrad, 1, null ) ;
            MDSTests.updateDhatFromScaledDissim( cells, dscale ) ;
        }

        System.out.println( "MDS" ) ;
        for (int i=0;i<numIters/4;i++) {
            MDS.updateDist( cells, X2 ) ;
            mds.computeStress( cells, stress ) ;
            if ( (i % 10)  == 0 )
              System.out.println( i + " Stress: " + stress.S ) ;
            MDS.computeGradient( cells, stress, X2, G ) ;
            MDS.updateVectors( X2, G, 0.5f, null ) ;
        }

        System.out.println( "\n Rescaling...\n" ) ;
        for (int i=0;i<numIters/8;i++) {
            mds.computeStress( cells, stress ) ;
            if ( ( i % 10 ) == 0 )
                System.out.println( i + "Stress : " + stress.S ) ;
            computeScaleGradient( stress, cells, dscale, sgrad ) ;
            MDS.updateVectors( dscale, sgrad, 1, null ) ;
            MDSTests.updateDhatFromScaledDissim( cells, dscale ) ;
        }

        System.out.println( "MDS" ) ;
        for (int i=0;i<numIters/4;i++) {
            MDS.updateDist( cells, X2 ) ;
            mds.computeStress( cells, stress ) ;
            if ( (i % 10)  == 0 )
              System.out.println( i + " Stress: " + stress.S ) ;
            MDS.computeGradient( cells, stress, X2, G ) ;
            MDS.updateVectors( X2, G, 0.5f, null ) ;
        }

    }

    /**
     *  Test normal MDS procedure.
     */
    public static void test2() {
        double ratio = 0.10 ;
        int numVectors = 500 ;
        int dims = 4 ;

        double[][] X1 = new double[numVectors][dims] ;
        double[][] X2 = new double[numVectors][dims] ;

        ArrayMath.rand( X1 ) ;
        ArrayMath.rand( X2 ) ;

        // Make a bunch of cells
        Vector cv = new Vector() ;
        for (int i=0;i<numVectors;i++) {
            for (int j=0;j<numVectors;j++) {
                if ( i!=j && Math.random() < ratio ) {
                    MDSCell cell = new MDSCell() ;
                    cell.i = i ;
                    cell.j = j ;
                    cv.addElement( cell ) ;
                }
            }
        }

        MDSCell[] cells = new MDSCell[ cv.size() ];
        for (int i=0;i<cells.length;i++) {
            cells[i] = ( MDSCell) cv.elementAt(i) ;
        }

        MDS mds = new MDS() ;
        MDS.updateDhat( cells, X1 ) ;  // Generate dhat values from test vectors.

        mds.setCells( cells ) ; // Set pre-allocated cells
        mds.setPointConfiguration( X2 ) ;  // set the initial point configuration

        mds.init() ;
        System.out.println( "Intial Stress: " + mds.s.S ) ;
        for ( int i=0;i<400;i++) {
            mds.iterate() ;
            if ( i % 20 == 0 )
                System.out.println( "Stress at iteration " + i + " : " + mds.s.S ) ;
        }
        try {
        Thread.sleep( 15000 ) ;
        }
        catch ( InterruptedException e ) {

        }
    }

    protected static int checkOrder( ArrayList[] lists ) {
        int count = 0 ;
        for (int i=0;i<lists.length;i++) {
            ArrayList list = lists[i] ;
            for (int j=0;j<list.size();j++) {
                OrderedCell cell = ( OrderedCell ) list.get(j) ;
                if ( cell.prev != null && cell.dhat < cell.prev.dhat ) {
                    System.out.println( "Cell " + i + " " + j + " is out of order." ) ;
                    count++ ;
                }
                else if ( cell.next != null && cell.dhat > cell.next.dhat ) {
                    System.out.println( "Cell " + i + " " + j + " is out of order." ) ;
                    count++ ;
                }
            }
        }
        return count ;
    }

    protected static void dumpCells( ArrayList[] lists ) {
        System.out.println("\nOld Dhat values:") ;
        for (int i=0;i<lists.length;i++) {
            ArrayList list = lists[i] ;
            System.out.print( "Row " + i + " ") ;
            for (int j=0;j<list.size();j++) {
                OrderedCell cell = ( OrderedCell ) list.get(j) ;
                System.out.print( j+ ":" + cell.olddhat ) ;
                System.out.print( " " ) ;
            }
            System.out.println() ;
        }

        System.out.println("\nDhat values:") ;
        for (int i=0;i<lists.length;i++) {
            ArrayList list = lists[i] ;
            System.out.print( "Row " + i + " ") ;
            for (int j=0;j<list.size();j++) {
                OrderedCell cell = ( OrderedCell ) list.get(j) ;
                System.out.print( j+ ":" + cell.dhat ) ;
                System.out.print( " " ) ;
            }
            System.out.println() ;
        }

        System.out.println("\nDDhat values:") ;
        for (int i=0;i<lists.length;i++) {
            ArrayList list = lists[i] ;
            System.out.print( "Row " + i + " ") ;
            for (int j=0;j<list.size();j++) {
                OrderedCell cell = ( OrderedCell ) list.get(j) ;
                System.out.print( j+ ":" + cell.ddhat ) ;
                System.out.print( " " ) ;
            }
            System.out.println() ;
        }
    }

    /**
     *  Test rescaling MDS procedure.
     */
    public static void test4() {
        int numVectors = 20 ;
        int dims = 3 ;
        double[][] X1 = new double[numVectors][dims] ;
        double[][] X2 = new double[numVectors][dims] ;
        ArrayList[] list = new ArrayList[numVectors];
        ArrayMath.rand( X1 ) ; ArrayMath.rand( X2 ) ;

        MDSCell[] cells = generateScaledData( X1, list ) ;
        RowScaledMDS mds = new RowScaledMDS() ;
        mds.setCells( cells ) ;
        mds.setPointConfiguration( X2 ) ;

        mds.init() ;
        System.out.println( "Intial Stress: " + mds.s.S ) ;
        for (int i=0;i<120;i++) {
            mds.iterate() ;
            if ( i % 20 == 0 )
                System.out.println( "Stress at iteration " + i + " : " + mds.s.S ) ;
        }

        System.out.println( "\n Rescaling:" ) ;
        for (int i=0;i<80;i++) {
            mds.iterateRescale() ;
            if ( i % 10 == 0 )
                System.out.println( "Stress at iteration " + i + " : " + mds.s.S ) ;
        }

        for (int j=0;j<2000;j++) {
            for (int i=0;i<40;i++) {
                mds.iterate() ;
            }
            if ( j % 5 == 0 ) {
                System.out.println( "\nMDS Fit" ) ;
                System.out.println( "Stress after fit: " + mds.s.S ) ;
            }
            //System.out.println( "\n Rescaling:" ) ;
            for (int i=0;i<50;i++) {
                mds.iterateRescale() ;
                int count = checkOrder(list) ; // Make sure that strictly monotonic increase of the groups
                if ( count > 0 ) {
                    System.out.println( "There were " + count + " violations detected: " ) ;
                    dumpCells( list ) ;
                    return ;
                }
            }
            //System.out.println( "Stress after rescaling: " + mds.s.S ) ;
        }

    }

    public static void main( String[] args ) {
        test4() ;

        try {
        Thread.sleep( 15000 ) ;
        }
        catch ( Throwable t ) {
        }
    }
}