package org.cougaar.cpe.model;

import java.io.Serializable;
import java.awt.*;

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

    protected String regionName ;
    protected Shape area ;
}
