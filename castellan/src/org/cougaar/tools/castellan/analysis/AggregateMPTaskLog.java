/**
 * AggregateMPTaskLog.java
 *
 * Created on August 9, 2001, 3:48 PM
 */

package org.cougaar.tools.castellan.analysis;
import java.util.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class AggregateMPTaskLog extends AggregateVerbTaskLog {

    /** Creates new AggregateMPTaskLog with pattern "verbs."
     */
    public AggregateMPTaskLog( String verb, String cluster ) {
        super( verb, cluster ) ;
        // this.verbs = verbs ;
    }
    
    public static AggregateMPTaskLog makeFromLog( AggregateVerbTaskLog avtl ) {
        AggregateMPTaskLog  amtl = new AggregateMPTaskLog( avtl.getVerb(), avtl.getCluster() ) ;
        amtl.parents = (ArrayList) avtl.parents.clone() ;
        // Get all parents to replace the old avtl with me.
        amtl.instances = (HashMap) avtl.instances.clone() ;
        // Get all children to replace their parents with me
        amtl.children = (ArrayList) avtl.children.clone() ;
        return amtl ;
    }
    
    public String[] getVerbs() { return verbs ; }
    
    String[] verbs ;
}
