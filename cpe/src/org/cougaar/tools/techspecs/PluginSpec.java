package org.cougaar.tools.techspecs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This characterizes a class of PlugIn.
 */
public class PluginSpec extends TechSpec
{
    /**
     *
     * The parent of a plugin is always null since a PluginSpec is not contained.
     *
     * @param plugInName The name of the plugin.
     * @param className The class which implements the plugin.
     * @param roleImplSpecs A set of roles implemented by the plugin.
     */
    public PluginSpec(String plugInName, String className, ArrayList roleImplSpecs )
    {
        super(plugInName, null, TechSpec.TYPE_PLUGIN, TechSpec.LAYER_APPLICATION );
        this.className = className ;
        for (int i = 0; i < roleImplSpecs.size(); i++) {
            RoleImplSpec roleImplSpec = (RoleImplSpec)roleImplSpecs.get(i);
            roleNameToRoleImplSpec.put( roleImplSpec.getName(), roleImplSpec ) ;
        }
    }

    public String getClassName()
    {
        return className;
    }

    public int getNumOpModes() {
        return opmodes.size() ;
    }

    public RoleImplSpec getRole( String rolename ) {
        return (RoleImplSpec) roleNameToRoleImplSpec.get( rolename ) ;
    }

    public void addRole( RoleImplSpec rs ) {
        roles.add( rs ) ;
    }

    public RoleImplSpec getRole( int i ) {
        return (RoleImplSpec) roles.get(i) ;
    }

    public int getNumRoles() {
        return roles.size() ;
    }

    String className ;

    /**
     * A list of RoleImplSpec objects.
     */
    ArrayList roles = new ArrayList() ;
    HashMap roleNameToRoleImplSpec = new HashMap() ;

    /**
     * Operating modes. Generated as perating mode conditions on the BB.
     */
    ArrayList opmodes = new ArrayList() ;

//    /**
//     *  This is a set of measurements associated with this plugin.  Measurement code
//     *  is generated automatically and inserted associated with actions.
//     */
//    ArrayList measurements = new ArrayList() ;
}
