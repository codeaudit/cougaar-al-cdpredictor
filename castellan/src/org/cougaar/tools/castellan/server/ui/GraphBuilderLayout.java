package org.cougaar.tools.castellan.server.ui;

import java.util.* ;
import java.io.* ;
import att.grappa.* ;
import org.cougaar.tools.castellan.analysis.* ;
import org.cougaar.tools.castellan.server.* ;
import org.cougaar.tools.castellan.pdu.* ;

/*
 * GraphBuilderLayout.java
 *
 * Created on September 21, 2001, 3:26 PM
 */

/**
 *
 * @author  wpeng
 * @version
 */
public class GraphBuilderLayout {
    public GraphBuilderLayout( ServerApp app ) {
        this.app = app ;
    }

    public int layoutAggregateGraph( File f, AggregateTaskBuilder builder ) {
        LinkedList open = new LinkedList() ;
        ArrayList closed = new ArrayList() ;
        Iterator enum = builder.getAggregateTasks() ;
        // Add to open list until no more can be found.
        while ( enum.hasNext() ) {
            open.add( enum.next() ) ;
        }
        boolean found = false ;
        
        while ( open.size() > 0 ) {
            AggregateLog al = ( AggregateLog ) open.removeFirst() ;
            for (int i=0;i<al.getNumChildren();i++) {
                AggregateLog child = al.getChild( i) ;
                if ( open.indexOf(child) == -1 && closed.indexOf(child) == -1 ) {
                    open.add( child ) ;
                }
            }
            closed.add( al ) ;
        }
        enum = closed.iterator() ;
        int count = 0 ;        
        
        try {
            FileOutputStream fos = new FileOutputStream( f ) ;
            PrintStream ps = new PrintStream( fos, true ) ;
            
            ps.println( "digraph G {" );
            ps.println( " size= \"7.5,5\";" ) ;

            while ( enum.hasNext() ) {
                count ++ ;
                AggregateLog o = ( AggregateLog ) enum.next() ;
                if ( o instanceof AggregateMPTaskLog ) {
                    AggregateVerbTaskLog avtl = ( AggregateVerbTaskLog ) o ;
                    ps.println( "\"" + avtl.getID() +
                    "\" [label=\" MPTask \\n" + avtl.getVerb() + "\\n" + avtl.getCluster() + "\\n#=" + avtl.getNumLogs() +
                    "\",shape=box,style=filled,color=palegreen]; " ) ;                    
                }
                else if ( o instanceof AggregateVerbTaskLog ) {
                    AggregateVerbTaskLog avtl = ( AggregateVerbTaskLog ) o ;
                    ps.println( "\"" + avtl.getID() +
                    "\" [label=\" Task \\n " + avtl.getVerb() + "\\n" + avtl.getCluster() + "\\n#=" + avtl.getNumLogs() +
                    "\",shape=box,style=filled,color=powderblue]; " ) ;
                }
                else if ( o instanceof AggregateExpansionLog ) {
                    AggregateExpansionLog ael = ( AggregateExpansionLog ) o ;
                    StringBuffer verbs = new StringBuffer() ;
                    verbs.append( '[' );
                    String[] cv = ael.getChildVerbs() ;
                    if ( cv.length > 0 ) {
                        verbs.append( cv[0] ) ;
                    }
                    for (int i=1;i<(( cv.length < 3 ) ? cv.length : 3);i++) {
                        verbs.append( ",\\n" ) ;
                        verbs.append( cv[i] ) ;
                    }
                    if ( cv.length > 3 ) {
                        verbs.append( ",\\n..." ) ;
                    }
                    verbs.append( ']' ) ;
                    
                    ps.println( "\"" + ael.getID() +
                    "\" [label=\"Expansion (" + ael.getCluster() + ",#=" + ael.getNumLogs() + ")\\n" + verbs.toString() + "\",shape=box,style=filled,color=palegoldenrod];");
                }
                else {
                    ps.println( "\"" + o.getID() +
                    "\" [shape=box,style=filled,color=powderblue]; " ) ;
                }
                
                for (int i=0;i<o.getNumChildren();i++) {
                    AggregateLog al = o.getChild(i) ;
                    ps.println( "\"" + o.getID() + "\" -> " + al.getID() + ";" );
                    
                }
            }
            ps.println( "}" ) ;
            ps.close();
            fos.close();
        }
        catch ( IOException e ) {
            e.printStackTrace() ;
        }
            
        return count ;
    }
    
    public int layoutAssetDependencyGraph( File f, LogPlanBuilder builder ) {
        int count = 0 ;

        try {
            FileOutputStream fos = new FileOutputStream( f ) ;
            PrintStream ps = new PrintStream( fos, true ) ;
            
            ps.println( "digraph G {" );
            ps.println( " size= \"7.5,5\";" ) ;
            for (Iterator e= builder.getAssets();e.hasNext();) {
                AssetLog al = ( AssetLog ) e.next() ;
                // System.out.println( "Processing asset " + al ) ;
                if ( al.getNumChildren() > 0 || al.getNumParents() > 0 ) {
                    count++ ;
                    if ( builder.isSourceAsset( al.getUID() ) || builder.isSinkAsset( al.getUID() ) ) {
                        ps.println( "\"" + al.getUID() 
                          + "\" [label=\"" + 
                          al.getUID() 
                          + "\",shape=box,style=filled,color=powderblue]; " ) ;
                    }
                    else if ( al.getAssetTypeId().equals( "UTC/RTOrg" ) ){
                        ps.println( "\"" + al.getUID() +  
                        "\" [shape=ellipse,style=filled,color=palegoldenrod]; " ) ;
                    }
                    else {
                        ps.println( "\"" + al.getUID() + 
                        "\" [label=\"" + 
                        al.getUID() + "\\n " + al.getAssetTypeId()                        
                        + "\",shape=box,style=filled,color=palegreen]; " ) ;
                    }
                }
                if ( al.getNumChildren() > 0 ) {
                    for (int i=0;i<al.getNumChildren();i++) {
                        AssetLog cal = al.getChild(i) ;
                        Enumeration ele = cal.getAssetVerbPairs() ;
                        StringBuffer label = new StringBuffer() ;
                        boolean found = false ;
                        // System.out.println( "Processing " + al.getUID() ) ;
                        while ( ele.hasMoreElements() ) {
                            AssetLog.AssetVerbPair p = ( AssetLog.AssetVerbPair ) ele.nextElement() ;
                            // System.out.println( "Processing asset verb pairs for " + p.getAsset() + "," + p.getVerb() ) ;
                            if ( found ) {
                                label.append( "," ) ;
                            }
                            if ( p.getAsset().equals( al.getUID() ) ) {
                                label.append( "verb=").append(p.getVerb()).append(',').append( cal.getTaskCount( p.getAsset(), p.getVerb() ) ) ;
                                found = true ;
                            }
                        }
                        
                        ps.print( '"' + al.getUID().toString() + '"' );
                        ps.print( " -> " );
                        ps.print( '"' + cal.getUID().toString() + '"' );
                        ps.print( " [" ) ;
                        ps.print( "label=\"" + label.toString() + "\"" ) ;
                        ps.println( "];" ) ;
                    }
                }
            }
            ps.println( "}" );
            ps.close();
            fos.close();
        }
        catch ( IOException e ) {
            e.printStackTrace() ;
        }
        
        return count ;
        
    }
    
    public int layoutTaskGraph( File f, LogPlanBuilder builder ) {
        // File f = app.getTempFile( ".dot" ) ;
        FileOutputStream fos = null ;
        try {
            fos = new FileOutputStream( f ) ;
        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }
        PrintStream ps = new PrintStream( fos, true ) ;
        
        PlanLogDatabase pld = builder.getDatabase() ;
        Collection c = pld.getTasks() ;
        
        int count = 0 ;
        ps.println( "digraph G {" );
        ps.println( " size= \"7.5,5\";" ) ;
        for ( Iterator iter=c.iterator(); iter.hasNext(); ) {
            TaskLog tl = ( TaskLog ) iter.next() ;
            if ( tl.getTaskVerb() != null && ( tl.getTaskVerb().equals( "ReportForDuty") || tl.getTaskVerb().equals("ReportForService") ) ) {
                continue ;
            }
            
            //if ( tl.getParent() != null && pld.getLog( tl.getParent() ) == null ) {
            //    continue ;
            //}
            
            count++ ;
            // Now, print children
            UniqueObjectLog l = pld.getPlanElementLogForTask( tl.getUID() ) ;
            
            //if ( GraphBuilder.isShadowTask( tl.getUID() ) ) {
            //     ps.println( "\"" + tl.getUID() + "\" [shape=box, style=filled,color=powderblue]; " ) ;
            //}
            //else {
            if ( tl.getRescindedExecutionTimestamp() == -1 ) {
                ps.println( "\"" + tl.getUID() +
                "\" [shape=record,label=\""+ tl.getUID() + "\\n" + tl.getTaskVerb() +
                "\" style=filled,color=palegoldenrod]; " ) ;
            }
            else {
                ps.println( "\"" + tl.getUID() +
                "\" [shape=record,label=\""+ tl.getUID() + "\\n" + tl.getTaskVerb() +
                "\" style=filled,color=blue]; " ) ;                
            }
            
            if ( l instanceof AllocationLog ) {
                AllocationLog al = ( AllocationLog ) l ;
                if ( al.getAllocTaskUID() != null ) {
                    TaskLog ctl = ( TaskLog ) pld.getLog( al.getAllocTaskUID() ) ;
                    if ( ctl != null ) {
                        ps.print( "\"" + tl.getUID() + '"' );
                        ps.print( " -> " );
                        ps.println( "\"" + ctl.getUID() + "\";\n" );
                    }
                }
            }
            else if ( l instanceof ExpansionLog ) {  // Point directly to children?
                ExpansionLog el = ( ExpansionLog ) l ;
                UIDPDU[] children = el.getChildren() ;
                for (int i=0;i<children.length;i++) {
                    ps.print( "\"" + tl.getUID() + '"' );
                    ps.print( " -> " );
                    ps.print( "\"" + children[i] + "\";\n" );
                    // No label yet
                    //ps.print( " [" ) ;
                }
            }
            else if ( l instanceof AggregationLog ) {
                AggregationLog el = ( AggregationLog ) l ;
                if ( el.getCombinedTask() != null ) {
                    ps.print( "\"" + el.getParent() + '"' ) ;
                    ps.print( " -> " ) ;
                    ps.print( "\"" + el.getCombinedTask() + "\";\n" ) ;
                }
            }
            // else handle AggregateLog objects
        }
        ps.println( "}" );
        ps.close();
        
        try {
            fos.close();
        }
        catch ( java.io.IOException ex ) {
            ex.printStackTrace() ;
        }
        return count ;
    }

    ServerApp app ;
}
