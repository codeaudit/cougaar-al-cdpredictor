package org.cougaar.cpe.model;

import org.cougaar.cpe.planning.zplan.ZoneTask;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.io.Serializable;

/**
 * A very simple serial plan representation.  The tasks must be added in sorted orde
 * and must be non-overlapping.
 */
public class Plan implements Serializable {
    public Plan() {
        this.tasks = new ArrayList() ;
    }

    private Plan( int value ) {
        this.tasks = new ArrayList(0) ;
    }

    public Plan(ArrayList newActions) {
        this.tasks = new ArrayList( newActions.size() ) ;
        // Clone all the tasks.
        Task prevAction = null ;
        for (int i = 0; i < newActions.size(); i++) {
            Task task = (Task)newActions.get(i);
            if ( prevAction != null ) {
                if ( task.getStartTime() < prevAction.getEndTime() ) {
                    throw new IllegalArgumentException( "Tasks must be non-overlapping and ordered from past to future." ) ;
                }
            }
            Task clonedTask ;
            this.tasks.add( clonedTask = (Task) task.clone() ) ;
            prevAction = clonedTask ;
        }
    }

    public Task getTaskForTime(long time)
    {
        for (int i = 0; i < tasks.size(); i++)
        {
            Task t = (Task) tasks.get(i);
            if (t.getStartTime() <= time && t.getEndTime() >= time)
            {
                return t;
            }
        }
        return null;
    }

    public Task getNearestTaskForTime( long time ) {

        Task t = getTaskForTime( time ) ;
        if ( t != null ) {
            return t ;
        }

        if ( getNumTasks() > 0 ) {
            if ( time > getTask( getNumTasks() - 1).getStartTime() ) {
                t = getTask( getNumTasks() - 1 ) ;
            }
            else if ( time < getTask(0).getStartTime() ) {
                t =  getTask( 0 ) ;
            }
            else {
                if ( tasks.size() >= 2 ) {
                    for (int i = 0; i < tasks.size()-1; i++) {
                        Task task = (Task)tasks.get(i);
                        Task nextTask = (Task) tasks.get(i+1) ;
                        if ( time > task.getEndTime() && time < nextTask.getStartTime() ) {
                            if ( task.getEndTime() - time > nextTask.getStartTime() - time ) {
                                return task ;
                            }
                            else {
                                return nextTask ;
                            }
                        }
                    }
                }
            }
        }

        return t ;
    }

    public Plan( Task t ) {
        tasks = new ArrayList( 1 ) ;
        tasks.add(t) ;
    }

    public Object clone() {
        // Create by cloning all the actions.
        Plan result = new Plan() ;
        result.tasks = new ArrayList() ;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = (Task)tasks.get(i);
            result.tasks.add( task.clone() ) ;
        }
        result.setCurrentActionIndex( getCurrentActionIndex() ) ;
        return result ;
    }

    public void setCurrentActionIndex( int index ) {
        if ( index > getNumTasks() ) {
            throw new NoSuchElementException( "Indexed task " + index + " does not exist, max index=" + ( getNumTasks() ) ) ;
        }
        currentActionIndex = index ;
    }

    public String toString() {
        StringBuffer result = new StringBuffer() ;
        result.append( "[Plan ");
        for (int i = 0; i < tasks.size(); i++) {
            Task task = (Task)tasks.get(i);
            result.append( task ) ;
            if ( i < tasks.size() - 1 ) {
                result.append( "," ) ;
            }
        }
        result.append( "]") ;
        return result.toString() ;
    }

    public int getCurrentActionIndex() {
        return currentActionIndex ;
    }

    public Task getCurrentTask() {
        if ( currentActionIndex == -1 ) {
            return null ;
        }
        if ( tasks.size() == 0 || currentActionIndex >= tasks.size() ) {
            return null ;
        }
        return (Task) tasks.get(currentActionIndex) ;
    }

    public boolean hasNextTask() {
        return currentActionIndex < ( tasks.size() - 1 ) ;
    }

    public boolean isEmpty() {
        return tasks.size() == 0 ;
    }

    public boolean isBeforeFirstTask() {
        return currentActionIndex == -1 && tasks.size() > 0 ;
    }

    public boolean isAfterLastTask() {
        return currentActionIndex >= tasks.size() && tasks.size() > 0 ;
    }

    /**
     * Mark this task as complete and move to the next action.
     */
    public Task nextTask() {
        currentActionIndex++ ;
        if ( currentActionIndex >= getNumTasks() ) {
            return null ;
        }
        else {
            return (Task) tasks.get( currentActionIndex ) ;
        }
    }

    public int getNumTasks() {
        return tasks.size() ;
    }

    public Task getTask(int i) {
       return (Task) tasks.get(i) ;
    }

    /**
     * Removes completed tasks < the currentIndex.
     */
    public void clearCompletedTasks() {
        if ( currentActionIndex > 0 ) {
            ArrayList newTasks = new ArrayList() ;
            for (int i=currentActionIndex;i<tasks.size();i++) {
                newTasks.add( tasks.get(i) );
            }
            tasks = newTasks ;
            currentActionIndex = 0 ;
        }
    }

    /**
     * Overlays a set of Tasks up to and including startIndex.
     * @param startIndex
     * @param newTasks New actions to be introduced at and including startIndex.
     */
    public void replan( int startIndex, ArrayList newTasks ) {
        ArrayList replannedActions = new ArrayList() ;
        long lastTime = 0 ;
        for (int i=0;i<startIndex;i++) {
            Task t = (Task) tasks.get(i) ;
            replannedActions.add( t  ) ;
            lastTime = Math.max( t.getEndTime(), lastTime ) ;
        }

        // Do some validation here.
        for (int i = 0; i < newTasks.size(); i++) {
            Task nt = (Task) ( ( Task ) newTasks.get(i) ).clone() ;
            if ( nt.getStartTime() < lastTime ) {
                throw new IllegalArgumentException( "Task " + nt + " has start time before " + replannedActions ) ;
            }
            replannedActions.add( nt );
        }
        tasks = replannedActions ;
    }

    public void replan( int startIndex, Plan newTasks ) {
        ArrayList replannedActions = new ArrayList() ;
        startIndex = Math.min( startIndex, tasks.size() ) ;
        long lastTime = 0 ;
        for (int i=0;i<startIndex;i++) {
            Task t = (Task) tasks.get(i) ;
            replannedActions.add( t ) ;
            lastTime = Math.max( t.getEndTime(), lastTime ) ;
        }

        for (int i = 0; i < newTasks.getNumTasks(); i++) {
            Task nt = (Task) newTasks.getTask(i).clone() ;
            if ( nt.getStartTime() < lastTime ) {
                throw new IllegalArgumentException( "New replanned task " + nt + " has start time before " + replannedActions ) ;
            }
            replannedActions.add( nt );
        }
        tasks = replannedActions ;
    }

    protected int currentActionIndex = -1;
    protected ArrayList tasks ;

    private static Plan nullPlan = new Plan( 0 ) ;

    public static Plan getNullPlan() {
        return nullPlan;
    }
}