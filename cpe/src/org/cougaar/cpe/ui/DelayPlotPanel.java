package org.cougaar.cpe.ui;

import com.axiom.lib.plot.PlotComponent;
import com.axiom.lib.plot.Plot2DContext;
import com.axiom.lib.plot.Plottable;
import com.axiom.lib.util.LongArray;
import com.axiom.lib.util.DoubleArray;

import org.cougaar.tools.techspecs.qos.DelayMeasurementPoint;
import org.cougaar.tools.techspecs.qos.DelayMeasurement;
import org.cougaar.tools.techspecs.qos.TimestampMeasurement;

import java.awt.*;
import java.util.Iterator;

public class DelayPlotPanel extends PlotComponent {
    private long[] timeArray;
    private long[] values;
    private long baseTime = 0 ;
    /**
     * Maximum time span in ms.
     */
    private long maxPlotDurationTime = 1500000 ;
    private long samplingRate = 5000L ;
    DelayMeasurementPoint dmp ;
    private AgentDisplayPlugin plugin;

    public DelayPlotPanel(AgentDisplayPlugin plugin, DelayMeasurementPoint dmp) {
        this.plugin = plugin ;
        this.dmp = dmp;
        getPlotContext().setTitle( " Delay Measurement Point \"" + dmp.getName() + "\"" );
        getPlotContext().setXLabel( "Time (s.)" ) ;
        getPlotContext().setYLabel( "Delay (s.)" );
        if ( dmp.getHistorySize() > 1 ) {
            updateData();
        }
        else {
            getPlotContext().setAxisBounds( 0, 0, 1, 1 );
        }
        baseTime = System.currentTimeMillis() ;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public void paint(Graphics g) {
        super.paint(g);
    }

    protected void updateData() {
//        System.out.println("DEBUG:: Updating displayed data for " + dmp );
        synchronized ( dmp ) {
            Plot2DContext pc = getPlotContext() ;
            if ( dmp.getHistorySize() < 1 ) {
                pc.removeAllPlottable();
                pc.setXLabel( "No data");
                return ;
            }


            Iterator iter = dmp.getMeasurements() ;
            boolean found = false ;
            while (iter.hasNext()) {
                DelayMeasurement measurement = (DelayMeasurement) iter.next();
                if ( measurement.getTimestamp() == TimestampMeasurement.UNKNOWN_TIMESTAMP ||
                     measurement.getLocalTime() == TimestampMeasurement.UNKNOWN_TIMESTAMP )
                {
                    found = true ;
                }
            }

            long currentTime = System.currentTimeMillis() ;
            long lastTime = dmp.getLastLocalTime() ;
            long startTime = lastTime - maxPlotDurationTime ;

            if ( found || ( dmp.getFirstLocalTime() + samplingRate ) > currentTime  ) {
                pc.removeAllPlottable();
                pc.setXLabel( "No data");
                return ;
            }

            //values = dmp.getInterpolatedDelays( dmp.getFirstLocalTime(), currentTime, samplingRate ) ;
            values = dmp.getInterpolatedDelays( startTime, currentTime, samplingRate ) ;
            double[] delayArray = new double[values.length ] ;
            timeArray = new long[ values.length ] ;
            // long count = dmp.getFirstLocalTime() ;
            long count = 0 ;
            for (int i = 0; i < timeArray.length; i++) {
                delayArray[i] = values[i] / 1000.0 ;
                timeArray[i] = count ;
                count += samplingRate / 1000 ;
            }
            LongArray times = new LongArray(timeArray) ;
            DoubleArray delays = new DoubleArray(delayArray ) ;

            pc.setProperty( Plot2DContext.BACKGROUND_COLOR_PROPERTY, Color.WHITE );
            pc.removeAllPlottable();
            Plottable p = pc.addPlot(times, delays) ;
            p.setProperty( Plot2DContext.COLOR_PROPERTY, Color.BLUE );
            pc.fitToBounds();
//            System.out.println(dmp.getName() + ":: Times=" + times );
//            System.out.println(dmp.getName() + ":: Delays=" + delays );
//            System.out.println(dmp.getName() + ":: Bounds=" + pc.getPlotBounds() );

            pc.setTitle( "Delay Measurement Point \"" + dmp.getName() + "\"");
            pc.setXLabel( "Time (sec.) " ) ;
            repaint();
        }
    }

}
