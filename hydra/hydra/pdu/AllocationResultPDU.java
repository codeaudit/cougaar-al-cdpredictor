/*
  * <copyright>
  *  Copyright 2001 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).  
  *  
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS 
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR 
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF 
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT 
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT 
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL 
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS, 
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR 
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.  
  * 
  * </copyright>
  *
  * CHANGE RECORD
  *  7/25/01 Initial version  by Penn State/IAI
  */

/*
 * AllocationResultPDU.java
 *
 * Created on June 12, 2001, 5:04 PM
 */

package org.hydra.pdu;
import org.cougaar.domain.planning.ldm.plan.AspectValue ;

/**
 *  This PDU is sent when a Allocation (or other plan element) has a new AR.
 *
 *  Allocation Results are complex objects and hence handled seperately from the PlanElement PDU.  
 *  How much we wish to capture should probably be tunable.
 * 
 * @author  wpeng
 * @version 
 */
public class AllocationResultPDU extends EventPDU {
    public static final short UNKNOWN = -1 ;
    public static final short RECEIVED = 1 ;
    public static final short REPORTED = 2 ;
    public static final short OBSERVED = 3 ;
    public static final short ESTIMATED = 4 ;
    
    public static final String STRING_UNKNOWN = "UNKNOWN" ;
    public static final String STRING_RECEIVED = "RECEIVED" ;
    public static final String STRING_REPORTED = "REPORTED" ;
    public static final String STRING_OBSERVED = "OBSERVED" ;
    public static final String STRING_ESTIMATED = "ESTIMATED" ;

    /** Creates new AllocationResultPDU. 
     *  @param planElementUID The plan element (allocation, aggregation, expansion) associated with this
     *    ar.
     *  @parm arType  One of RECEIVED, REPORTED, or OBSERVED, ESTIMATED, or UNKNOWN.
     */
    public AllocationResultPDU(UIDPDU planElementUID, short arType, int action, long executionTime, long time ) {
        super( EventPDU.TYPE_ALLOCATION_RESULT, action, executionTime, time ) ;
        this.planElementUID = planElementUID ;
        this.arType = arType ;
    }
    
    public void setConfidence( float confidence ) {
        this.confidence = confidence ;   
    }
    
    public float getConfidence() {
        return confidence ;
    }
    
    public int getARType() {
        return arType ;    
    }
    
    public static String arTypeToString( short type ) {
        switch ( type ) {
            case ESTIMATED :
                return STRING_ESTIMATED ;
            case REPORTED :
                return STRING_REPORTED ;
            case RECEIVED :
                return STRING_RECEIVED ;
            case OBSERVED :
                return STRING_OBSERVED ;
            default :
                return STRING_UNKNOWN ;
        }
    }
    
    public void outputParamString( StringBuffer buf ) {
        super.outputParamString( buf ) ;
        buf.append( ",peID=" ).append( planElementUID ) ;
       buf.append( ",type=" ) ;
       buf.append( arTypeToString( arType ) ) ;
       buf.append( ",success=" ) ;
       if ( success == -1 ) {
           buf.append( '?' ) ;
       }
       else if ( success == 0 ) {
           buf.append( "false" ) ;
       }
       else
           buf.append( "true" ) ;
    }
    
    public UIDPDU getPlanElementUID() { return planElementUID ; }
    
    public void setSuccess( boolean isSuccessful ) {
        success = ( byte ) ( ( isSuccessful ) ? 1 : 0 ) ;   
    }
    
    public byte getSuccess() { return success ; }
        
    public void setAspectValues( AspectValue[] newAspects ) {
        // 8.2.1 compatible
        //aspects = AspectValue.clone( newAspects ) ;
        aspects = new AspectValue[ newAspects.length ] ;
        for (int i=0;i<newAspects.length;i++) {
           aspects[i] = ( AspectValue ) newAspects[i].clone() ;   
        }
    }
    
    public int getNumAspects() {
        return aspects.length ;
    }
    
    public AspectValue getAspectValues( int i ) {
        return aspects[i] ;
    }

    /** The plan element to which I am assigned.
     */
    UIDPDU planElementUID ;
    
    /** Received, reported, observed. */
    short arType = UNKNOWN ;
    
    AspectValue[] aspects ;
    
    /** Indicates the degree of confidence in this result, between [0,1] */
    float confidence = -1 ;
    
    byte success = -1 ;
    
    static final long serialVersionUID = -7300840866857520717L;    
}
