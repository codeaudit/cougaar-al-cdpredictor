/*
 * Directive.java
 *
 * Created on October 1, 2001, 4:03 PM
 */

package org.hydra.pdu;

/**
 *
 * @author  wpeng
 * @version 
 */
public class DirectivePDU extends org.hydra.pdu.PDU {

    public static final String LOGGING = "Logging" ;
    
    public static final String PREDICATE = "Predicate" ;
    
    /** Creates new Directive */
    public DirectivePDU( String tag, Object value ) {
        this.tag = tag ;
        this.value = value ;
    }

    String tag ;
    Object value ;
}
