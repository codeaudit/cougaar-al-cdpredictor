package org.cougaar.cpe.unittests;

import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurement;
import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.cpe.ui.MPObserver;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * A graph corresponding to each metric type.
 */

public class MetricsGraphPanel extends JPanel implements MPObserver {
    private XYSeriesCollection collection;
    private JFreeChart chart;
    private XYSeries series;
    private ChartPanel panel;

    public MetricsGraphPanel(TimePeriodMeasurementPoint tmp)
    {
        this.tmp = tmp;
        setLayout( new BorderLayout() );
        collection = new XYSeriesCollection() ;
        series = new XYSeries( "Value" ) ;
        collection.addSeries( series ); ;
        chart = ChartFactory.createXYLineChart( tmp.getName(), "Time(sec.)", "Value",
                collection, PlotOrientation.VERTICAL, false, false, false ) ;
        panel = new ChartPanel( chart ) ;
        add( panel, BorderLayout.CENTER );
    }

    public MeasurementPoint getMeasurementPoint()
    {
        return tmp ;
    }

    public void updateData() {
        chart.setNotify(false);

        collection.removeSeries( series );
        series.clear();
        synchronized ( tmp ) {
            Iterator iter = tmp.getMeasurements() ;
            while (iter.hasNext())
            {
                TimePeriodMeasurement measurement = (TimePeriodMeasurement) iter.next();
                Object value = measurement.getValue() ;
                if ( value instanceof Number ) {
                    series.add( measurement.getStartTime() /1000.0, ( (Number) value).doubleValue() );
                }
            }
        }
        collection.addSeries( series );
        chart.setNotify( true );
    }

    TimePeriodMeasurementPoint tmp ;
}
