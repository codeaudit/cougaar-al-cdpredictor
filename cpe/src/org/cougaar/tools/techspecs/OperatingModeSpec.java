package org.cougaar.tools.techspecs;

import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.adaptivity.OperatingModeCondition;

import java.io.Serializable;

/**
 *
 */
public class OperatingModeSpec extends TechSpec
{
    public OperatingModeSpec(String name, TechSpec parent, int layer, OMCRangeList rangeList )
    {
        super(name, parent, TechSpec.TYPE_OPERATING_MODE, layer);
        this.rangeList = rangeList;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * Make an instance of an op mode conforming to this techspec.
     * @return
     */
    public OperatingMode makeOperatingMode() {
        return new OperatingModeCondition( getName(), getRangeList() ) ;
    }

    public OMCRangeList getRangeList()
    {
        return rangeList;
    }

    String description ;
    OMCRangeList rangeList ;
}
