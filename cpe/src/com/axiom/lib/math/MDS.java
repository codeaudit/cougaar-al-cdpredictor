package com.axiom.lib.math ;
import com.axiom.lib.util.* ;

/**
 *  Base class for implementing MDS algorithm.
 */
public class MDS {

    /**
     *  Bundle of relevent stress values, where S is the stress and is equal to
     *  sqrt( SS / T ).
     */
    public static class Stress {
       public double SS, T, S ;
    }

    /**
     *  Compute stress, which is defined as
     *  S = sqrt( SS / T ) = sqrt( sum( dist - dhat)^2 / sum( dist^2) )
     */
    public void computeStress( MDSCell[] cells, Stress stress ) {
       double SS = 0, T = 0, S = 0 ;

       for (int i=0;i<cells.length;i++) {
           double dist = cells[i].dist ;
           T += dist * dist ;
           double tmp =  dist - cells[i].dhat ;
           SS += tmp * tmp ;
       }

       stress.S = ( double ) Math.sqrt( SS / T ) ;
       stress.T = T ;
       stress.SS = SS ;
    }

    /**
     *  Generate dhat information from vectors.  This is primarily used for
     *  testing, since ordinarily dhat data would be derived from the dissim
     *  values for each cell.
     */
    public static void updateDhat( MDSCell[] cells, double[][] X ) {
        updateValue( cells, X, 2, MDSCell.DHAT ) ;
    }

    /**
     *  Update distance values.
     */
    public static void updateDist( MDSCell[] cells, double[][] X ) {
        updateValue( cells, X, 2, MDSCell.DIST ) ;
    }

    /**
     *  Update dist parameter for a group of cells using L norm of vectors in X.
     *
     *  @param cells  An array of cells of size M
     *  @param X      An array of size M of double arrays, each of uniform size.
     *  @param field  One of MDSCell.DIST, MDSCell.DHAT, or MDSCell.DISSIM target fields to update.
     *  @param norm   The Lk parameter for the norm by which the interpoint distances are
     *                to be calculated.  norm=2 corresponds to the Euclidean distance
     */
    public static void updateValue( MDSCell[] cells, double[][] X, int norm, int field ) {
        switch ( norm ) {
          case 0 :
            throw new RuntimeException( "Invalid norm parameter." ) ;

          case 2 : // Usual L2 norm case is somewhat optimized.
            for (int i=0;i<cells.length;i++) {
                MDSCell cell = cells[i] ;
                double[] x1 = X[cell.i];
                double[] x2 = X[cell.j];
                double tmp = ArrayMath.dist( x1, x2 ) ;
                cell.set( field, tmp ) ;
            }
          break ;
          default :
            for (int i=0;i<cells.length;i++) {
                MDSCell cell = cells[i] ;
                double[] x1 = X[cell.i];
                double[] x2 = X[cell.j];
                double tmp = 0 ;
                for (int j=0;j<x1.length;j++) {
                   tmp += Math.pow( Math.abs( x1[j] - x2[j] ), norm ) ;
                }
                double res = ( double ) Math.pow( tmp, 1/norm ) ;
                cell.set( field, res ) ;
            }
        }
    }

    /**
     *  Update array X with new gradient value multiplied by a uniform stepsize.  Since
     *  this is gradient descent, we subtract the gradient value.
     */
    public static void updateVectors( double[][] X, double[][] G, double stepSize, boolean[] fixed ) {

        for (int i=0;i<X.length;i++) {
            if (fixed == null || !fixed[i] ) {
                double[] xi = X[i];
                double[] gi = G[i];
                for (int j=0;j<xi.length;j++) {
                    xi[j] = xi[j] - gi[j] * stepSize ;
                }
            }
        }
    }

    /**
     *  @param cells  A list of cells.
     *  @param X      An array of vectors of uniform size representing the current configuration.
     *  @param G     An array of gradients dS/dX to be updated by this method.
     */
    public static void computeGradient( MDSCell[] cells, Stress stress, double[][] X, double[][]G ) {
        for (int i=0;i<G.length;i++) {
           ArrayMath.set( G[i], 0 ) ;
        }
        double S = stress.S, T = stress.T, SS = stress.SS ;

        for (int i=0;i<cells.length;i++) {
            MDSCell cell = cells[i] ;
            if ( cell.i == cell.j ) {
                continue ;
            }

            double temp =  S * ( ( cell.dist - cell.dhat ) / SS - ( cell.dist ) / T ) / cell.dist ;
            double[] xi = X[cell.i]; double[] xj = X[cell.j];
            double[] gi = G[cell.i]; double[] gj = G[cell.j];

            for (int l=0;l<xi.length;l++) {
               double temp2 = temp * ( xi[l] - xj[l] );
               gi[l] += temp2 ;
               gj[l] -= temp2 ;
            }
        }
    }

    /** Return the current step size. */
    public double getStepSize() { return alpha ; }

    public int getIterCount() { return iterCount ; }

    protected static boolean ensureUniformSize( double[][] vectors ) {
        if ( vectors.length == 0 )
            return true ;

        int length = vectors[0].length ;

        for (int i=1;i<vectors.length;i++) {
            if ( vectors[i] == null || vectors[i].length != length )
                return false ;
        }

        return true ;
    }

    public void setCells( MDSCell[] cells ) {
        this.cells = cells ;
    }

    public int getFitDims() {
        return fitDims ;
    }

    /**
     *  Used to update the current (or initial) point configuration.
     */
    public void setPointConfiguration( double[][] points ) {
        if ( points.length > 0 && points[0] != null ) {
            fitDims = points[0].length ;
        }
        X = points ;
    }

    public void setFixed( boolean[] fixed ) {
        this.fixed = fixed ;
    }

    public double[][] getPointConfiguration() {
        return X ;
    }

    public double[][] getBestFitPointConfiguration() {
        return bestX ;
    }

    public void setBestFitPointConfiguration( double[][] best ) {
        bestX = best ;
    }

    public double getInitialStepSize() { return initialStepSize ; }

    public void setInitialStepSize( double stepsize ) { this.initialStepSize = stepsize ; }

    public Stress getStress() {
        return s ;
    }

    public Stress getBestStress() {
        return bestS ;
    }

    /**
     *  Initialize in prepration for a series of MDS fit iterations.
     */
    public void init() {
        if ( X == null )
            throw new RuntimeException( "No vector configuration initialized." ) ;
        if ( fitDims <= 0 )
            throw new RuntimeException( "Fit dimensions invalid : " + fitDims ) ;
        if ( !ensureUniformSize( X ) )
            throw new RuntimeException( "Initial configuration is not uniform." ) ;

        bestX = new double[X.length][fitDims];
        
        // Initialize some parameters
        alpha = initialStepSize ;
        iterCount = 0 ;
        updateDist( cells, X ) ;

        // Initialize the gradient arrays.  Each row is the grad of a single object i
        grad = new double[X.length][fitDims];
        prevGrad = new double[X.length][fitDims];

        // Run a single time to set up initial stress conditions
        computeStress( cells, s ) ;
        computeGradient( cells, s, X, grad ) ;
        computeStress( cells, s ) ;
        bestS.S = s.S ; bestS.SS = s.SS ; bestS.T = s.T ;

        // Compute the magnitude(?) of the gradient
        ArrayMath.copy( grad, prevGrad );
        oldMagGrad = magGrad = ArrayMath.L2Norm( grad ) ;
        stressBuf.add( new Double( s.S ) ) ;

        tmp = new double[fitDims];
    }

    /**
     *  Do a "restart" of the MDS process.
     */
    public void reinit() {
    }

    /**
     *  Compute the normalized magnitude by subtracting the component
     *  by component mean.
     */
    private double normMag( double[][] x1 ) {
        ArrayMath.set( tmp, 0 ) ;
        
        for (int i=0;i<x1.length;i++) {
            double[] tmp1 = x1[i];
            for (int j=0;j<tmp.length;j++) {
                tmp[j] += tmp1[j] ;
            }
        }

        ArrayMath.amult( tmp, 1d /x1.length ) ;

        double result = 0;
        for (int i=0;i<x1.length;i++) {
            for (int j=0;j<x1[i].length;j++) {
               double tmp2 = x1[i][j] - tmp[j] ;
               result += tmp2 * tmp2 ;
            }
        }
        return Math.sqrt( result ) ;
    }

    protected void updateBestConfig() {
    
    }

    /**
     *  Perform a single MDS iteration.
     */
    public void iterate() {
        updateDist( cells, X ) ;
        computeStress( cells, s ) ;

        if ( s.S < bestS.S ) {
            // Update best configuration
            ArrayMath.copy( X, bestX ) ;
            bestS.S = s.S ; bestS.SS = s.SS ; bestS.T = s.T ;
            updateBestConfig() ; // Call any customized config. update
        }

        computeGradient( cells, s, X, grad ) ;
        // Update using the current step size, and normalized gradient.
        magGrad = ArrayMath.L2Norm( grad ) ;
        double magX = normMag( X ) ;
        double stepSize = alpha * ( magX / magGrad );

        // Update the configuration vectors using the current stepsize
        updateVectors( X, grad, stepSize, fixed ) ;

        // Compute all the update parameters
        ArrayMath.amult( prevGrad, grad ) ;
        double cosGrad = ArrayMath.asum( prevGrad ) / ( magGrad * oldMagGrad );
        ArrayMath.copy( grad, prevGrad ); oldMagGrad = magGrad ;

        double angleFactor = Math.pow( 4, Math.pow(cosGrad,3) );
        double relaxFactor = 1.3 / ( 1 + Math.pow( Math.min( 1, s.S/ ( ( Double ) stressBuf.getLast() ).doubleValue() ), 5.0 ) );
        double goodLuckFactor = Math.min(1, s.S/ ( ( Double ) stressBuf.getFirst() ).doubleValue() );
        stressBuf.add( new Double( s.S ) );                  // Save most recent stress value
        alpha = alpha * angleFactor * relaxFactor * goodLuckFactor;

        // Clamp to max step size
        if ( alpha > maxStepSize )
            alpha = maxStepSize ;

        // Update number of iterations
        iterCount++ ;
    }

    /** Number of iterations. */
    int iterCount = 0 ;
    int fitDims = 3 ;
    double initialStepSize = 0.2 ;
    double maxStepSize = 2.0 ;
    double alpha, oldMagGrad, magGrad ;
    double[][] grad, prevGrad ;
    Stress s = new Stress() ;
    Stress bestS = new Stress() ;
    CircularArray stressBuf = new CircularArray(5) ;

    /** Allocated cells for each i,j pair.*/
    MDSCell[] cells ;


    /** Configuration vectors. */
    double[][] X ;
    double[][] bestX ;

    /** Indicates which vectors are fixed. */
    boolean[] fixed ;

    private double[] tmp ;
}