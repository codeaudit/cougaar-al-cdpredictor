/**
 * User: wpeng
 * Date: May 2, 2003
 * Time: 2:23:54 PM
 */
package org.cougaar.cpe.model;

import java.io.Serializable;

public abstract class TaskResult implements Serializable, Cloneable {

    public abstract Object clone() ;
}
