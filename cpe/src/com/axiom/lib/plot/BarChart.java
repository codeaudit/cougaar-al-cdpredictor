package com.axiom.lib.plot ;
import com.axiom.lib.util.NumberArray ;
import java.awt.geom.Rectangle2D ;
import com.axiom.lib.util.DoubleArray ;
import java.util.Enumeration ;
import com.axiom.lib.util.ArrayMath ;

public abstract class BarChart extends AbstractPlot {

    public BarChart( NumberArray x, NumberArray y ) {

    }

    public boolean hasChildren() {
        return false ;
    }

    public Enumeration elements() {
        return null ;
    }

}