package com.axiom.lib.awt.geom ;
import java.awt.* ;
import java.awt.geom.* ;
import com.axiom.lib.util.* ;
import com.axiom.lib.awt.* ;

public class MatrixRenderer {

    /**
     *  @param A FloatMatrix, IntMatrix or ShortMatrix.
     *  @return a 2D slice of the matrix arranged in
     *   FloatMatrix form.
     */
    protected FloatMatrix getSlice( Object o ) {
        FloatMatrix tmp = null ;

        if ( o instanceof ShortMatrix ) {
            ShortMatrix m = ( ShortMatrix ) o ;
            int[] dims = m.getDimensions() ;
            int xd = dims[ xdim ] ;
            int yd = dims[ ydim ] ;
            int[] tindx = ( int[] ) index.clone() ;
            tmp = new FloatMatrix( xd, yd ) ;

            for (int i=0;i<xd;i++) {
                for (int j=0;j<yd;j++) {
                    tindx[xdim] = i; tindx[ydim] = j ;
                    float value = m.at(tindx) ;
                    tmp.set( i, j, value ) ;
                }
            }
        }
        else if ( o instanceof FloatMatrix ) {
            FloatMatrix m = ( FloatMatrix ) o ;
            int[] dims = m.getDimensions() ;
            int xd = dims[ xdim ] ;
            int yd = dims[ ydim ] ;
            int[] tindx = ( int[] ) index.clone() ;
            tmp = new FloatMatrix( xd, yd ) ;

            for (int i=0;i<xd;i++) {
                for (int j=0;j<yd;j++) {
                    tindx[xdim] = i; tindx[ydim] = j ;
                    float value = m.at(tindx) ;
                    tmp.set( i, j, value ) ;
                }
            }
        }
        else if ( o instanceof IntMatrix ) {
            IntMatrix m = ( IntMatrix ) o ;
            int[] dims = m.getDimensions() ;
            int xd = dims[ xdim ] ;
            int yd = dims[ ydim ] ;
            int[] tindx = ( int[] ) index.clone() ;
            tmp = new FloatMatrix( xd, yd ) ;

            for (int i=0;i<xd;i++) {
                for (int j=0;j<yd;j++) {
                    tindx[xdim] = i; tindx[ydim] = j ;
                    float value = m.at(tindx) ;
                    tmp.set( i, j, value ) ;
                }
            }
        }

        return tmp ;
    }

    public boolean getScaleToFit() { return true ; }

    public int getFirstDimension() { return xdim ; }

    public int getSecondDimension() { return ydim ; }

    public float getScale() { return scale ; }

    public void setScale( float newScale ) { this.scale = newScale ; }

    public Point getOrigin() { return origin ; }

    public void paint( Component c, Graphics g, Object o ) {

        Rectangle r = c.getBounds() ;
        g.setColor( Color.gray ) ;
        g.fillRect( 0, 0, r.width, r.height ) ;
        int xoffset = getOrigin().x , yoffset = getOrigin().y ;
        FloatMatrix fm = getSlice( o ) ;
        float lmin, lmax ;
        float fscale ;

        if ( getScaleToFit() ) {
            fscale = ( float ) Math.min( r.getWidth() / ( float ) fm.getNumColumns(),
                              r.getHeight() / ( float ) fm.getNumRows() ) ;
        }
        else {
            fscale = getScale() ;
        }

        lmin = fm.min() ; lmax = fm.max() ;
        float vscale = 255f / ( ( float ) ( lmax - lmin ) );
        Point2D src1 = new Point2D.Float(), dest1 = new Point2D.Float(),
           src2 = new Point2D.Float(), dest2 = new Point2D.Float() ;
        if ( fm != null ) {
            // Create an affine xform to rotate and scale
            AffineTransform xform = new AffineTransform() ;
            // xform.scale( getScale(), getScale() );
            xform.rotate( -Math.PI / 2 );
            xform.scale( -fscale , fscale ) ;
            xform.translate( xBorder / fscale, yBorder / fscale );
            src1.setLocation( -1/fscale, -1/fscale );
            src2.setLocation( fm.getNumRows() + 1/fscale, fm.getNumColumns() + 1/fscale) ;
            xform.transform( src1, dest1 ) ;
            xform.transform( src2, dest2 ) ;
            g.setColor( Color.white ) ;
            g.drawRect( ( int ) dest1.getX(), (int )dest1.getY(),
                        ( int ) ( dest2.getX() - dest1.getX() ),
                        ( int ) ( dest2.getY() - dest1.getY() ) );

            for ( int i=0;i<fm.getNumRows();i++) {
                for (int j=0;j<fm.getNumColumns();j++) {
                   src1.setLocation( i,  j ) ;
                   src2.setLocation( i + 1, j + 1 ) ;
                   xform.transform( src1, dest1 ) ;
                   xform.transform( src2, dest2 ) ;
                   float value = fm.at(i,j) ;
                   int ivalue = ( int ) ( ( value - lmin ) * vscale ) ;
                   g.setColor( map.get(ivalue) );
                   g.fillRect( ( int ) dest1.getX(), ( int ) dest1.getY(),
                               ( int ) ( dest2.getX() - dest1.getX() ),
                               ( int ) ( dest2.getY() - dest1.getY() ) );
                }
            }
        }
    }

    int[] index = { 0, 0 } ;
    int xdim = 0, ydim = 1;
    float scale = 4, xBorder = 0, yBorder = 0 ;
    Point origin = new Point( 10, 10 );
    ColorMap map = ColorMap.getInstance( ColorMap.COLOR_MAP_GRAY ) ;
}