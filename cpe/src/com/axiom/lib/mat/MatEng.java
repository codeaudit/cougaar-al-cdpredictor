package com.axiom.lib.mat ;
import com.axiom.lib.util.* ;
import java.lang.Class;
import java.lang.Void;

/**
 *   A Jave interface to the Matlab engine.
 */
public class MatEng {

    public native void open( String cmd ) ;

    public native void close() ;

    /** Evaluate a Matlab command.
     */
    public native void evalString( String cmd ) ;

    /** Output buffering doesn't work yet.
     */
    public void setBufSize( int size ) {
        if ( size < 0 ) throw new RuntimeException( "Invalid size." ) ;
        bufLen = size ;
        buffer( size ) ;
    }

    public boolean isOpen() {
        return engPtr != 0 ;
    }

    /**  Return an instance of MatEng.
     */
    public synchronized static MatEng getInstance() {
        if ( instance == null ) {
            instance = new MatEng() ;
            instance.open( null ) ;
        }
        return instance ;
    }

    /** Get the current output buffer.
     */
    public native char[] getBuffer() ;

    public void putArray( String name, FloatMatrix matrix ) {
        if ( matrix == null || matrix.getRank() != 2 || name == null ||
             name.length() == 0 )
        {
            throw new RuntimeException() ;
        }
        float[] array = matrix.getArray() ;
        double[] da = new double[ array.length ];
        ArrayMath.cast( array, da ) ;

        int[] dims = matrix.getDimensions() ;
        putArray( name, dims[0], dims[1], da ) ;
    }

    //public void putArray( String name, int w, int h, double[] array ) {
    //    if ( array.length < w * h ) {
    //        throw new IllegalArgumentException() ;
    //    }
    //    _putArray( name, w, h, array ) ;
    //}

    private native void putArray( String name, int w, int h, double[] array ) ;

    /** Return a matrix in the current workspace.
     *  @param name The name of the variable.
     *  @return a matrix.
     */
    public native FloatMatrix getArray( String name ) ;

    static {
        try {
            Runtime.getRuntime().loadLibrary( "mat" ) ;
        }
        catch ( Exception e ) {
            System.out.println( e ) ;
        }
    }

    private native void buffer( int size ) ;

    /** Frees any memory used for buffering results. */
    private native void freeBuf() ;

    /** Pointer to internal character buffer. */
    private volatile long bufPtr = 0L ;
    private volatile int bufLen = 0 ;
    /** Pointer to MATLAB engine. */
    private volatile long engPtr = 0L ;

    protected void finalize() throws java.lang.Throwable {
        super.finalize();
        close() ; // Always close the engine
        if ( bufPtr != 0 ) {
           bufLen = 0 ;
           freeBuf() ;
        }
    }

    private volatile static MatEng instance = null ;

    public static void main( String[] args ) {
        java.awt.Frame frame = new java.awt.Frame() ;
        frame.setSize( 640, 480 ) ;
        frame.setVisible( true ) ;
        MatEng eng = new MatEng() ;
        eng.open(null) ;
        eng.setBufSize( 1024 );
        eng.evalString( "mice = [ 4 4 4 ; 2 3 4 ]" ) ;
        FloatMatrix fm = new FloatMatrix( 4, 4 ) ;
        fm.seqdiag();
        eng.putArray( "moose", fm );
        eng.evalString( "fig1 = surf( moose )" );
        FloatMatrix fig1 = eng.getArray( "fig1" ) ;
        FloatMatrix mice = eng.getArray( "mice" ) ;
        System.out.println( "fig1=" + fig1.toString() ) ;
        System.out.println( "mice=" + mice ) ;
        //char[] c = eng.getBuffer() ;
        //String s = new String( c ) ;
        eng.close() ;

        eng.open(null) ;
        eng.evalString( "x=1:.2:10" );
        eng.evalString( "y=sin(x);z=cos(x);figure;plot3( x,y,z )" ) ;
        //eng.close() ;
    }
}
