package org.cougaar.cpe.model;

/**
 * User: wpeng
 * Date: Apr 22, 2003
 * Time: 4:55:32 PM
 */
public class SustainmentPlanExecutor {

    private static SustainmentPlanExecutor executor;

    public static final SustainmentPlanExecutor getInstance() {
        if ( executor != null ) {
            return executor ;
        }
        executor = new SustainmentPlanExecutor() ;
        return executor ;
    }

    public void execute( SupplyVehicleEntity entity, WorldState state ) {
        Plan p = entity.getPlan( ) ;
        if ( p == null || p.isAfterLastTask() ) {
            return ;
        }

        SupplyTask task = (SupplyTask) p.getCurrentTask() ;

        // If I am at the beginning of the plan, find the first task.
        if ( task == null && p.hasNextTask() ) {
            task = (SupplyTask) p.nextTask() ;
        }
        else if ( task == null && !p.hasNextTask() ) {
            return ;
        }

        // Skip any complete tasks.
        while ( task != null && task.isComplete() ) {
            task = (SupplyTask) p.nextTask() ;
        }

        // No tasks visible, get out.
        if ( task == null ) {
            return ;
        }

        if ( task.getObservedResult() == null && ( state.getTime() >= task.getStartTime() || task.getType() == SupplyTask.ACTION_RECOVERY ) ) {
            SupplyResult sr = new SupplyResult( state.getTime() ) ;
            task.setObservedResult( sr );
            task.setDisposition( Task.ACTIVE );
        }

        if ( task.getType() == SupplyTask.ACTION_RECOVERY ) {
            // Recovery is not dependent on time

            // See if the supply entity has returned to base.
            if ( entity.getY() > state.getRecoveryLine() ) {
                double destY = entity.getY() -
                        entity.getMovementRate() * state.getDeltaT() ;
                if ( destY < state.getRecoveryLine() ) {
                    destY = state.getRecoveryLine() ;
                }
                double destX = entity.getX() ;
                state.moveSupplyUnit( entity, destX, destY );
            }

            // Mark as complete if I am done.
            if ( entity.getY() <= state.getRecoveryLine() ) {
                task.setComplete( true );
                task.setDisposition( Task.PAST );
            }
        }
        else if ( task.getType() == SupplyTask.SUPPLY_AMMO || task.getType() == SupplyTask.SUPPLY_FUEL )
        {
            // Start the task exactly on time.
            if ( state.getTime() >= task.getStartTime() ) {
                // Load supplies if I am at or below recovery line.
                if ( entity.getY() <= state.getRecoveryLine() ) {
                    if ( task.getType() == SupplyTask.SUPPLY_AMMO && entity.getAmmoQuantity() < task.getQuantity() ) {
                        entity.setAmmoQuantity( ( int ) task.getQuantity() );
                        entity.setFuelQuantity( 0 );
                    }
                    else if ( task.getType() == SupplyTask.SUPPLY_FUEL && entity.getFuelQuantity() < task.getQuantity()) {
                        entity.setFuelQuantity( task.getQuantity());
                        entity.setAmmoQuantity( 0 );
                    }

                }
                // Move back to recovery lines
                else if ( ( ( task.getType() == SupplyTask.SUPPLY_AMMO && entity.getAmmoQuantity() < SupplyVehicleEntity.MAX_UNITS ) ||
                     ( task.getType() == SupplyTask.SUPPLY_FUEL && entity.getFuelQuantity() < SupplyVehicleEntity.MAX_UNITS ) ) &&
                        entity.getY() > state.getRecoveryLine())
                {
                    // Move towards the recovery line to get new load.
                    double destY = entity.getY() - entity.getMovementRate() * state.getDeltaT() ;
                    if ( destY < state.getRecoveryLine() ) {
                        destY = state.getRecoveryLine() ;
                    }
                    double destX = entity.getX() ;
                    state.moveSupplyUnit( entity, destX, destY );
                }

                // Move the unit towards the x axis to make a delivery.
                if ( entity.getY() < 0 ) {
                    double destY = entity.getY() + entity.getMovementRate() * state.getDeltaT() ;
                    if ( destY > 0 ) {
                        destY = 0 ;
                    }
                    double destX = entity.getX() ;
                    state.moveSupplyUnit( entity, destX, destY );
                }

                // Now unload if we have reached the last cycle.
                if ( entity.getY() >= 0 && state.getTime() >= ( task.getEndTime() - state.getDeltaT() ) )
                {
                    Entity ent = state.getEntity( task.getDestination() ) ;
                    if ( ent != null && ent instanceof UnitEntity ) {
                        SupplyResult sr = (SupplyResult) task.getObservedResult () ;
                        if ( sr != null ) {
                            // This should never happen!
                            sr = new SupplyResult() ;
                        }
                        state.supply( ( UnitEntity ) ent, entity, task.getType(), task.getQuantity(), sr ) ;
                        task.setComplete( true );
                        task.setDisposition( Task.PAST );
                    }
                }
            }
            else {
                if ( entity.getY() > state.getRecoveryLine() ) {
                    if ( ( task.getType() == SupplyTask.SUPPLY_AMMO &&
                            entity.getAmmoQuantity() == 0 ) ||
                          ( task.getType() == SupplyTask.SUPPLY_FUEL &&
                            entity.getFuelQuantity() == 0 ) )
                    {
                        System.out.println("\n\nSUSTAINMENT PLAN EXECUTOR:: WARNING:: Need to recover " + entity +
                                " to execute task" + task );
                    }

                }
            }
        }
    }
}
