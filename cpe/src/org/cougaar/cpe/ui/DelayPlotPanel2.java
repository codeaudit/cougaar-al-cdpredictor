package org.cougaar.cpe.ui;

import com.axiom.lib.plot.PlotComponent;
import com.axiom.lib.plot.Plot2DContext;
import com.axiom.lib.plot.Plottable;
import com.axiom.lib.util.LongArray;
import com.axiom.lib.util.DoubleArray;

import org.cougaar.tools.techspecs.qos.DelayMeasurementPoint;
import org.cougaar.tools.techspecs.qos.DelayMeasurement;
import org.cougaar.tools.techspecs.qos.TimestampMeasurement;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.XYSeries;
import org.jfree.data.CategoryDataset;
import org.jfree.data.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class DelayPlotPanel2 extends JPanel implements MPObserver {
    private long[] timeArray;
    private long[] values;
    private long baseTime = 0 ;
    /**
     * Maximum time span in ms.
     */
    private long maxPlotDurationTime = 2000000 ;
    private long samplingRate = 5000L ;
    DelayMeasurementPoint dmp ;
    private AgentDisplayPlugin plugin;
    private JFreeChart chart;
    private XYSeries xyseries;
    private XYSeriesCollection collection;
    private ChartPanel panel;
    private JLabel infoLabel;

    public DelayPlotPanel2(AgentDisplayPlugin plugin, DelayMeasurementPoint dmp) {
        this.plugin = plugin ;
        this.dmp = dmp;
        baseTime = System.currentTimeMillis() ;
        setLayout( new BorderLayout() ) ;

        collection = new XYSeriesCollection();
        xyseries = new XYSeries( "Delay" ) ;
        collection.addSeries( xyseries );

        chart = ChartFactory.createXYLineChart(
            dmp.getName(),              // chart title
            "Time(s.)",                 // domain axis label
            "Delay(s.)",                // range axis label
            collection,                 // data
            PlotOrientation.VERTICAL,   // orientation
            false,                      // include legend
            true,                       // tooltips
            false                       // urls
        );
        infoLabel = new JLabel( "No data." ) ;

        panel = new ChartPanel( chart ) ;
        add( BorderLayout.CENTER, panel ) ;
        add( BorderLayout.SOUTH, infoLabel ) ;
    }

    public MeasurementPoint getMeasurementPoint()
    {
        return dmp ;
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

    public void updateData() {
        synchronized ( dmp ) {

            if ( dmp.getHistorySize() < 1 ) {
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
            long firstTime = dmp.getFirstLocalTime() ;
            long lastTime = dmp.getLastLocalTime() ;
            long startTime = Math.max( lastTime - maxPlotDurationTime, firstTime ) ;

            //values = dmp.getInterpolatedDelays( dmp.getFirstLocalTime(), currentTime, samplingRate ) ;
            values = dmp.getInterpolatedDelays( startTime, currentTime, samplingRate ) ;
            double[] delayArray = new double[values.length ] ;
            timeArray = new long[ values.length ] ;
            // long count = dmp.getFirstLocalTime() ;
            long count = 0 ;
            infoLabel.setText("Number of data values=" + timeArray.length );
            for (int i = 0; i < timeArray.length; i++) {
                delayArray[i] = values[i] / 1000.0 ;
                timeArray[i] = count ;
                count += samplingRate / 1000 ;
            }
//            LongArray times = new LongArray(timeArray) ;
//            DoubleArray delays = new DoubleArray(delayArray ) ;

            // Turn off notify and update the data set. From the beginning.
            chart.setNotify( false );
            collection.removeSeries( xyseries );
            xyseries.clear();
            for (int i=0;i<delayArray.length;i++) {
                xyseries.add( timeArray[i], delayArray[i], false );
            }
            collection.addSeries( xyseries );
            chart.setNotify( true );
        }
    }

}
