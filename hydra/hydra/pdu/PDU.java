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
  *  6/12/01 Initial version  by IAI
  */

package org.hydra.pdu;
import java.util.* ;
import java.text.* ;

/**
 * Base class for all (non-nested) PDUs.  Enforces that they
 * are serializable.
 * @author  wpeng
 * @version 
 */
public class PDU implements java.io.Serializable {
    /** Destination/source identifier for Castellan server.
     */
    public static final String SERVER = "SERVER" ;
    public static final String UNKNOWN = "UNKNOWN" ;
    
    /** Creates new PDU */
    public PDU() {
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer() ;
        toString( sb ) ;
        return sb.toString() ;
    }

    public void toString( StringBuffer buf ) {
        buf.append( '[' ) ;
        String s = getClass().getName() ;
        buf.append( s.substring( s.lastIndexOf( '.' ) + 1, s.length() - 3 ) ) ;
        outputParamString( buf ) ;
        if ( src != null ) {
        buf.append( ",src=" + src ) ;
        }
        buf.append( ']' );
        // return buf.toString() ;
    }
    
    protected void outputParamString( StringBuffer buf ) {
    }    
        
    public void setSource( String src ) {
       this.src  = src  ;   
    }
    
    public String getSource() { return src ; }
        
    public void setDestination( String dest ) {
       this.dest  = dest  ;   
    }
    
    public String getDestination() { return dest ; }
    
    public static final void format( StringBuffer buf, UIDPDU uid ) {
          if ( uid == null ) {
              buf.append( "null" ) ;   
          }
          else 
              uid.outputParamString( buf ) ;
    }
    
    public static final void format( StringBuffer buf, SymbolPDU pdu ) {
          if ( pdu == null ) {
              buf.append( "null" ) ;   
          }
          else 
              pdu.outputParamString( buf ) ;        
    }
    
    /** Aux. function for generating time.
     */
    public static final void formatTimeAndDate( StringBuffer buf, long time ) {
        synchronized ( date ) {
           date.setTime( time ) ;
           df.format( date, buf, new FieldPosition( DateFormat.YEAR_FIELD ) ) ;
           calendar.setTime( date ) ;
           buf.append( ' ' ) ;
           int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
           if ( hourOfDay < 10 ) {
              buf.append( '0' ) ;   
           }
           buf.append( hourOfDay ).append( ':' ) ;
           int minute = calendar.get( Calendar.MINUTE ) ;
           if ( minute < 10 ) {
              buf.append( '0' ) ;   
           }
           buf.append( minute ).append( ':' ) ;
           int second = calendar.get( Calendar.SECOND ) ;
           if ( second < 10 ) {
               buf.append( '0' ) ;   
           }
           buf.append( second ).append( '.' ).append( calendar.get(Calendar.MILLISECOND) ) ;
           //buf.append( calendar.get(Calendar.AM_PM) ) ;
           //buf.append( '.' ).append( time - (time/1000) * 1000  ) ;
        }
    }
    
    public static final String formatTimeAndDate( long time ) {
           StringBuffer buf = new StringBuffer() ;
           formatTimeAndDate( buf, time ) ;
           return buf.toString() ;
    }
    
    public static String formatElapsedTime( long time ) {
       StringBuffer buf = new StringBuffer() ;
       time = time / 1000 ;
       long temp ;
       buf.append( temp = time / 36000 ) ;
       time = time - temp * 36000 ;
       buf.append( "h " ).append( temp = time / 60  ) ;
       time = time - temp * 60 ;
       buf.append( "m " ).append( time ).append( "s " ) ;
       return buf.toString() ;
    }
    
    public static void formatTime( StringBuffer buf, long time ) {
        synchronized ( date ) {
            date.setTime( time ) ;
            calendar.setTime( date ) ;
           int hourOfDay = calendar.get( Calendar.HOUR_OF_DAY ) ;
           if ( hourOfDay < 10 ) {
              buf.append( '0' ) ;   
           }
           buf.append( hourOfDay ).append( ':' ) ;
           int minute = calendar.get( Calendar.MINUTE ) ;
           if ( minute < 10 ) {
              buf.append( '0' ) ;   
           }
           buf.append( minute ).append( ':' ) ;
           int second = calendar.get( Calendar.SECOND ) ;
           if ( second < 10 ) {
               buf.append( '0' ) ;   
           }
           buf.append( second ).append( '.' ).append( calendar.get(Calendar.MILLISECOND) ) ;
        }        
    }
    
    public static String formatTime( long time ) {
        StringBuffer buf = new StringBuffer() ;
        formatTime( buf, time ) ;
        return buf.toString() ;
    }
        
    /** This is filled in by the infrastructure when the PDU is received.  Generally, it 
     * indicates which cluster the PDU originated from. Leave this null for PDUs which
     * are being sent.
     */
    protected String src ;
    
    /** Destination agent.  Set by the sender before it is sent or by the infrastructure.
     *  May be null.
     */
    protected transient String dest ;
    
    protected static final Date date = new Date() ;
    protected static final DateFormat df ;
    protected static final Calendar calendar = Calendar.getInstance();

    static {
        df = DateFormat.getDateInstance( DateFormat.SHORT ) ;
    } 
    
    static final long serialVersionUID = 8976454126563625615L;    
}
