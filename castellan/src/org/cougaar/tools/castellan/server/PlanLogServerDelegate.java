package org.cougaar.tools.castellan.server;

import org.cougaar.core.plugin.ComponentPlugin;

/**
 * Silly  class created to delegate to.  Will be replaced by
 * plug-in when everything works.
 */
public class PlanLogServerDelegate
{
    public PlanLogServerDelegate(ComponentPlugin adapter)
    {
        this.adapter = adapter;
    }

    public void initSubscriptions() {
        // Suscribe to InMemoryEventLog objects
    }

    public void execute() {
    }

    ComponentPlugin adapter ;
}
