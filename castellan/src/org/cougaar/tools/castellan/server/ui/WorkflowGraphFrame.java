package org.cougaar.tools.castellan.server.ui;

import att.grappa.*;
import org.cougaar.tools.alf.AgentLoadObserver;
import org.cougaar.tools.alf.BoundaryVerbTaskAggregate;
import org.cougaar.tools.castellan.server.ui.events.LogReferenceEventListener;
import org.cougaar.tools.castellan.server.ui.events.LogReferenceEvent;
import org.cougaar.tools.castellan.analysis.UniqueObjectLog;

public class WorkflowGraphFrame extends GraphFrame implements LogReferenceEventListener
{
    public WorkflowGraphFrame ( AgentLoadObserver observer, String name, Graph graph )
    {
        super( name, graph );
        this.observer = observer ;
        this.gp.addGrappaListener( new GrappaAdapter() {
            public void grappaClicked ( Subgraph subgraph, Element element, GrappaPoint point, int i, int i1, GrappaPanel panel )
            {
                doMouseClicked( subgraph, element, point, i, i1, panel ) ;
            }
        }) ;
    }

    public void logReferenceOccured ( LogReferenceEvent evt )
    {
        UniqueObjectLog ul = evt.getLog() ;

        if ( ul != null ) {

            cluster = ul.getCluster();
        }
    }

    public void doMouseClicked(Subgraph subgraph, Element element, GrappaPoint point, int i, int i1, GrappaPanel panel) {

        if ( element != null ) {
//            System.out.println( "Mouse clicked on element " + element.getName() );
        }
        if ( element instanceof Node ) {
            Node n = ( Node ) element ;
            int id = -1 ;

            try {
                id = Integer.parseInt( n.getName() ) ;
            }
            catch ( Exception e ) {

            }
            if ( id != -1 ) {
               BoundaryVerbTaskAggregate bvtl = observer.getTaskAggregate( id, null, cluster, null, null )  ;
                if ( bvtl != null ) {
                    //System.out.println( "Selected " + bvtl );
                    int location = splitPane.getDividerLocation() ;
                    splitPane.setRightComponent( new AggregateLogPanel( bvtl ) );
                    splitPane.setDividerLocation( location );
                }
            }
        }
    }

    AgentLoadObserver observer;
    protected String cluster;
}
