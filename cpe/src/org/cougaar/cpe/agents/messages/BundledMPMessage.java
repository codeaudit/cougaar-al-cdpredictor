package org.cougaar.cpe.agents.messages;

import org.cougaar.tools.techspecs.qos.MeasurementPoint;
import org.cougaar.tools.techspecs.events.MessageEvent;

import java.util.ArrayList;

/**
 * User: wpeng
 * Date: Sep 16, 2003
 * Time: 11:23:21 PM
 */
public class BundledMPMessage extends MessageEvent
{

    public ArrayList getMeasurementPointData()
    {
        return mpData;
    }

    public void addData( MeasurementPoint mp ) {
        mpData.add( mp ) ;
    }

    public void addData( String fileName, byte[] data ) {
        mpData.add( new Object[] { fileName, data } ) ;
    }

    ArrayList mpData = new ArrayList() ;
}
