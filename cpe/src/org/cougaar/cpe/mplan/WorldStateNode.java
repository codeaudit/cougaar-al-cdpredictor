package org.cougaar.cpe.mplan;

import com.axiom.pspace.search.GraphNode;
import com.axiom.pspace.search.DefaultGraphNode;
import org.cougaar.cpe.model.WorldStateModel;

/**
 *  A node representing the state of the world and the accumulated score.
 */
public class WorldStateNode extends DefaultGraphNode {

    public WorldStateNode( WorldStateNode parent, org.cougaar.cpe.model.WorldStateModel state) {
        super(parent) ;
        this.state = state;
        score = (float) state.getScore() ;
        id = count++ ;
    }

    public int getId()
    {
        return id;
    }

    public String toString() {
        long pid=-1 ;
        GraphNode parent = getParent() ;
        if ( parent != null ) {
            pid=((WorldStateNode) parent).id ;
        }
        return ( "[id=" + id + ",par=" + pid + ",d=" +
                depth + ",sc=" + score + ",fc="
                + fuelConsumption + ",ac=" + ammoConsumption + ",oc=" + overallCoverage + ",cc= " + criticalCoverage + "]" );
    }

    public float getZoneCoverage()
    {
        return zoneCoverage;
    }

    public void setZoneCoverage(float zoneCoverage)
    {
        this.zoneCoverage = zoneCoverage;
    }

    public double getOverallCoverage() {
        return overallCoverage;
    }

    public void setOverallCoverage(double overallCoverage) {
        this.overallCoverage = overallCoverage;
    }

    public double getCriticalCoverage() {
        return criticalCoverage;
    }

    public void setCriticalCoverage(double criticalCoverage) {
        this.criticalCoverage = criticalCoverage;
    }

    public double getTargetCoverage() {
        return targetCoverage;
    }

    public void setTargetCoverage(double targetCoverage) {
        this.targetCoverage = targetCoverage;
    }

    public boolean isIdentical(GraphNode n) {
        return false;
    }

    public org.cougaar.cpe.model.WorldStateModel getState() {
        return state;
    }

    public double getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public int getAmmoConsumption() {
        return ammoConsumption;
    }

    public void setAmmoConsumption(int ammoConsumption) {
        this.ammoConsumption = ammoConsumption;
    }

    public double getScore() {
        return score;
    }

    public void setScore( double score) {
        this.score = (float) score;
    }

    /**
     * The projected world state at this time.
     */
    org.cougaar.cpe.model.WorldStateModel state ;

    protected int id ;
    protected float score ;
    /**
     * You are given a zero if you are in the current zone.
     * You are given a -distance from the zone if you are not in the current zone.  A +1 indicates
     * the zone coverage has not been inited.
     */
    protected float zoneCoverage = 1 ;

    protected int ammoConsumption ;
    protected double fuelConsumption ;
    protected double overallCoverage = -1 ;
    protected double criticalCoverage = -1 ;
    protected double targetCoverage = -1 ;

    private static int count = 0 ;
}