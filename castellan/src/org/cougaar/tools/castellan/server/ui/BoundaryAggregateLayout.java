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
package org.cougaar.tools.castellan.server.ui;

import org.cougaar.tools.castellan.analysis.AggregateTaskBuilder;
import org.cougaar.tools.castellan.analysis.AggregateLog;
import org.cougaar.tools.castellan.server.ServerApp;
import org.cougaar.tools.alf.AgentLoadObserver;
import org.cougaar.tools.alf.BoundaryVerbTaskAggregate;
import org.cougaar.tools.alf.BoundaryConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class BoundaryAggregateLayout {

    public int layoutAggregateGraph( File f, AgentLoadObserver builder ) {
        int count = 0 ;
        try {
            FileOutputStream fos = new FileOutputStream( f ) ;
            PrintStream ps = new PrintStream( fos, true ) ;

            ps.println( "digraph G {" );
            ps.println( " size= \"7.5,5\";" ) ;

            for ( Iterator  iter = builder.getAggregates(); iter.hasNext(); ) {
                BoundaryVerbTaskAggregate bvta = ( BoundaryVerbTaskAggregate ) iter.next() ;
                int id = bvta.getID() ;
                ps.print( "\"" + id + "\"" ) ;
                ps.println( " [label=\""  // Begin label
                          + bvta.getCluster() + "," + bvta.getVerb() + ",\\n"
                          + BoundaryConstants.toParamString( bvta.getBoundaryType() )
                          + "\\n#instances=" + bvta.getNumLogs()
                          + "\""  +             // End the label
                          " shape=box,style=filled,color=palegreen]; "   // The appearance
                ) ;
                //
                for (int i=0;i<bvta.getNumChildren();i++) {
                    AggregateLog al = bvta.getChild(i) ;
                    ps.println( "\"" + bvta.getID() + "\" -> \"" + al.getID() + "\";" );
                }
                count++ ;
            }
            ps.println( "}" ) ;
            ps.close();
            fos.close();
        }
        catch ( Exception e ) {
             e.printStackTrace(); ;
        }
        return count ;
    }
}

