package org.cougaar.cpe.agents.messages;

import org.cougaar.cpe.model.UnitEntity;
import org.cougaar.cpe.model.Plan;
import org.cougaar.tools.techspecs.events.MessageEvent;

/**
 * User: wpeng
 * Date: Apr 14, 2003
 * Time: 12:40:37 PM
 */
public class ManueverPlanMessage extends MessageEvent {

    public ManueverPlanMessage( String entityName, org.cougaar.cpe.model.Plan plan ) {
        setValue( plan );
        this.entityName = entityName ;
    }

    public org.cougaar.cpe.model.Plan getPlan() {
        return (org.cougaar.cpe.model.Plan) getValue() ;
    }

    public String getEntityName() {
        return entityName;
    }

    protected String entityName ;
}
