package org.cougaar.cpe.model;

import org.cougaar.cpe.splan.SPlanner;

import java.util.ArrayList;

/**
 * User: wpeng
 * Date: Apr 23, 2003
 * Time: 5:41:54 PM
 */
public class SupplyUnit extends Entity {
    public SupplyUnit(String uid, double x, double y, ArrayList customers, ArrayList supplyEntities) {
        super(uid, x, y);
        this.customers = customers;
        this.supplyEntities = supplyEntities;
    }

    public Object clone() {
        SupplyUnit result = new SupplyUnit( getId(), getX(), getY(),
                customers == null ? null : (ArrayList) customers.clone(),
                supplyEntities == null ? null :
                ( ArrayList ) supplyEntities.clone() ) ;
        return result ;
    }

    public void update(WorldState ws) {

        SPlanner splanner = new SPlanner() ;
        splanner.plan( ws, customers, supplyEntities ) ;
    }

    /**
     * A list of supply entities ids.
     */
    ArrayList supplyEntities = new ArrayList() ;

    /**
     * A list of customer ids.
     */
    ArrayList customers = new ArrayList() ;
}
