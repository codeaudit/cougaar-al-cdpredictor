package org.cougaar.tools.techspecs;

import org.cougaar.planning.ldm.plan.RelationshipImpl;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This is made to describe the relationship between two
 * RoleSpec functions.  It is akin to the org.cougaar.planning.ldm.plan.Relationship
 * interface except that it allows for multiple roles to interact, e.g. a relationship
 * Also, the relationships are explicit in terms of functionality, e.g. the CPYCommander and
 * CPYSubordinate relationship is specific rather than assuming a generic Superior
 * Subordinate relationship as in Cougaar. This is because the Roles in the TechSpec form actually specify
 * interactions, messages, and the states and behavior of the individual components.
 *
 * <p> This is not derived from TechSpec because it is does not fall in the TechSpec containment
 * hierarchy.
 */
public class RelationshipSpec implements Serializable
{
    public static int CARDINALITY_N = -1 ;

    public static int CARDINALITY_UNKNOWN = 0 ;

    /**
     * Specifies a role participating within the relationship.
     */
    public static final class RoleInfo implements Serializable {

        public RoleInfo(int cardinality, String roleName, boolean required)
        {
            this.cardinality = cardinality;
            if ( cardinality < -1 ) {
                throw new RuntimeException( "Cardinality " + cardinality + " not valid.  Must be >= -1." ) ;
            }
            this.roleName = roleName;
            this.required = required;
        }


        public int getCardinality()
        {
            return cardinality;
        }

        public boolean isRequired()
        {
            return required;
        }

        public String getName()
        {
            return roleName;
        }

        /**
         * The name of the peer role.
         */
        private String roleName ;

        /**
         * Does this peer role need to be satisfied for this role to be valid?
         */
        private boolean required ;

        /**
         * The cardinality of the role within the
         */
        private int cardinality ;
    }

    public RelationshipSpec(String relationshipName)
    {
        this.relationshipName = relationshipName;
    }

    public String getRelationshipName()
    {
        return relationshipName;
    }

    public RoleInfo getRoleInfo( String role )
    {
        return (RoleInfo) roleInfo.get( role );
    }

    public void addRoleInfo( RoleInfo info ) {
        roleInfo.put( info.getName(), info ) ;
    }

    /**
     * The name of the relationship, e.g. "CPYC2Relationship"
     *
     */
    String relationshipName ;

    HashMap roleInfo = new HashMap();
}
