package com.axiom.lib.awt ;
import java.awt.Color ;

/**
 *  Maps integers to color values. 
 */
public class ColorMap implements java.io.Serializable {
    public static final int COLOR_MAP_GRAY = 0 ;
    
    public static final int COLOR_MAP_HOT = 1;
    
    public static final int COLOR_MAP_COOL = 2;
    
    ColorMap() {
    }

    public ColorMap( Color[] map ) {
        this.map = ( Color[] ) map.clone()  ;
    }

    public int getSize() {
        return map.length ;
    }

    /**
     *   @param index Type of color map: COLOR_MAP_GRAY, COLOR_MAP_HOT, and COLOR_MAP_COOL.
     */
    public static final ColorMap getInstance( int index ) {
        switch ( index ) {
            case COLOR_MAP_GRAY :
                return GRAYMAP ;
            default :
                return null ;
        }
    }
    
    public Color get( int i ) {
        try {
            return map[i] ;
        }
        catch ( ArrayIndexOutOfBoundsException e ) {
            if ( i < 0 )
                return map[0];
            else if ( i >= map.length ) {
                return map[map.length-1] ;
            }
            else return null ;
        }
    }

    protected Color[] map ;

    private final static ColorMap GRAYMAP ;

    static {
        ColorMap colorMap = new ColorMap() ;

        // Make the GRAYMAP
        Color[] tempmap = new Color[256];
        for (int i=0;i<tempmap.length;i++) {
            tempmap[i] = new Color( i, i, i ) ;
        }
        colorMap.map = tempmap ;
        GRAYMAP = colorMap ;
    }

}