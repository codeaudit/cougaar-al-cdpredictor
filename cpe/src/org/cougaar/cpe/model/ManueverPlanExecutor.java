package org.cougaar.cpe.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.awt.geom.Point2D;

/**
 * Executes the current plan based on the perceived world state.
 */
public class ManueverPlanExecutor {
    private static ManueverPlanExecutor executor;

    public static final ManueverPlanExecutor getInstance() {
        if ( executor != null ) {
            return executor ;
        }
        executor = new ManueverPlanExecutor() ;
        return executor ;
    }

    /**
     * Get the tracking error.  Where should I be (positionally) in the plan relative to
     * where I am now?
     *
     * @return A positive or negative value.
     */
    public double getPositionError( UnitEntity entity, WorldState state) {
        Plan actionPlan = entity.getManueverPlan() ;

        // No plan == no error.
        if ( actionPlan == null || actionPlan.isEmpty() ) {
            return 0 ;
        }

        // Find the current task based on time only
        UnitTask t = null ;
        for (int i=0;i<actionPlan.getNumTasks();i++) {
            t = (UnitTask) actionPlan.getTask(i) ;
            if ( t.getStartTime() >= state.getTime() && t.getEndTime() < state.getTime() ) {
                break ;
            }
        }

        // Compute the actual error.
        if ( t != null ) {
            if ( Math.abs( t.getDestination() - entity.getX() )
                 > ( t.getEndTime() - state.getTime() ) * VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE )
            {
                double dx = t.getDestination() - entity.getX() ;
                if ( dx < 0 ) {
                    return dx + VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE * ( t.getEndTime() - state.getTime() ) ;
                }
                else if ( dx > 0 ) {
                    return dx - VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE * ( t.getEndTime() - state.getTime() ) ;
                }
            }
            else {
                return 0 ;
            }
        }

        return 0 ;
    }

    public void execute( UnitEntity entity, WorldState state ) {
        Plan actionPlan = entity.getManueverPlan() ;

        if ( actionPlan == null || actionPlan.isEmpty() ) {
            return ;
        }

        if ( actionPlan.isBeforeFirstTask() ) {
            actionPlan.nextTask() ;
        }

        if ( actionPlan.getCurrentTask() != null ) {
            UnitTask t = (UnitTask) actionPlan.getCurrentTask() ;

            // Go to the next task if my time is up.  No allowances for slack time will be made.
            while ( t != null && state.getTime() >= t.getEndTime()  ) {
                ExecutionResult er = (ExecutionResult) t.getObservedResult() ;
                if ( er == null ) {
                    er = new ExecutionResult( entity.getX(), entity.getY(), entity.getX(), entity.getY() ) ;
                }
                else {
                    er.setEndPosition( entity.getX(), entity.getY() );
                }
                t.setDisposition( Task.PAST );

                // Move to the next task until we find a state within the bracket.
                t = (UnitTask) actionPlan.nextTask() ;
                if ( t != null ) {
                    t.setExecutionResult( new ExecutionResult( entity.getX(), entity.getY(), entity.getX(), entity.getY() ));
                }
            }

//            if ( t== null || t.getStartTime() > state.getTime() ) {
//                // System.out.println("Warning: Entity " + entity.getId() + " has no active task.");
//            }

            // Execute the current task.
            if ( t != null && t.getStartTime() <= state.getTime() ) {

                t.setDisposition( Task.ACTIVE );

                // Move the entity.
                // Only move along the x-axis the standard movement amount.
                if ( t.getMoveAction() == UnitTask.ACTION_MOVE ) {
                    if ( t.getObservedResult() == null ) {
                        t.setExecutionResult( new ExecutionResult( entity.getX(), entity.getY(), entity.getX(), entity.getY() ));
                    }
                    double distToTarget = t.getDestX() - entity.getX() ;
                    double maxDistanceMoved = VGWorldConstants.UNIT_NORMAL_MOVEMENT_RATE * state.getDeltaT() ;
                    double targetX = 0 ;
                    if ( distToTarget < -1E-5 ) {
                        targetX = entity.getX() - Math.min( -distToTarget, maxDistanceMoved ) ;
                    }
                    else if ( distToTarget > 1E-5 ) {
                        targetX = entity.getX() + Math.min( distToTarget, maxDistanceMoved ) ;
                    }

                    // Now, actually move and update the fuel consumption accordingly.
                    if ( Math.abs( distToTarget ) >= 1E-5 ) {
                        MoveResult result = state.moveUnit( entity, new Point2D.Double( targetX, entity.getY() )) ;
                        ExecutionResult er = (ExecutionResult) t.getObservedResult() ;

                        er.setFuelConsumption(
                                er.getFuelConsumption()
                                + result.getFuelConsumption() );
                        er.setEndPosition( result.getEndX(), result.getEndY() );
                    }
                }
                else if ( t.getMoveAction() == UnitTask.ACTION_TRACK_TARGET ) {
                    // Try and track the target here.
                }

                EngageByFireResult er = null ;
                Collection designatedTargets = t.getTargets() ;

                // Engage by fire on the target.
                if ( t.isEngageByFire() ) {

                    // Find the targets by priority, e.g. the lowest target
                    // automatically gets the highest priority.

                    // Engage by fire if I am within range to the target.
                    ArrayList targetsInRange = state.getTargetsInRange( entity ) ;

                    if ( targetsInRange != null ) {
                        ArrayList designatedTargetsInRange = new ArrayList() ;
                        for (Iterator iterator = designatedTargets.iterator(); iterator.hasNext();) {
                            String s = (String) iterator.next();
                            if ( targetsInRange.contains(s) ) {
                               designatedTargetsInRange.add(s) ;
                            }
                        }

                        if ( designatedTargetsInRange.size() > 0) {
                            // Actually engage by fire the nearest designated target.
                            EntityInfo info = findNearestTarget( designatedTargetsInRange,
                                    state ) ;
                            er = state.engageByFire( entity,
                                     info.getEntity().getId() );
                        }
                        else {
                            // Opportunistic fire at the nearest target
                           if ( entity.getAmmoQuantity() >
                                entity.getCriticalAmmoThreshold() &&
                                   targetsInRange.size() > 0 ) {
                               // Engage the nearest target.
                               EntityInfo lowestTargetInfo =
                                       findNearestTarget(targetsInRange, state);
                               if ( lowestTargetInfo != null ) {
                                    er = state.engageByFire( entity,
                                        lowestTargetInfo.getEntity().getId() ) ;
                               }
                           }

                        }
                    }
                }

                // Update the current execution result with fuel, ammo, attrition
                // etc.
                ExecutionResult cur = (ExecutionResult) t.getObservedResult() ;
                if ( er != null ) {
                    EngageByFireResult ter = cur.getResult( er.getTargetId() ) ;
                    ter.setAttritValue( ter.getAttritValue() + er.getAttritValue() );
                    ter.setDestroyed( er.isDestroyed() );
                    ter.setAmmoConsumed( ter.getAmmoConsumed() + er.getAmmoConsumed() );
                }
                cur.setEndPosition( entity.getX(), entity.getY());
            }
        }
    }

    private EntityInfo findNearestTarget(ArrayList targets, WorldState state) {
        double lowestTarget = Double.MAX_VALUE ;
        EntityInfo lowestTargetInfo = null ;
        for (int i = 0; i < targets.size(); i++) {
            String s = (String)targets.get(i);
            EntityInfo einfo = state.getEntityInfo( s ) ;
            if ( einfo.getEntity().getY() < lowestTarget ) {
                lowestTarget =  einfo.getEntity().getY() ;
                lowestTargetInfo = einfo ;
            }
        }
        return lowestTargetInfo;
    }

    // UnitEntity entity ;
}
