package org.cougaar.cpe.model;

/**
 * User: wpeng
 * Date: Apr 21, 2003
 * Time: 4:36:48 PM
 */
public class SupplyEntity extends Entity
{

    public SupplyEntity(String uid, double x, double y) {
        super(uid, x, y);
    }

    public Plan getSupplyPlan() {
        return supplyPlan;
    }

    public void setSupplyPlan(Plan supplyPlan) {
        this.supplyPlan = supplyPlan;
    }

    public double getCapacity() {
        return VGWorldConstants.MAX_SUPPLY_UNIT_LOAD - totalUnits ;
    }

    public int getAmmoQuantity() {
        return ammoQuantity;
    }

    public float getFuelQuantity() {
        return fuelQuantity;
    }

    public void setAmmoQuantity(int ammoQuantity) {
        this.ammoQuantity = ammoQuantity;
    }

    public void setFuelQuantity(float fuelQuantity) {
        this.fuelQuantity = fuelQuantity;
    }

    public void addAmmoQuantity( int quantity ) {
        int newAmmoQuantity = ammoQuantity + quantity ;
        float newTotalQuantity = fuelQuantity + newAmmoQuantity ;
        if ( newTotalQuantity > VGWorldConstants.MAX_SUPPLY_UNIT_LOAD ) {
            throw new RuntimeException( "Quantity " + quantity + " exceeds space capacity " + ( VGWorldConstants.MAX_SUPPLY_UNIT_LOAD - totalUnits ) ) ;
        }
        ammoQuantity = newAmmoQuantity ;
        totalUnits = newTotalQuantity ;
    }

    public void addFuelQuantity( float quantity ) {
        float newFuelQuantity = fuelQuantity + quantity ;
        float newTotalQuantity = ammoQuantity + newFuelQuantity ;
        if ( newTotalQuantity > VGWorldConstants.MAX_SUPPLY_UNIT_LOAD ) {
            throw new RuntimeException( "Quantity " + quantity + " exceeds space capacity " + ( VGWorldConstants.MAX_SUPPLY_UNIT_LOAD - totalUnits ) ) ;
        }
        fuelQuantity = newFuelQuantity ;
        totalUnits = newTotalQuantity ;
    }

    public void removeAmmoQuantity( int quantity ) {
        if ( quantity > ammoQuantity ) {
           throw new RuntimeException( "Quantity " + quantity + " does not exist, found ammoQuantity=" + ammoQuantity ) ;
        }
        ammoQuantity -= quantity ;
        totalUnits = ammoQuantity + fuelQuantity ;
    }

    public void removeFuelQuantity( double quantity ) {
        if ( quantity > fuelQuantity ) {
           throw new RuntimeException( "Quantity " + quantity + " does not exist, found fuelQuantity=" + fuelQuantity ) ;
        }
        ammoQuantity -= quantity ;
        totalUnits = ammoQuantity + fuelQuantity ;
    }

    public Object clone() {
        SupplyEntity result = new SupplyEntity( getId(), getX(), getY() ) ;
        result.totalUnits = totalUnits ;
        result.ammoQuantity = ammoQuantity ;
        result.fuelQuantity = fuelQuantity ;
        return result ;
    }

    public Plan getPlan() {
        return supplyPlan ;
    }


    public void update( WorldState ws ) {
    }

    float totalUnits ;
    int ammoQuantity ;
    float fuelQuantity ;

    Plan supplyPlan ;

}
