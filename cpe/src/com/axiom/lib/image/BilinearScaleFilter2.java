package com.axiom.lib.image ;

import java.awt.image.* ;

/**
 *  Filters image using bilinear interpolation.  Note that only positive
 *  scales (e.g. "zooms" ) are supported at this time.  If the dest is smaller
 *  than the src, the behavior is not clear.
 */
public class BilinearScaleFilter2 extends ReplicateScaleFilter {

    public BilinearScaleFilter2( int w, int h ) {
        super( w, h ) ;
    }

    private void calculateMaps() {
//        System.out.println( "Calculate maps" );
//        System.out.println( "SrcWidth " + srcWidth + " SrcHeight " + srcHeight ) ;
//        System.out.println( "DestWidth " + destWidth + " DestHeight " + destHeight ) ;

        // What the @*#$( does this do?
	srcrows = new int[destHeight + 1];
	for (int y = 0; y <= destHeight; y++) {
	    srcrows[y] = (2 * y * srcHeight + srcHeight) / (2 * destHeight);
	}
	srccols = new int[destWidth + 1];
	for (int x = 0; x <= destWidth; x++) {
	    srccols[x] = (2 * x * srcWidth + srcWidth) / (2 * destWidth);
	}

        float xscale = ( ( float ) srcWidth - 1 ) / ( ( float ) destWidth );
        float yscale = ( ( float ) srcHeight - 1 ) / ( ( float ) destHeight ) ;

        srcxlarr = new int[destWidth+1];
        srcxuarr = new int[destWidth+1];
        srcxarr = new int[destWidth+1];

        for (int x = 0 ; x <= destWidth;x++) {
            float tmp = x * xscale ;
            srcxlarr[x] = (int) Math.floor( tmp ) ;
            srcxuarr[x] = (int) Math.ceil( tmp ) ;
            srcxarr[x] = (int) ( ( tmp - srcxlarr[x] ) * 255f );
        }

        srcylarr = new int[destHeight+1];
        srcyuarr = new int[destHeight+1];
        srcyarr = new int[destHeight+1];

        for (int y = 0 ; y <= destHeight;y++) {
            float tmp = y * yscale ;
            srcylarr[y] = (int) Math.floor( tmp ) ;
            srcyuarr[y] = (int) Math.ceil( tmp ) ;
            srcyarr[y] = (int) ( ( tmp - srcylarr[y] ) * 255f );
        }

        /*
        System.out.println( "srcrows" );
        for (int i=0;i<srcrows.length;i++) {
            System.out.print( srcrows[i] + " " );
        }

        System.out.println( "\nsrccols" );
        for (int i=0;i<srccols.length;i++) {
            System.out.print( srccols[i] + " " );
        }

        System.out.println( "\nsrcxlarr" );
        for (int i=0;i<srcxlarr.length;i++) {
            System.out.print( srcxlarr[i] + " " );
        }

        System.out.println( "\nsrcxuarr" );
        for (int i=0;i<srcxuarr.length;i++) {
            System.out.print( srcxuarr[i] + " " );
        }

        System.out.println( "\nsrcylarr" );
        for (int i=0;i<srcxlarr.length;i++) {
            System.out.print( srcylarr[i] + " " );
        }

        System.out.println( "\nsrcyuarr" );
        for (int i=0;i<srcxuarr.length;i++) {
            System.out.print( srcyuarr[i] + " " );
        }
        */
    }

    // Simply accumulate pixels until the buffer is complete, then call the old
    // setPixels.  Currently only works for single scan line at a time operation (
    // e.g. x-0) and hence is not general enough for real use.
    public void setPixels( int x, int y, int w, int h,
                            ColorModel model, int pixels[], int off,
			    int scansize )
    {
        if ( h == srcHeight ) {
           setPixels2( x, y, w, h, model, pixels, off, scansize ) ;
        }

        if ( intImgBuf == null ) {
           intImgBuf = new int[ srcHeight * srcWidth ] ;
        }

        //System.out.println( "setPixels: x: " + x + " y: " + y + ", w: " + w + ", h: " + h
        //                    + ", offset: " + off + ", scansize " + scansize ) ;

        int tindex = x + scansize * y ;
        for (int i=0;i<h;i++) {
            System.arraycopy( pixels, 0, intImgBuf, tindex, w ) ;
            tindex += scansize ;
        }

        if ( y + h >= srcHeight ) {
            setPixels2( 0, 0, srcWidth, srcHeight, model, intImgBuf, 0, scansize ) ;
        }
    }

    public void setPixels2(int x, int y, int w, int h,
			  ColorModel model, int pixels[], int off,
			  int scansize)
    {
        //System.out.println( "ColorModel " + model ) ;
        //System.out.println( "setPixels: x: " + x + " y: " + y + ", w: " + w + ", h: " + h
        //                   + ", offset: " + off + ", scansize " + scansize ) ;
	if (srcrows == null || srccols == null) {
	    calculateMaps();
	}
	int sx, sy;
	int dx1 = (2 * x * destWidth + srcWidth - 1) / (2 * srcWidth);
	int dy1 = (2 * y * destHeight + srcHeight - 1) / (2 * srcHeight);

        //System.out.print( "dx1 " + dx1 + ", dy1 " + dy1 ) ;

        // Obsolete.  Used only for debugging purposes.
        float xscale = ( ( float ) srcWidth ) / ( ( float ) destWidth );
        float yscale = ( ( float ) srcHeight ) / ( ( float ) destHeight ) ;

        //System.out.println( ", xscale " + xscale + " yscale " + yscale ) ;

	int outpix[];
	if (outpixbuf != null && outpixbuf instanceof int[]) {
	    outpix = (int[]) outpixbuf;
	} else {
	    outpix = new int[destWidth];
	    outpixbuf = outpix;
	}

        // Main rendering loop
	for (int dy = dy1; (sy = srcrows[dy]) < y + h; dy++) {
            int srcyl = srcylarr[dy];
            int srcyu = srcyuarr[dy];
            int srcoffl = off + scansize * (srcyl - y) ;
            int srcoffu = off + scansize * (srcyu - y) ;

	    int srcoff = off + scansize * (sy - y);
	    int dx ;
            for ( dx = dx1; (sx = srccols[dx]) < x + w; dx++) {
                int srcxl = srcxlarr[dx];
                int srcxu = srcxuarr[dx];

                //System.out.println( "dx " + dx + ", dy " + dy ) ;
                //System.out.println( "srcyl" + srcyl + ", srcyu : " + srcyu ) ;
                //System.out.println( "srcoffl " + srcoffl + " srcoffu " + srcoffu )  ;

                if ( srcxl == srcxu && srcyl == srcyu )
    		        outpix[dx] = pixels[srcoff + sx];
                else {
                    //System.out.println( "srcxl " + srcxl + " srcxu " + srcxu ) ;
                    //System.out.print( " srcoffl + srcxl =" + ( srcoffl + srcxl ) ) ;
                    //System.out.print( ", srcoffl + srcxu =" + ( srcoffl + srcxu ) ) ;
                    //System.out.print( ", srcoffu + srcxl =" + ( srcoffu + srcxl ) ) ;
                    //System.out.println( ", srcoffu + srcxu =" + ( srcoffu + srcxu ) ) ;
                    int pelvalue = average( model, pixels[srcoffl + srcxl], pixels[srcoffl + srcxu],
                                            pixels[srcoffu + srcxl], pixels[srcoffu + srcxu],
                                            srcxarr[dx], srcyarr[dy] ) ;
                    outpix[dx] = pelvalue ;
                }
	    }
	    if (dx > dx1) {
		consumer.setPixels(dx1, dy, dx - dx1, 1,
				   model, outpix, dx1, destWidth);
	    }
	}
    }

    public final int interp( int v1, int v2, float sfact ) {
        return ( int ) ( ( 1 - sfact ) * v1 + sfact * v2 );
    }

    public byte average( byte llpel, byte ulpel, byte lupel, byte uupel, byte distx, float disty )
    {
        int v1 = interp(llpel,ulpel,distx) ;
        int v2 = interp(llpel,lupel,disty) ;
        int v3 = interp(lupel,uupel,distx) ;
        int v4 = interp(ulpel,uupel,disty) ;

        return ( byte ) ( ( interp( v1, v3, disty ) + interp( v2, v4, distx ) ) / 2 ) ;
    }

    public int average( ColorModel model, int llpel, int ulpel, int lupel, int uupel, int distx, int disty ) {
        int result = 0 ;

        int rll = llpel >> 16 & 0xFF ;
        int gll = llpel >> 8 & 0xFF ;
        int bll = llpel & 0xFF ;

        int rul = ulpel >> 16 & 0xFF ;
        int gul = ulpel >> 8 & 0xFF ;
        int bul = ulpel & 0xFF ;

        int rlu = lupel >> 16 & 0xFF ;
        int glu = lupel >> 8 & 0xFF ;
        int blu = lupel & 0xFF ;

        int ruu = uupel >> 16 & 0xFF ;
        int guu = uupel >> 8 & 0xFF ;
        int buu = uupel & 0xFF ;

        int idistx = 255-distx;
        int idisty = 255-disty;

        int r1 = interp[rll][idistx] + interp[rul][distx];
        int r2 = interp[rll][idisty] + interp[rlu][disty];
        int r3 = interp[rlu][idistx] + interp[ruu][distx];
        int r4 = interp[rul][idisty] + interp[ruu][disty];

        int g1 = interp[gll][idistx] + interp[gul][distx];
        int g2 = interp[gll][idisty] + interp[glu][disty];
        int g3 = interp[glu][idistx] + interp[guu][distx];
        int g4 = interp[gul][idisty] + interp[guu][disty];

        int b1 = interp[bll][idistx] + interp[bul][distx];
        int b2 = interp[bll][idisty] + interp[blu][disty];
        int b3 = interp[blu][idistx] + interp[buu][distx];
        int b4 = interp[bul][idisty] + interp[buu][disty];

        int r = ( interp[r1][idisty] + interp[r3][disty] +
                  interp[r2][idistx] + interp[r4][distx] ) >> 1 ;
        int g = ( interp[g1][idisty] + interp[g3][disty] +
                  interp[g2][idistx] + interp[g4][distx] ) >> 1 ;
        int b = ( interp[b1][idisty] + interp[b3][disty] +
                  interp[b2][idistx] + interp[b4][distx] ) >> 1 ;

        /*
        int r1 = interp(rll,rul,distx) ;
        int r2 = interp(rll,rlu,disty) ;
        int r3 = interp(rlu,ruu,distx) ;
        int r4 = interp(rul,ruu,disty) ;

        int g1 = interp(gll,gul,distx) ;
        int g2 = interp(gll,glu,disty) ;
        int g3 = interp(glu,guu,distx) ;
        int g4 = interp(gul,guu,disty) ;

        int b1 = interp(bll,bul,distx) ;
        int b2 = interp(bll,blu,disty) ;
        int b3 = interp(blu,buu,distx) ;
        int b4 = interp(bul,buu,disty) ;

        int r = ( interp( r1, r3, disty ) + interp( r2, r4, distx ) ) >> 1 ;
        int g = ( interp( g1, g3, disty ) + interp( g2, g4, distx ) ) >> 1 ;
        int b = ( interp( b1, b3, disty ) + interp( b2, b4, distx ) ) >> 1 ;
*/
        return 0xFF << 24 | ( r & 0xFF ) << 16 | ( g & 0xFF ) << 8 | ( b & 0xFF );
    }

    int[] srcxlarr ;
    int[] srcxuarr ;
    int[] srcylarr, srcyuarr ;
    int[] srcxarr, srcyarr ;
    int[] intImgBuf ;

    /**
     *   256x256 interpolation table. inter[j][mix] = ( mix ) * ( j / 255 );
     */
    static int[][] interp ;

    static {
        interp = new int[256][256];

        for (int x=0;x<256;x++) {
            for (int mix=0;mix<256;mix++) {
                interp[x][mix] = (int) ( x * ( ( float ) mix ) / 255f ) ;
            }
        }
    }
}
