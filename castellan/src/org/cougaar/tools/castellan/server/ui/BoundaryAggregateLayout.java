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

import org.cougaar.tools.alf.AgentLoadObserver;
import org.cougaar.tools.alf.BoundaryConstants;
import org.cougaar.tools.alf.BoundaryVerbTaskAggregate;
import org.cougaar.tools.castellan.analysis.AggregateLog;
import org.cougaar.tools.castellan.util.MultiHashSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class BoundaryAggregateLayout
{

    public int layoutClusteredGraph ( File f, AgentLoadObserver builder )
    {
        int count = 0;
        MultiHashSet mhs = new MultiHashSet();
        for ( Iterator iter = builder.getAggregates(); iter.hasNext(); )
        {
            BoundaryVerbTaskAggregate bvta = ( BoundaryVerbTaskAggregate ) iter.next();
            if ( !bvta.getVerb().equals( "GetLogSupport" ) ) {
                mhs.put( bvta.getCluster(), bvta );
            }

        }

        try
        {
            FileOutputStream fos = new FileOutputStream( f );
            PrintStream ps = new PrintStream( fos, true );
            ps.println( "digraph G {" );
            ps.println( "rankdir=LR;" ) ;
//            ps.println( " size= \"7.5,5\";" );
            ps.println( "ranksep=1.0;" ) ;
            ps.println( "concentrate=false;" ) ;

            int ccount = 0 ;
            // Iterate through subgraphs.
            for ( Iterator iter = mhs.keys(); iter.hasNext(); )
            {
                String key = ( String ) iter.next();
                Object[] objects = mhs.getObjects( key );
                ps.println( "subgraph cluster" + ccount ++  + " {" );
                ps.println( "label=\"" + key + "\";");  // Label of the cluster
                for ( int i = 0; i < objects.length; i++ )
                {
                    BoundaryVerbTaskAggregate bvtl = ( BoundaryVerbTaskAggregate ) objects[i];
                    int id = bvtl.getID();
                    ps.print( "\"" + id + "\"" );
                    printBoundaryNode( ps, bvtl );
                    count++;
                    for ( int j = 0; j < bvtl.getNumChildren(); j++ )
                    {
                        BoundaryVerbTaskAggregate al = ( BoundaryVerbTaskAggregate ) bvtl.getChild( j );

                        // This is within the cluster.
                        if ( al.getCluster().equals( key ) )
                        {
                            ps.println( "\"" + bvtl.getID() + "\" -> \"" + al.getID() + "\";" );
                        }
                    }
                }
                ps.println( "}" );  // end of the subgraph
            }

            // Get all intracluster edges.
            for ( Iterator iter = builder.getAggregates(); iter.hasNext(); )
            {
                BoundaryVerbTaskAggregate bvta = ( BoundaryVerbTaskAggregate ) iter.next();

                for ( int i = 0; i < bvta.getNumChildren(); i++ )
                {
                    BoundaryVerbTaskAggregate al = ( BoundaryVerbTaskAggregate ) bvta.getChild( i );
                    if ( !bvta.getCluster().equals( al.getCluster() ) && !bvta.getVerb().equals("GetLogSupport") )
                    {
                        ps.println( "\"" + bvta.getID() + "\" -> \"" + al.getID() + "\";" );
                    }
                }
            }

            ps.println( "}" );
            ps.close();
            fos.close();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return count;
    }

    protected void printBoundaryNode ( PrintStream ps, BoundaryVerbTaskAggregate bvta )
    {
//        ps.print( " [label=\""  // Begin label
//                + bvta.getCluster() + "," + bvta.getVerb() + ",\\n"
//                + BoundaryConstants.toParamString( bvta.getBoundaryType() )
//                + "\\n#instances=" + bvta.getNumLogs()
//                + "\"" );             // End the label

        // This is the slimmed down presentation.
        ps.print( " [label=\""  // Begin label
                + bvta.getVerb()
                + "(" + bvta.getNumLogs() + ")"
                + "\"" );             // End the label

        int btype = bvta.getBoundaryType();
        if ( bvta.getParent() != null && ( ( BoundaryVerbTaskAggregate ) bvta.getParent() ).getVerb().equals("GetLogSupport") ) {
            ps.println( " shape=hexagon,style=filled,color=aquamarine];" );
        }
        else if ( BoundaryConstants.isIncoming( btype ) && BoundaryConstants.isOutgoing( btype ) )
        {
            ps.println( " shape=box,style=filled,color=powderblue];" );
        }
        else if ( BoundaryConstants.isIncoming( btype ) || BoundaryConstants.isSource(btype))
        {
            ps.println( " shape=box,style=filled,color=palegoldenrod];" );
        }
        else if ( BoundaryConstants.isOutgoing( btype ) )
        {
            ps.println( " shape=box,style=filled,color=palegreen];" );
        }
        else if ( BoundaryConstants.isTerminal( btype ) )
        {
            ps.println( " shape=ellipse,style=filled,color=lightgrey];" );
        }
        else
        {
            ps.println( "]" );  // Default
        }
    }

    public int layoutAggregateGraph ( File f, AgentLoadObserver builder )
    {
        int count = 0;
        try
        {
            FileOutputStream fos = new FileOutputStream( f );
            PrintStream ps = new PrintStream( fos, true );

            ps.println( "digraph G {" );
            ps.println( " size= \"7.5,5\";" );

            for ( Iterator iter = builder.getAggregates(); iter.hasNext(); )
            {
                BoundaryVerbTaskAggregate bvta = ( BoundaryVerbTaskAggregate ) iter.next();
                int id = bvta.getID();
                ps.print( "\"" + id + "\"" );
                ps.print( " [label=\""  // Begin label
                        + bvta.getCluster() + "," + bvta.getVerb() + ",\\n"
                        + BoundaryConstants.toParamString( bvta.getBoundaryType() )
                        + "\\n#instances=" + bvta.getNumLogs()
                        + "\"" );             // End the label

                int btype = bvta.getBoundaryType();
                if ( BoundaryConstants.isIncoming( btype ) && BoundaryConstants.isOutgoing( btype ) )
                {
                    ps.println( " shape=box,style=filled,color=paleblue];" );
                }
                else if ( BoundaryConstants.isIncoming( btype ) )
                {
                    ps.println( " shape=box,style=filled,color=paleyellow];" );
                }
                else if ( BoundaryConstants.isOutgoing( btype ) )
                {
                    ps.println( " shape=box,style=filled,color=palegreen];" );
                }
                else if ( BoundaryConstants.isTerminal( btype ) )
                {
                    ps.println( " shape=ellipse,style=filled,color=lightgrey];" );
                }
                else
                {
                    ps.println( "]" );  // Default
                }

                //
                for ( int i = 0; i < bvta.getNumChildren(); i++ )
                {
                    AggregateLog al = bvta.getChild( i );
                    ps.println( "\"" + bvta.getID() + "\" -> \"" + al.getID() + "\";" );
                }
                count++;
            }
            ps.println( "}" );
            ps.close();
            fos.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            ;
        }
        return count;
    }
}

