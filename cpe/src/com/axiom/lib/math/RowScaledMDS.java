package com.axiom.lib.math ;
import com.axiom.lib.util.* ;

public class RowScaledMDS extends MDS {

    /**
     *  Assuming a linear scaling function f, e.g. dhat = f(dissim,alpha) = alpha * dissim
     *  for each row, compute the gradient d Stress / d alpha.
     */
    public static void computeRowScaleGradient( MDS.Stress stress,
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
     *  Compute the gradient dS/ d dissim for each cell individually.  The assumption here
     *  is that the ordering is parametrized for each cell in each row independently.
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
            OrderedCell cell = ( OrderedCell ) cells[i];
            cell.dhat = - grad[i] * stepSize + cell.dhat;
        }

        // Check to see cell's adjacencies are preserved.
        for (int i=0;i<cells.length;i++) {
            OrderedCell cell = ( OrderedCell ) cells[i];
            OrderedCell next = cell.next ;
            OrderedCell prev = cell.prev ;
            if ( prev != null && cell.dhat < prev.dhat )
                cell.dhat = cell.dhat;
            if ( next != null && cell.dhat > next.dhat )
                cell.dhat = next.dhat ;
        }
    }

    public static void updateOrderPreserving2( MDSCell[] cells, double[] grad, double stepSize ) {

        for (int i=0;i<cells.length;i++) {
            OrderedCell cell = ( OrderedCell ) cells[i];
            cell.olddhat = cell.dhat ;
            cell.ddhat = - grad[i] * stepSize ;
        }

        for (int i=0;i<cells.length;i++) {
            OrderedCell cell = ( OrderedCell ) cells[i];
            OrderedCell next = cell.next ;
            OrderedCell prev = cell.prev ;

            // Limit gradient to at most 1
            if ( cell.ddhat < -1 ) cell.ddhat = -1 ;
            else if ( cell.ddhat > 1 ) cell.ddhat = 1 ;

            if ( cell.ddhat <0 ) {
                double r = 0.96;
                if ( prev != null ) {
                   if ( prev.ddhat > 0 )
                       r = Math.abs( cell.ddhat / ( Math.abs(cell.ddhat) + Math.abs(prev.ddhat) ) );

                   cell.dhat = cell.dhat + ( cell.dhat - prev.dhat ) * cell.ddhat * r ;
                }
                else
                    cell.dhat+= cell.ddhat ;
            }
            if ( cell.ddhat > 0 ) {
                double r = 0.96 ;

                if ( next != null ) {
                    if ( next.ddhat < 0 )
                        r = Math.abs( cell.ddhat / ( Math.abs(cell.ddhat) + Math.abs(next.ddhat) ) ) ;
                    cell.dhat = cell.dhat + ( next.dhat - cell.dhat ) * cell.ddhat * r ;
                }
                else
                    cell.dhat+= cell.ddhat ;
            }
        }
    }

    public double[] getBestDhat() {
        return bestDhat ;
    }

    protected void updateBestDhat() {
        for (int i=0;i<cells.length;i++) {
            bestDhat[i] = cells[i].dhat ;
        }
    }

    public double getRescaleStepSize() { return rescaleStepSize ; }

    public void setRescaleStepSize( double ss ) { rescaleStepSize = ss ; }

    public void init() {
        // Validate the cells, rows, etc to insure all conditions are met.
        super.init() ;
        cellGrad = new double[cells.length] ;
        bestDhat = new double[cells.length] ;
        rescaleCount = 0 ;
        updateBestDhat() ;
    }

    /** Overrides standard update of the best configuration. */
    protected void updateBestConfig() {
        super.updateBestConfig() ;
        updateBestDhat() ;
    }

    /**
     *  Order-preserving rescaling iteration for each cell to minimize stress.  It relies
     *  on valid stress values.
     */
    public void iterateRescale() {
        computeStress( cells, s ) ;
        computeCellDissimGradient( s, cells, cellGrad ) ;
        //updateOrderPreserving( cells, cellGrad, rescaleStepSize ) ;
        updateOrderPreserving2( cells, cellGrad, rescaleStepSize ) ;
        rescaleCount++ ;
    }

    double rescaleStepSize = 0.1 ;

    double[] cellGrad ;

    /** Rescaled dhat values corresponding to the best stress. */
    double[] bestDhat ;

    double[][] rowScale ;

    int rescaleCount ;
}