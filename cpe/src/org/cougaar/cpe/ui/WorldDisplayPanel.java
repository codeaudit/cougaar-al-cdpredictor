package org.cougaar.cpe.ui;

import org.cougaar.cpe.model.*;
import org.cougaar.cpe.planning.zplan.ZoneWorld;
import org.cougaar.cpe.planning.zplan.Aggregate;
import org.cougaar.cpe.planning.zplan.BNAggregate;
import org.cougaar.cpe.planning.zplan.ZoneTask;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Iterator;
import java.util.ArrayList;

public class WorldDisplayPanel extends JPanel
{
//    private RefreshThread refreshThread;

    public WorldDisplayPanel()
    {
    }

    public WorldDisplayPanel(org.cougaar.cpe.model.WorldState ws)
    {
        this.ws = ws;
//        refreshThread = new RefreshThread();
//        refreshThread.start();
    }

    public JFrame getParentFrame()
    {
        return parentFrame;
    }

    public void setParentFrame(JFrame parentFrame)
    {
        this.parentFrame = parentFrame;
    }

    public WorldState getWorldState()
    {
        return ws;
    }

    public synchronized void setWorldState(WorldState ws)
    {
        this.ws = ws;
    }

    public synchronized void paint(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (ws == null)
        {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        AffineTransform at = g2.getTransform();
        //System.out.println("Default affine xform=" + at );

        Rectangle bounds = getBounds();
        double boardWidth = ws.getBoardWidth() , boardHeight = ws.getBoardHeight();
        borderSize = borderRatio * Math.max( boardWidth, boardHeight ) ;
        double scale = Math.min((bounds.getWidth()) / (boardWidth + borderSize * 2),
                (bounds.getHeight()) / (boardHeight + Math.abs(ws.getRecoveryLine()) + borderSize * 2));

        // at.translate( borderSize, borderSize );
        at.translate(borderSize * scale, (boardHeight + borderSize) * scale);
        at.scale(scale, -scale);

        g2.setTransform(at);
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke((float) (1 / scale)));
        g2.draw(new Rectangle2D.Double(0, ws.getRecoveryLine(), boardWidth, boardHeight + Math.abs(ws.getRecoveryLine())));
        g2.draw(new Line2D.Double(0, 0, ws.getBoardWidth(), 0));

        // This is the default xform.
        AffineTransform originalTransform = g2.getTransform();
        Iterator units = ws.getUnits();

        // Draw the zones.
        drawZones( originalTransform, g2, scale);

        FontMetrics fm = g2.getFontMetrics();
        float width = (float) (fm.getWidths()[0] / scale) * 0.7f;
        float height = (float) ((float) (fm.getMaxAscent() + fm.getMaxDescent()) / scale);

        while (units.hasNext())
        {
            org.cougaar.cpe.model.UnitEntity unitEntity = (org.cougaar.cpe.model.UnitEntity) units.next();
            drawUnitIcon(g2, unitEntity.getPosition(), scale, unitEntity.getRangeShape());
            g2.setColor(Color.WHITE);
            g2.translate(unitEntity.getX() - ((width * unitEntity.getId().length()) / 2), unitEntity.getY() - height * 1.1);
            g2.scale(1 / scale, -1 / scale);
            g2.drawString(unitEntity.getId(), 0, 0);

            // Reset back to the default xform.
            g2.setTransform(originalTransform);
        }

        drawUnitTasks( g2, scale, originalTransform ) ;

        Iterator supplyUnits = ws.getSupplyVehicleEntities();
        while (supplyUnits.hasNext())
        {
            SupplyVehicleEntity supplyEntity = (SupplyVehicleEntity) supplyUnits.next();
            g2.setColor(Color.YELLOW);
            drawUnitIcon(g2, supplyEntity.getPosition(), scale, null);
            g2.setColor(Color.WHITE);
            g2.translate(supplyEntity.getX() - width * supplyEntity.getId().length() / 2, supplyEntity.getY() - height * 1.1);
            g2.scale(1 / scale, -1 / scale);
            if (supplyEntity.getId() != null)
            {
                g2.drawString(supplyEntity.getId(), 0, 0);
            }
            g2.setTransform(originalTransform);
        }

        Iterator targets = ws.getTargets();
        while (targets.hasNext())
        {
            org.cougaar.cpe.model.Entity e = (org.cougaar.cpe.model.Entity) targets.next();
            GeneralPath path = new GeneralPath();
            if (e.isActive())
            {
                g2.setColor(Color.MAGENTA);
            }
            else
            {
                g2.setColor(Color.GREEN.darker());
            }
            path.moveTo((float) (targetPoints[0][0] / scale + e.getX()), (float) (targetPoints[0][1] / scale + e.getY()));
            for (int i = 1; i < targetPoints.length; i++)
            {
                double[] targetPoint = targetPoints[i];
                path.lineTo((float) (targetPoint[0] / scale + e.getX()), (float) (targetPoint[1] / scale + e.getY()));
            }
            path.lineTo((float) (targetPoints[0][0] / scale + e.getX()), (float) (targetPoints[0][1] / scale + e.getY()));
            g2.draw(path);
        }

        g2.setColor(Color.YELLOW);
        g2.draw(new Line2D.Double(0, ws.getPenaltyHeight(), ws.getBoardWidth(), ws.getPenaltyHeight()));

        if (ws.isLogEvents())
        {
            ArrayList events = ws.clearEvents();
            if (events != null)
            {
                for (int i = 0; i < events.size(); i++)
                {
                    Object o = events.get(i);
                    if (o instanceof WorldState.EngageByFireEvent)
                    {
                        WorldState.EngageByFireEvent e = (WorldState.EngageByFireEvent) o;
                        EntityInfo info = ws.getEntityInfo(e.getUnitId());
                        UnitEntity entity = (UnitEntity) info.getEntity();
                        EntityInfo tinfo = ws.getEntityInfo(e.getEr().getTargetId());
                        TargetEntity tentity = (TargetEntity) tinfo.getEntity();
                        GeneralPath path = new GeneralPath();
                        path.moveTo((float) entity.getX(), (float) entity.getY());
                        path.lineTo((float) tentity.getX(), (float) tentity.getY());
                        if (e.getEr().getAttritValue() == 0)
                        {
                            g2.setColor(Color.WHITE);
                        }
                        else
                        {
                            g2.setColor(Color.DARK_GRAY);
                        }
                        g2.draw(path);
                    }
                }
            }
        }
    }

    // TODO Finish this.
    private void drawUnitTasks(Graphics2D g2, double scale, AffineTransform originalTransform )
    {
        g2.setStroke(new BasicStroke((float) (1 / scale)));
        FontMetrics fm = g2.getFontMetrics();
        float width = (float) (fm.getWidths()[0] / scale) * 0.7f;
        float height = (float) ((float) (fm.getMaxAscent() + fm.getMaxDescent()) / scale);

        Iterator iter = ws.getUnits() ;
        while (iter.hasNext())
        {
            UnitEntity unitEntity = (UnitEntity) iter.next();
            if ( unitEntity.getManueverPlan() != null ) {
                Plan p = unitEntity.getManueverPlan() ;
                if ( p.getNumTasks() == 0 ) {
                    continue ;
                }

                // Find the first task to plot.
                for (int i=0;i<p.getNumTasks();i++) {
                    UnitTask task = (UnitTask) p.getTask(i) ;
                    if ( task.included( ws.getTime() ) || task.getStartTime() >= ws.getTime() ) {
                        long startTime = Math.max( ws.getTime(), task.getStartTime() ) ;
                        long visibleTime = ws.getTime() + ( long ) ( ws.getUpperY() / ( VGWorldConstants.getTargetMoveRate() ) * 1000 ) ;
                        // When this zone ends.
                        long endTime = Math.min( task.getEndTime(),  visibleTime )  ;

                        if ( startTime > visibleTime || endTime < ws.getTime() || endTime < startTime ) {
                            continue ;
                        }
                        float taskStartX ;
                        if ( i > 0 ) {
                            UnitTask prev = (UnitTask) p.getTask( i-1) ;
                            taskStartX = (float) prev.getDestX() ;
                        }
                        else {
                            taskStartX = unitEntity.getX() ;
                        }
                        float yLower = (float) ((VGWorldConstants.getTargetMoveRate() / 1000) * (startTime - ws.getTime())) + ( float ) ws.getLowerY() ;
                        float yUpper = (float) ((VGWorldConstants.getTargetMoveRate() / 1000) * (endTime - ws.getTime())) + ( float ) ws.getLowerY() ;

                        float lowerX = ( ( float ) (startTime - task.getStartTime() ) ) /
                                ( task.getEndTime() - task.getStartTime() )
                                * ( float ) ( task.getDestX() - taskStartX ) + taskStartX ;
                        float upperX = (float) (( ( endTime - task.getStartTime() ) /
                                ( task.getEndTime() - task.getStartTime() ) ) * ( task.getDestX() - taskStartX ) + taskStartX) ;
                        GeneralPath s = new GeneralPath() ;
                        s.moveTo( lowerX, yLower );
                        s.lineTo( upperX, yUpper );
                        g2.setColor( Color.LIGHT_GRAY );
                        g2.draw( s );

                        if ( i == p.getNumTasks() -1 ) {
                            g2.setColor(Color.LIGHT_GRAY);
                            g2.translate( upperX - ((width * unitEntity.getId().length()) / 2), yUpper + height * 1.1);
                            g2.scale(1 / scale, -1 / scale);
                            g2.drawString(unitEntity.getId(), 0, 0);

                            // Reset back to the default xform.
                            g2.setTransform(originalTransform);
                        }
                    }
                }
            }
        }
    }

    static Color[] c = {Color.BLUE.darker(), Color.GREEN.darker().darker()};

    private void drawZones( AffineTransform originalXForm, Graphics2D g2, double scale)
    {
        if (ws instanceof ZoneWorld)
        {
            FontMetrics fm = g2.getFontMetrics();
            float width = (float) (fm.getWidths()[0] / scale) * 0.7f;
            float height = (float) ((float) (fm.getMaxAscent() + fm.getMaxDescent()) / scale);

            ZoneWorld zw = (ZoneWorld) ws;
            for (int i = 0; i < zw.getNumAggUnitEntities(); i++)
            {
                BNAggregate agg = (BNAggregate) zw.getAggUnitEntity(i);
                Zone z = agg.getCurrentZone();

                // Set the color associated with this zone
                g2.setStroke(new BasicStroke((float) (1 / scale)));

                float lower, upper ;
                if (z != null)
                {
                    if (z instanceof IndexedZone)
                    {
                        lower = zw.getZoneLower((IndexedZone) z);
                        upper = zw.getZoneUpper((IndexedZone) z);
                    }
                    else
                    {
                        Interval interval = (Interval) z;
                        lower = interval.getXLower();
                        upper = interval.getXUpper();
                    }
                    // Draw the current zone as a bar on the bottom
                    g2.setColor(c[i % c.length]);
                    Rectangle2D r2d = new Rectangle2D.Double(lower, ws.getRecoveryLine() - 0.2, upper - lower, 0.2);
                    g2.fill(r2d);

                    // Draw the text at the appropriate location
                    g2.setColor(Color.WHITE);
                    g2.translate(lower + (upper - lower) / 2 - ( width * agg.getId().length() ) / 2, ws.getRecoveryLine() - 0.4 - height );
                    g2.scale(1 / scale, -1 / scale);
                    g2.drawString(agg.getId(), 0, 0);

                    // Reset back to the default xform.
                    g2.setTransform(originalXForm);
                }

                // Draw the text indicating the name of the unit on the bottom

                // Draw the zone plans if they exist.
                Plan psz = agg.getZonePlan();
                if (psz != null)
                {
                    for (int j = 0; j < psz.getNumTasks(); j++)
                    {
                        ZoneTask t = (ZoneTask) psz.getTask(j);
                        Zone sz = t.getStartZone() ;
                        Zone ez = t.getEndZone() ;
                        if ( sz instanceof  IndexedZone ) {
                            sz = zw.getIntervalForZone( ( IndexedZone ) sz ) ;
                        }
                        if ( ez instanceof  IndexedZone ) {
                            ez = zw.getIntervalForZone( ( IndexedZone ) ez ) ;
                        }
                        Interval si = (Interval) sz ;
                        Interval ei = (Interval) ez ;

                        // When this zone starts.
                        long startTime = Math.max( ws.getTime(), t.getStartTime() ) ;
                        long visibleTime = ws.getTime() + ( long ) ( ws.getUpperY() / ( VGWorldConstants.getTargetMoveRate() ) * 1000 ) ;
                        // When this zone ends.
                        long endTime = Math.min( t.getEndTime(),  visibleTime )  ;

                        if ( startTime > visibleTime || endTime < ws.getTime() || endTime < startTime ) {
                            continue ;
                        }

                        si = zw.interpolateIntervalForTask( t, startTime ) ;
                        ei = zw.interpolateIntervalForTask( t, endTime ) ;
                        float yLower = (float) ((VGWorldConstants.getTargetMoveRate() / 1000) * (startTime - ws.getTime())) + ( float ) ws.getLowerY() ;
                        float yUpper = (float) ((VGWorldConstants.getTargetMoveRate() / 1000) * (endTime - ws.getTime())) + ( float ) ws.getLowerY() ;

                        // Now, paint this "trapezoid"
                        GeneralPath path = new GeneralPath() ;
                        path.moveTo( si.getXLower(), yLower );
                        path.lineTo( si.getXUpper(), yLower );
                        path.lineTo( ei.getXUpper(), yUpper );
                        path.lineTo( ei.getXLower(), yUpper );
                        path.lineTo( si.getXLower(), yLower );
                        g2.setColor(c[i % c.length]);
                        g2.fill( path );
                        // Draw on top of this.
                        g2.setColor(Color.DARK_GRAY);
                        g2.draw( path );
                    }

//                    int index = psz.getIndexForTime( ws.getTime() ) ;
//                    long startTime = psz.getStartTimeForZone( index ), endTime = psz.getEndTimeForZone( index ) ;
//
//                    z = (IndexedZone) psz.getZone( index ) ;
//                    float lower = zw.getZoneLower( z ) ;
//                    float upper = zw.getZoneUpper( z ) ;
//
//                    Rectangle2D r2d = new Rectangle2D.Double( lower, 0, upper,
//                            ( VGWorldConstants.getTargetMoveRate() / 1000 ) * ( endTime - ws.getTime() ) ) ;
//                    g2.draw( r2d );
//
//                    for (int j=index+1;j<psz.getNumIndices();j++) {
//                        z = (IndexedZone) psz.getZone( j ) ;
//                        startTime = psz.getStartTimeForZone( index );
//                        endTime = psz.getEndTimeForZone( index ) ;
//                        double heightAboveZero =
//                                ( VGWorldConstants.getTargetMoveRate() / 1000 ) * ( startTime - ws.getTime() );
//                        r2d = new Rectangle2D.Double( lower, heightAboveZero,
//                                upper,
//                                ( VGWorldConstants.getTargetMoveRate() / 1000 ) * ( endTime - startTime ) ) ;
//                    }
                }
            }
        }
    }

    Color unitIconColor = Color.LIGHT_GRAY.darker();

    protected void drawUnitIcon(Graphics2D g2, Point2D position, double scale, Shape rs)
    {
        if (rs != null)
        {
            g2.setColor(Color.WHITE);
        }
        else
        {
            g2.setColor(Color.YELLOW);
        }
        Rectangle2D unitShape = new Rectangle2D.Double(-(iconWidth / scale) / 2 + position.getX(), -(iconHeight / scale) / 2 + position.getY(),
                iconWidth / scale, iconHeight / scale);
        g2.draw(unitShape);

        // Draw the range shape around each unit.
        if (rs != null)
        {
            AffineTransform at = new AffineTransform();
            at.translate(position.getX(), position.getY());
            GeneralPath p = new GeneralPath(rs);
            p.transform(at);
            g2.setColor(unitIconColor);
            g2.draw(p);
        }
    }

    public class RefreshThread extends Thread
    {

        public void run()
        {
            while (true)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                }

                if (ws != null)
                {
                    synchronized (ws)
                    {
                        repaint();
                    }
                }
                else
                {
                    repaint();
                }
            }
        }
    }

    JFrame parentFrame;
    static final double[][] targetPoints = {{-10, 0}, {0, 10}, {10, 0}, {0, -10}};
    static final double iconWidth = 17, iconHeight = 17;
    // static Rectangle2D unitShape = new Rectangle2D.Double( -10, -10, 20, 20 ) ;
    double borderSize = 1;
    double borderRatio = 0.08 ;
    org.cougaar.cpe.model.WorldState ws;

}
