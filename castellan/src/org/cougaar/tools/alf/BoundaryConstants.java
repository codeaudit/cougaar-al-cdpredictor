package org.cougaar.tools.alf;

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
  */

public class BoundaryConstants {

    public static final boolean isIncoming( int type ) {
        return ( type & INCOMING ) != 0 ;
    }

    public static final boolean isOutgoing( int type ) {
        return ( type & OUTGOING ) != 0 ;
    }

    public static final boolean isTerminal( int type ) {
        return ( type & TERMINAL ) != 0 ;
    }

    public static final boolean isSource( int type ) {
        return ( type & SOURCE ) != 0 ;
    }

    public static final boolean isUnknown( int type ) {
        return ( type & UNKNOWN ) != 0 ;
    }

    public static final boolean isInternal( int type ) {
        return ( type & INTERNAL ) != 0 ;
    }

    public static final String toParamString( int type ) {
        StringBuffer buf = new StringBuffer() ;
        buf.append( "[ " ) ;
        if ( isIncoming(type) ) {
            buf.append( "INCOMING " ) ;
        }
        if ( isSource(type) ) {
            buf.append( "SOURCE " ) ;
        }
        if ( isTerminal(type) ) {
            buf.append( "TERMINAL " ) ;
        }
        if ( isOutgoing(type) ) {
            buf.append( "OUTGOING " ) ;
        }
        if ( isInternal(type) ) {
            buf.append( "INTERNAL " ) ;
        }
        if ( isUnknown(type) ) {
            buf.append( "UNKNOWN " ) ;
        }
        buf.append( "]" ) ;
        return buf.toString() ;
    }

    /**
     * A task coming into this agent.
     */
    public static final int INCOMING = 0x0001 ;

    /**
     * A task going out of this agent.
     */
    public static final int OUTGOING = 0x0002 ;

    /**
     * A task terminating at this agent (allocated to a concrete asset.)
     */
    public static final int TERMINAL = 0x0004 ;

    /**
     * Within the local graph.
     */
    public static final int INTERNAL = 0x0008 ;

    /**
     * This task has unknown status.
     */
    public static final int UNKNOWN = 0x0010 ;

    /**
     * I am generated locally in this asset since I have no parent.
     */
    public static final int SOURCE = 0x0020 ;
}
