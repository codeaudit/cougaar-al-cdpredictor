package org.cougaar.cpe.ui;

import com.axiom.lib.plot.PlotComponent;
import com.axiom.lib.plot.Plot2DContext;
import com.axiom.lib.plot.GeometryPlot;
import com.axiom.lib.plot.Plottable;
import org.cougaar.tools.techspecs.qos.EventDurationMeasurementPoint;
import org.cougaar.tools.techspecs.qos.DelayMeasurement;
import org.cougaar.tools.techspecs.qos.TimestampMeasurement;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;

import javax.swing.*;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;
import java.awt.*;

/**
 * User: wpeng
 * Date: Sep 20, 2003
 * Time: 8:18:17 PM
 */
public class EventDurationPlotPanel extends PlotComponent implements MPObserver
{
    EventDurationMeasurementPoint emp ;
    private AgentDisplayPlugin plugin;
    private long baseTime ;
    private long maxPlotDuration = 1500000;

    public EventDurationPlotPanel(AgentDisplayPlugin plugin, EventDurationMeasurementPoint emp )
    {
        this.plugin = plugin ;
        this.emp = emp;
        getPlotContext().setTitle( " Event Duration MP \"" + emp.getName() + "\"" );
        getPlotContext().setXLabel( "Time (secs)" ) ;
        getPlotContext().setYLabel( "Duration (s.)" );
        if ( emp.getHistorySize() > 1 ) {
            updateData();
        }
        else {
            getPlotContext().setAxisBounds( 0, 0, 1, 1 );
        }
        baseTime = System.currentTimeMillis() ;
    }

    public long getMaxPlotDuration()
    {
        return maxPlotDuration;
    }

    public void setMaxPlotDuration(long maxPlotDuration)
    {
        this.maxPlotDuration = maxPlotDuration;
    }

    public void updateData()
    {
        synchronized ( emp ) {
            Plot2DContext pc = getPlotContext() ;
            if ( emp.getHistorySize() < 1 ) {
                pc.removeAllPlottable();
                pc.setXLabel( "No data");
                return ;
            }

            // The base time is the minimum time.
            DelayMeasurement first = (DelayMeasurement) emp.getFirstMeasurement() ;
            DelayMeasurement last = (DelayMeasurement) emp.getLastMeasurement() ;
            long startTime = last.getTimestamp() - maxPlotDuration ;

            // Just start the base time at the beginning.
            baseTime = first.getTimestamp() ;
            long limitTime = 0L ;
            Iterator iter = emp.getMeasurements() ;
            while (iter.hasNext()) {
                DelayMeasurement dm = (DelayMeasurement) iter.next() ;
                limitTime = Math.max( dm.getTimestamp(), limitTime ) ;
            }

            double duration = ( limitTime - baseTime ) / 1000.0 ;
            double width =  duration / 300 ;
            if ( width < 1 ) {
                width = 1 ;
            }

            iter = emp.getMeasurements() ;
            int i = 0 ;
            while (iter.hasNext()) {
                DelayMeasurement dm = (DelayMeasurement) iter.next() ;
                // Beginning time of event.
                double eventTime = dm.getTimestamp() ;
                if ( eventTime < startTime ) {
                    continue;
                }
                // Delay in seconds
                double value = dm.getDelay() / 1000.0 ;

                Rectangle2D r2 = new Rectangle2D.Double( ( eventTime - baseTime ) / 1000.0, 0, value , value ) ;
                GeometryPlot gp;
                pc.addPlottable( gp = new GeometryPlot( r2 ) );
                if ( i % 2 == 0 ) {
                    gp.setProperty( Plottable.COLOR_PROPERTY, Color.BLUE );
                }
                else {
                    gp.setProperty( Plottable.COLOR_PROPERTY, Color.RED );
                }
                gp.setProperty( GeometryPlot.FILLED_PROPERTY, Boolean.TRUE );
                i++ ;
            }
            pc.fitToBounds();
            pc.setXLabel( "Time (sec.), # measurements=" + emp.getHistorySize() ) ;
        }
        SwingUtilities.invokeLater( new Runnable() {
            public void run()
            {
                repaint();
            }
        });
    }

    public MeasurementPoint getMeasurementPoint()
    {
        return emp ;
    }

}
