package org.cougaar.tools.techspecs;

import org.cougaar.tools.techspecs.events.ActionEvent;

/**
 * Base class for events.  Events trigger actions, and are emitted by actions.
 */
public abstract class ActionEventSpec extends TechSpec
{
    public static final int EVENT_TIMER = 0 ;
    public static final int EVENT_INPUT_MESSAGE = 1 ;

    protected ActionEventSpec( String name, ActionSpec spec, int type )
    {
        super( name, spec, TechSpec.TYPE_ACTION_INPUT, spec.getLayer() ) ;
        this.type = type ;
    }

    public int getType()
    {
        return type;
    }

    public abstract boolean matches( ActionEvent e ) ;

    private int type ;
}
