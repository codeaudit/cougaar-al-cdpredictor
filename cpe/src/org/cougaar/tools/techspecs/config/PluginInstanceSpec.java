package org.cougaar.tools.techspecs.config;

import org.cougaar.tools.techspecs.TechSpec;
import org.cougaar.tools.techspecs.PluginSpec;

import java.io.Serializable;

public class PluginInstanceSpec extends TechSpec {


    public PluginInstanceSpec(String name, AgentInstanceSpec parent, PluginSpec spec, RelationshipInstance instance) {
        super(name, parent, TechSpec.TYPE_PLUGIN, TechSpec.LAYER_APPLICATION );
        this.pluginSpec = spec ;
        this.instance = instance;
    }

    public RelationshipInstance getInstance() {
        return instance;
    }

    public void setInstance(RelationshipInstance instance) {
        this.instance = instance;
    }

    public PluginSpec getPluginSpec()
    {
        return pluginSpec;
    }

    PluginSpec pluginSpec ;
    RelationshipInstance instance ;
}
