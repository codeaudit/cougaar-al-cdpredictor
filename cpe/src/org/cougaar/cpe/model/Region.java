package org.cougaar.cpe.model;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Region implements Serializable
{
    public Region(Shape area, String regionName)
    {
        this.area = area;
        this.regionName = regionName;
    }

    public Shape getArea()
    {
        return area;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void writeObject( ObjectOutputStream oos ) {
        try {
            oos.defaultWriteObject();
            if ( area instanceof Rectangle2D ) {
                oos.writeBoolean( true );
                Rectangle2D r2d = (Rectangle2D) area ;
                oos.writeDouble( r2d.getX() );
                oos.writeDouble( r2d.getY() );
                oos.writeDouble( r2d.getWidth() );
                oos.writeDouble( r2d.getHeight() );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readObject( ObjectInputStream ois ) {
        try {
            ois.defaultReadObject();
            boolean shape = ois.available() > 0 && ois.readBoolean() ;
            if ( shape ) {
                double x = ois.readDouble() ;
                double y = ois.readDouble() ;
                double w = ois.readDouble() ;
                double h = ois.readDouble() ;
                area = new Rectangle2D.Double( x, y, w, h) ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected String regionName ;
    protected transient Shape area ;
}
