package com.axiom.lib.image ;    
    
/**
 *  Bitmap format information for various formats.
 */
public class Format {

    public static final int READ_1BPP = 0x0001 ;
    
    public static final int READ_4BPP = 0x0002 ;

    public static final int READ_8BPP = 0x0004 ;
    
    public static final int READ_16BPP = 0x0008 ;
    
    public static final int READ_24BPP = 0x0010 ;
    
    public static final int WRITE_1BPP = 0x0001 << 6 ;
    
    public static final int WRITE_4BPP = 0x0002 << 6 ;

    public static final int WRITE_8BPP = 0x0004 << 6 ;
    
    public static final int WRITE_16BPP = 0x0008 << 6 ;
    
    public static final int WRITE_24BPP = 0x0010 << 6 ;    
        
    public String shortName ;
    
    public String longName ;
    
    public String[] extensions ;
    
    /**
     *  Flags for R and W capabilities associated with this package.
     */
    public int capabilities ;
    
    public Bitmap getModule() {
        return module ;
    }
    
    /**
     * Identifies bitmap module associated with this format.
     */
    Bitmap module ;
}
