package org.cougaar.cpe.model;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.castellan.util.MultiHashSet;
import org.cougaar.cpe.planning.zplan.ZoneWorld;

import java.util.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * User: wpeng
 * Date: Apr 20, 2003
 * Time: 3:17:43 PM
 */
public class WorldStateUtils {
    public static final int COVERAGE_ALL = 0 ;
    public static final int COVERAGE_IN_RANGE = 1 ;
    public static final int COVERAGE_CRITICAL = 2 ;
    /**
     * Optimal coverage is 1-1 for each target. This is a search parameter and hence is
     * adjustable.
     */
    public static final double MAX_COVERAGE = 2 ;

    public static double computeCoverage( WorldState ws ) {
        return computeCoverage( ws, COVERAGE_ALL ) ;
    }

    public static double computeInRangeCoverage( WorldState ws ) {
        return computeCoverage( ws, COVERAGE_IN_RANGE ) ;
    }

    public static double computeCriticalCoverage( WorldState ws ) {
        return computeCoverage( ws, COVERAGE_CRITICAL ) ;
    }

    /**
     * Compute a weighted coverage in favor of targets which are lower. Targets
     * which are out of range are not counted.
     *
     * @param ws
     * @return
     */
    public static double computeWeightedCoverage( WorldState ws ) {
        // Try and maintain uniformly weighted coverage.
        MultiHashSet coveredUnits = new MultiHashSet() ;
        HashMap units = new HashMap() ;

        for ( Iterator iter = ws.getUnits(); iter.hasNext(); ) {
            UnitEntity ue = (UnitEntity) iter.next() ;
            ArrayList list = null ;

            list = ws.getTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;

            if ( list != null ) {
                for (int i = 0; i < list.size(); i++) {
                    String targetEntity = ( String )list.get(i);
                    coveredUnits.put( targetEntity, ue.getId() ) ;
                }
                units.put( ue.getId(), list ) ;
            }
        }

        // For each target, compute how many units are targetting it.
        Iterator iter = coveredUnits.keys() ;

        // Each unit needs to be covered by a maximum of a single unit.
        double totalCoverage = 0 ;

        while (iter.hasNext()) {
            double coverage = 0 ;
            String entity = (String) iter.next();
            Object[] os = coveredUnits.getObjects( entity ) ;
            for (int i = 0; i < os.length; i++) {
                String unitId = (String) os[i];
                ArrayList tlist = (ArrayList) units.get(unitId) ;
                if ( tlist != null ) { // Fractional contribution to coverage.
                    TargetEntity tentity = (TargetEntity) ws.getEntity( unitId ) ;
                    // Weight in favor of low hanging fruit.
                    coverage += 1 / ( tentity.getY() + 1 ) ;
                }
            }
            totalCoverage += Math.min( MAX_COVERAGE, coverage ) ;
        }
        return totalCoverage ;
    }

    public interface WeightingFunction {
        public float computeWeight( WorldState ws, TargetEntity te, UnitEntity ue ) ;
    }

    public static class UnweightedFunction implements WeightingFunction {

        public float computeWeight(WorldState ws, TargetEntity te, UnitEntity ue)
        {
            return 1 ;
        }
    }

    public static class InverseFunction implements WeightingFunction {
        public float computeWeight(WorldState ws, TargetEntity te, UnitEntity ue)
        {
            return ( 1/ ( te.getY() + 1 ) ) ;
        }
    }

    /**
     * Weights all targets > limit to zero.  Otherwise, it weights targets < penalty
     * height to one and targets greater than the penalty height based on
     * 1 / ( height + 1 ),
     */
    public static class LimitedInverseFunction implements WeightingFunction {

        public float computeWeight(WorldState ws, TargetEntity te, UnitEntity ue)
        {
            if ( te.getY() < ws.getPenaltyHeight() ) {
                return 1 ;
            }
            if ( te.getY() < limit ) {
                return ( float ) ( 1/ ( ( te.getY() - ws.getPenaltyHeight() ) / 3 + 1 ) ) ;
            }
            return 0 ;
        }

        public float getLimit()
        {
            return limit;
        }

        public void setLimit(float limit)
        {
            this.limit = limit;
        }

        protected float limit = 5 ;
    }

    public static final WeightingFunction CONSTANT_FUNCTION = new UnweightedFunction() ;
    public static final WeightingFunction INVERSE_FUNCTION = new InverseFunction() ;
    public static final WeightingFunction LIMITED_INVERSE_FUNCTION = new LimitedInverseFunction() ;

    private static InRangeTargetsPredicate IN_RANGE_TARGETS_PREDICATE = new InRangeTargetsPredicate() ;

    private static class InRangeTargetsPredicate implements UnaryPredicate {
        public boolean execute(Object o)
        {
            TargetEntity entity = (TargetEntity) o ;

            return entity.getY() < criticalHeight ;
        }

        public double getCriticalHeight()
        {
            return criticalHeight;
        }

        public void setRangeHeight(double criticalHeight)
        {
            this.criticalHeight = criticalHeight;
        }

        double criticalHeight = 0 ;
    } ;

    private static ArrayList targets = new ArrayList() ;

    private static Comparator leftToRightComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            Entity e1 = (Entity) o1, e2 = (Entity) o2 ;
            double order = e1.getX() - e2.getX() ;
            if ( order < 0 ) {
                return -1 ;
            }
            else if ( order > 0 ) {
                return 1 ;
            }
            return 0 ;
        }
    } ;

    public static float computeZoneCoverage( WorldState ws, ArrayList units, Interval zone ) {
        float coverage = 0 ;
        for (int i=0;i<units.size();i++) {
            UnitEntity ue = (UnitEntity) ws.getEntity( ( String ) units.get(i) ) ;
            if ( ue.getX() < zone.getXLower() ) {
                coverage -= zone.getXLower() - ue.getX() ;
            }
            else if (ue.getX() > zone.getXUpper() ) {
                coverage -= ue.getX() - zone.getXUpper() ;
            }
        }
        return coverage ;
    }

    /**
     * Compute coverage within an interval.
     *
     * @param ws
     * @param type
     * @param function
     * @param maxCoverage
     * @param interval
     * @return
     */
    public static double computeCoverage( WorldState ws, ArrayList unitList, int type,
                                          WeightingFunction function, double maxCoverage, Interval interval )
    {
        // Try and maintain uniformly weighted coverage.
        MultiHashSet coveredTargetsToUnitsMap = new MultiHashSet() ;
        HashMap unitToTargetMap = new HashMap() ;
        Rectangle2D.Float r2d = null ;
        if ( interval != null ) {
            r2d = new Rectangle2D.Float(interval.getXLower(), 0, interval.getXUpper() - interval.getXLower(), (float) ws.getBoardHeight() ) ;
        }

        // Look only at my units.
        for ( Iterator iter = unitList.iterator(); iter.hasNext(); ) {
            UnitEntity ue = (UnitEntity) ws.getEntity( (String) iter.next() );
            ArrayList coveredTargets = null ;

            switch ( type ) {
                case COVERAGE_ALL :
                    coveredTargets = ws.getTargetsCoveredInArea( ue.getX(), ue.getY(), ue.getRangeShape(), r2d ) ;
                    break ;
                case COVERAGE_IN_RANGE :
                    coveredTargets = ws.getTargetsInRange( ue.getX(), ue.getY(), ue.getRangeShape(), r2d ) ;
                    break ;
                case COVERAGE_CRITICAL :
                    // Covering critical targets outside the zone is still rewarded
                    coveredTargets = ws.getCriticalTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                default :
                    throw new IllegalArgumentException( "Type " + type + " not understood." ) ;
            }

            if ( coveredTargets != null ) {
                for (int i = 0; i < coveredTargets.size(); i++) {
                    String targetEntity = ( String ) coveredTargets.get(i);
                    coveredTargetsToUnitsMap.put( targetEntity, ue.getId() ) ;
                }
                unitToTargetMap.put( ue.getId(), coveredTargets ) ;
            }
        }

        // For each target, compute how many units are targetting it.
        Iterator iter = coveredTargetsToUnitsMap.keys() ;

        double totalCoverage = 0 ;

        while (iter.hasNext()) {
            String targetId = (String) iter.next();
            double localCoverage = 0 ;
            // These are the units which are covering this target.
            Object[] os = coveredTargetsToUnitsMap.getObjects( targetId ) ;
            if ( os != null && os.length > 0 ) {
                TargetEntity te = (TargetEntity) ws.getEntity( targetId ) ;
                localCoverage += function.computeWeight( ws, te, null ) ;
            }

            totalCoverage += Math.min( localCoverage, maxCoverage ) ;
        }

        double partialCoverage = computePartialCoverage( ws, coveredTargetsToUnitsMap, type, maxCoverage ) ;

        return totalCoverage + partialCoverage ;

    }

    /**
     * Compute coverage with a weighting function.  Also, include a partial coverage.
     *
     * @param ws
     * @param type
     * @param function
     * @param maxCoverage  This ought to be 1.4, e.g. the weighted coverage of a single target is at most 1.
     * @return
     */
    public static synchronized double computeCoverage( WorldState ws, int type,
                                                       WeightingFunction function, double maxCoverage ) {
        // Try and maintain uniformly weighted coverage.
        MultiHashSet coveredTargetsToUnitsMap = new MultiHashSet() ;
        HashMap units = new HashMap() ;

        for ( Iterator iter = ws.getUnits(); iter.hasNext(); ) {
            UnitEntity ue = (UnitEntity) iter.next() ;
            ArrayList coveredTargets = null ;

            switch ( type ) {
                case COVERAGE_ALL :
                    coveredTargets = ws.getTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                case COVERAGE_IN_RANGE :
                    coveredTargets = ws.getTargetsInRange( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                case COVERAGE_CRITICAL :
                    coveredTargets = ws.getCriticalTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                default :
                    throw new IllegalArgumentException( "Type " + type + " not understood." ) ;
            }

            if ( coveredTargets != null ) {
                for (int i = 0; i < coveredTargets.size(); i++) {
                    String targetEntity = ( String ) coveredTargets.get(i);
                    coveredTargetsToUnitsMap.put( targetEntity, ue.getId() ) ;
                }
                units.put( ue.getId(), coveredTargets ) ;
            }
        }

        // For each target, compute how many units are targetting it.
        Iterator iter = coveredTargetsToUnitsMap.keys() ;

        double totalCoverage = 0 ;

        while (iter.hasNext()) {
            String targetId = (String) iter.next();
            double localCoverage = 0 ;
            // These are the units which are covering this target.
            Object[] os = coveredTargetsToUnitsMap.getObjects( targetId ) ;
            if ( os != null && os.length > 0 ) {
                TargetEntity te = (TargetEntity) ws.getEntity( targetId ) ;
                localCoverage += function.computeWeight( ws, te, null ) ;
            }

            totalCoverage += Math.min( localCoverage, maxCoverage ) ;
        }

        double partialCoverage = computePartialCoverage( ws, coveredTargetsToUnitsMap, type, maxCoverage ) ;

        return totalCoverage + partialCoverage ;
    }

    public static double computePartialCoverage( WorldState ws,
                                                 MultiHashSet coveredTargetsToUnitsMap,
                                                 int type, double maxCoverage ) {
        // Compute a list of _uncovered_ targets which are critical (i.e. below
        // the penalty height.)
        targets.clear();
        switch ( type ) {
            case COVERAGE_CRITICAL :
                IN_RANGE_TARGETS_PREDICATE.setRangeHeight( ws.getPenaltyHeight() );
                break ;
            case COVERAGE_IN_RANGE :
                IN_RANGE_TARGETS_PREDICATE.setRangeHeight( VGWorldConstants.getUnitRangeHeight() ) ;
                break ;
            case COVERAGE_ALL :
                IN_RANGE_TARGETS_PREDICATE.setRangeHeight( Double.MAX_VALUE );
                break ;
            default :
                throw new IllegalArgumentException( "Type " + type + " not recognized." ) ;
        }

        ws.getTargets( targets, IN_RANGE_TARGETS_PREDICATE );
        Iterator iter = targets.iterator() ;
        while (iter.hasNext())
        {
            TargetEntity targetEntity = (TargetEntity) iter.next();
            Object[] coveringUnits = coveredTargetsToUnitsMap.getObjects( targetEntity.getId() ) ;
            if ( coveringUnits != null && coveringUnits.length > 0 ) {
                iter.remove();
            }
        }

        // Collections.sort( targets, leftToRightComparator );

        // We now have a list of uncovered critical targets, sorted from left to right.
        // Each has an influence function based on a Gaussian with variance == 1/2 of the range width,
        // Compute the partial coverage associated with these targets.

        iter = ws.getUnits() ;
        double partialCoveragePenalty = 0 ;
        while (iter.hasNext())
        {
            UnitEntity unitEntity = (UnitEntity) iter.next();
            double localCoverage = 0 ;
            for (int i = 0; i < targets.size(); i++) {
                TargetEntity targetEntity = (TargetEntity)targets.get(i);
                float dist = (float) (targetEntity.getX() - unitEntity.getX()) ;
                localCoverage += Math.exp( - ( dist * dist )
                        / VGWorldConstants.getUnitRangeWidth() ) ;
            }
            partialCoveragePenalty = Math.min( localCoverage, maxCoverage ) ;
        }

        return partialCoveragePenalty ;
    }

    /**
     * Compute overall coverage index, weighted by the height of the units (e.g.
     * whether they are below or above the penalty line.
     * @param ws
     * @return
     */
    public static double computeCoverage( WorldState ws, int type ) {
        // Try and maintain uniformly weighted coverage.
        MultiHashSet coveredUnits = new MultiHashSet() ;
        HashMap units = new HashMap() ;

        for ( Iterator iter = ws.getUnits(); iter.hasNext(); ) {
            UnitEntity ue = (UnitEntity) iter.next() ;
            ArrayList list = null ;

            switch ( type ) {
                case COVERAGE_ALL :
                    list = ws.getTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                case COVERAGE_IN_RANGE :
                    list = ws.getTargetsInRange( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                case COVERAGE_CRITICAL :
                    list = ws.getCriticalTargetsCovered( ue.getX(), ue.getY(), ue.getRangeShape() ) ;
                    break ;
                default :
                    throw new IllegalArgumentException( "Type " + type + " not understood." ) ;
            }

            if ( list != null ) {
                for (int i = 0; i < list.size(); i++) {
                    String targetEntity = ( String )list.get(i);
                    coveredUnits.put( targetEntity, ue.getId() ) ;
                }
                units.put( ue.getId(), list ) ;
            }
        }

        // For each target, compute how many units are targetting it.
        Iterator iter = coveredUnits.keys() ;

        // Each unit needs to be covered by a maximum of a single unit.
        double totalCoverage = 0 ;

        while (iter.hasNext()) {
            double coverage = 0 ;
            String entity = (String) iter.next();
            Object[] os = coveredUnits.getObjects( entity ) ;
            for (int i = 0; i < os.length; i++) {
                String unitId = (String) os[i];
                ArrayList tlist = (ArrayList) units.get(unitId) ;
                if ( tlist != null ) { // Fractional contribution to coverage.
                    coverage += 1 ;
                }
            }
            totalCoverage += Math.min( MAX_COVERAGE, coverage ) ;
        }

//        Iterator iter = coveredUnits.values().iterator() ;
//        while (iter.hasNext()) {
//            String targetId = (String) iter.next();
//            Entity entity = wsm.getInfo( targetId ).getEntity() ;
//
//        }
        return totalCoverage ;
    }

    /**
     * Merge the current world state and the new perceived world state together by updating targets.
     *
     * @param current The current world state.
     * @param newModel The perceived world state to be merged.
     */
    public static void mergeSensorValues( WorldStateModel current, WorldStateModel newModel )
    {
        Iterator newTargets = newModel.getTargets() ;
        while (newTargets.hasNext())
        {
            TargetEntity newSensedTarget = (TargetEntity) newTargets.next();
            TargetContact currentContact = (TargetContact) current.getEntity( newSensedTarget.getId() );
            if ( currentContact != null ) {
                if ( newSensedTarget instanceof TargetContact ) {
                    TargetContact newContact = (TargetContact) newSensedTarget ;

                    // Only merge contacts with less error and a difference of 10 secs.
                    if ( newContact.getXError() < currentContact.getXError()
                            && ( newContact.getTimeStamp() - currentContact.getTimeStamp() ) >= - 10000 )
                    {
                        currentContact.setPosition( newContact.getX(), newContact.getY() );
                        currentContact.setError( newContact.getXError(), newContact.getYError() );
                    }
                }
                else {
                    currentContact.setPosition( newSensedTarget.getX(), newSensedTarget.getY() );
                    currentContact.setError( 0, 0 );
                }
            }
            else {
                currentContact = (TargetContact) newSensedTarget.clone() ;
                current.addEntity( currentContact );
            }
        }
    }

    public static void mergeWorldStateWithReference( WorldStateModel current, WorldStateModel reference ) {
        // Copy the newTargets.

        Iterator newTargets = reference.getTargets() ;
        while (newTargets.hasNext()) {
            TargetContact targetContact = (TargetContact) newTargets.next();
            //System.out.println("Reference zone world=" + referenceZoneWorld );
            TargetContact currentContact = (TargetContact) current.getEntity( targetContact.getId() ) ;
            if ( currentContact == null ) {
                current.addEntity( ( Entity ) targetContact.clone() );
            }
            else {
                currentContact.setPosition( targetContact.getX(), targetContact.getY() );
                currentContact.setStrength( targetContact.getStrength() );
                currentContact.setSuppressed( targetContact.isSuppressed(), reference.getTime() );
            }
        }

        // Any targets which are not sensed by the reference are removed if the timestamp is removed.
        ArrayList targetsToBeRemoved = new ArrayList();
        Iterator oldTargets = current.getTargets() ;
        while (oldTargets.hasNext()) {
            TargetContact targetContact = (TargetContact) oldTargets.next();
            TargetContact newTargetcontact = (TargetContact) reference.getEntity( targetContact.getId() ) ;
            if ( newTargetcontact == null ) {
               targetsToBeRemoved.add( targetContact.getId() ) ;
            }
        }

        // Remove the non-visible targets
        for (int i=0;i<targetsToBeRemoved.size();i++) {
            String id = (String) targetsToBeRemoved.get(i) ;
            current.deleteTarget( id ) ;
        }

    }


    /**
     * The maximum distance between zones is calculated conservatively here as the maximum distance.
     *
     * @param zw
     * @param z1
     * @param z2
     * @return
     */
    public static float calculateMaxDistance( ZoneWorld zw, IndexedZone z1, IndexedZone z2 ) {
        float zl1 = zw.getZoneLower( z1 ), zu1 = zw.getZoneUpper( z1 ) ;
        float zl2 = zw.getZoneLower( z2 ), zu2 = zw.getZoneUpper( z2 ) ;

        return Math.max( Math.abs( zl1 - zl2 ), Math.abs( zu1 - zu2 ) ) ;
    }

    public static float calculateMaxDistance(Interval z1, Interval z2 ) {
        float zl1 = z1.getXLower(), zu1 = z1.getXUpper() ;
        float zl2 = z2.getXLower(), zu2 = z2.getXUpper() ;

        return Math.max( Math.abs( zl1 - zl2 ), Math.abs( zu1 - zu2 ) ) ;

    }
}
