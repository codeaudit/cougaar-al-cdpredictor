package org.cougaar.tools.techspecs;

import org.cougaar.tools.techspecs.events.ActionEvent;

/**
 * Specifies an input or output message triggering or emitted by an action.
 */
public class ActionMessageSpec extends ActionEventSpec
{
    public ActionMessageSpec(String name, ActionSpec spec, String eventClassName )
    {
        super(name, spec, ActionEventSpec.EVENT_INPUT_MESSAGE);
        this.inputClassName = eventClassName ;
        try
        {
            realClass = Class.forName( eventClassName ) ;
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public String getInputClassName()
    {
        return inputClassName;
    }

    public boolean matches(ActionEvent e)
    {
        if ( realClass != null ) {
            return realClass.isAssignableFrom( e.getClass() ) ;
        }
        return false ;
    }

    String inputClassName ;
    Class realClass ;
}
