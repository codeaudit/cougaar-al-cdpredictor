package org.cougaar.tools.techspecs.config;

import org.cougaar.tools.castellan.util.MultiHashSet;
import org.cougaar.tools.techspecs.RelationshipSpec;
import org.cougaar.tools.techspecs.RoleImplSpec;

import java.io.Serializable;

public class RelationshipInstance implements Serializable
{
    public RelationshipInstance(RelationshipSpec relationshipSpec)
    {
        this.relationshipSpec = relationshipSpec;
    }

    public RelationshipSpec getRelationshipSpec()
    {
        return relationshipSpec;
    }

    /**
     * Connect the PluginInstance (instantiated within an agent) with a particular role.
     *
     * @param spec
     * @param roleName
     */
    public void addRoleInstance( PluginInstanceSpec spec, String roleName ) {
        RelationshipSpec.RoleInfo info = relationshipSpec.getRoleInfo( roleName ) ;

        if ( roleName == null ) {
            throw new RuntimeException( "Role " + roleName + " does not exist in relationship spec " + relationshipSpec.getRelationshipName() ) ;
        }

        RoleImplSpec roleImplSpec = spec.getPluginSpec().getRole( roleName ) ;
        if ( roleImplSpec == null ) {
            System.out.println("Role " + roleName + " does not exist in Plugin " + spec.getPluginSpec().getName() +
                    " implemented by class " + spec.getPluginSpec().getClassName() );
        }

        Object[] o = roleNameToPluginInstance.getObjects( roleName ) ;
        if ( o.length >= 1 && info.getCardinality() == 1 ) {
            throw new IllegalArgumentException( "Role " + roleName + " has cardinality 1 in " + relationshipSpec.getRelationshipName() ) ;
        }

        for (int i = 0; i < o.length; i++)
        {
            Object object = o[i];
            if ( object == spec ) {
                return ;
            }
        }

        roleNameToPluginInstance.put( roleName,  spec ) ;
    }

    RelationshipSpec relationshipSpec ;

    /**
     * Map role names to plugin instances.  The plugin instances must implement the
     * approach roles.
     */
    MultiHashSet roleNameToPluginInstance = new MultiHashSet() ;
}
