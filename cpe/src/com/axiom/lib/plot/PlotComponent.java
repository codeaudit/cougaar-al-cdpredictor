package com.axiom.lib.plot;

import javax.swing.*;
import java.awt.*;

public class PlotComponent extends JComponent {

    public PlotComponent() {
        pc = new Plot2DContext() ;
    }

    public Plot2DContext getPlotContext() { return pc ; }

    public void paint( Graphics g ) {
        Rectangle r = this.getBounds() ;
        pc.setPlotBounds( 0, 0, r.width, r.height ) ;
        // g.setClip( 0, 0, r.width, r.height );
        pc.plot( g ) ;
    }

    Plot2DContext pc ;
}