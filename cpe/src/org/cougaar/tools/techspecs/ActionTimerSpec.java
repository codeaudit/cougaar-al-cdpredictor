package org.cougaar.tools.techspecs;

/**
 *
 * This applies only to StandardTimers.  StandardTimers are
 * responsible for inserting messages with the TimerId into the
 * message queue. They are started and stopped with the role itself (i.e.
 * when the role is activiated) or by actions in order to trigger transitions from
 * one state to the other.
 *
 * <p. In the single state role case, a periodic timer is started with the RoleState associated with it
 * and stops when another RoleState turns off the timer.
 */
public class ActionTimerSpec
{
    public ActionTimerSpec(boolean periodic, String timerId)
    {
        isPeriodic = periodic;
        this.timerId = timerId;
    }

    protected boolean isPeriodic ;
    protected long defaultPeriod ;
    protected long defaultDelay ;
    protected String timerId ;
}
