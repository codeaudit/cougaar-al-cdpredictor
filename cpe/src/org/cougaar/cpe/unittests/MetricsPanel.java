package org.cougaar.cpe.unittests;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.ReferenceWorldState;
import org.cougaar.cpe.model.WorldMetrics;
import org.cougaar.cpe.model.MeasuredWorldMetrics;
import org.cougaar.tools.techspecs.qos.TimePeriodMeasurementPoint;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Create a metrics panel.
 */
public class MetricsPanel extends  JPanel {
    private WorldState ws;
    private GridBagLayout gbl;
    private int numPanelsPerRow = 4 ;
    private Dimension minimumSize = new Dimension(320,240) ;
    private ArrayList measurementPoints;
    private ArrayList panels = new ArrayList();

    public MetricsPanel(WorldState ws) {
        this.ws = ws;
        makePanels(ws);

        thread.start();
    }

    private void makePanels(WorldState ws)
    {
        if ( ws == null ) {
            return ;
        }

        MeasuredWorldMetrics wm = (MeasuredWorldMetrics) ws.getDefaultMetric() ;
        if ( wm == null ) {
            return ;
        }

        measurementPoints = new ArrayList() ;

        measurementPoints.add( wm.getEntryRate() ) ;
        measurementPoints.add( wm.getKills() ) ;
        measurementPoints.add( wm.getAttrition() ) ;
        measurementPoints.add( wm.getPenalties() ) ;
        measurementPoints.add( wm.getViolations() ) ;

        setLayout( gbl = new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ;
        gbc.weighty = 100 ;
        int gridX = 0 , gridY = 0;
        for (int i=0;i<measurementPoints.size();i++) {
            TimePeriodMeasurementPoint timePeriodMeasurementPoint = (TimePeriodMeasurementPoint) measurementPoints.get(i) ;
            MetricsGraphPanel panel = new MetricsGraphPanel( timePeriodMeasurementPoint ) ;
            panel.setPreferredSize( minimumSize );
            panel.setMinimumSize( minimumSize );
            gridX = i % numPanelsPerRow ;
            gridY = i / numPanelsPerRow ;
            System.out.println("GridX=" + gridX + ",GridY=" + gridY );
            gbc.gridx = gridX; gbc.gridy = gridY ;
            gbl.setConstraints( panel, gbc );
            add( panel ) ;
            panels.add( panel ) ;
        }
    }

    public void setWorldState(WorldState ws)
    {
        this.ws = ws;
        removeAll();
        makePanels( ws );
    }

    private void updateData()
    {
        for (int i = 0; i < panels.size(); i++) {
            MetricsGraphPanel metricsGraphPanel = (MetricsGraphPanel)panels.get(i);
            metricsGraphPanel.updateData();
        }
    }

    protected class RefreshThread extends Thread {
        public void run()
        {
            while ( true ) {
                try
                {
                    sleep(2000) ;
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                updateData() ;
            }
        }

    }

    RefreshThread thread = new RefreshThread() ;
}
