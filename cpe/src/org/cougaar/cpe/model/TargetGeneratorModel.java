package org.cougaar.cpe.model;

import org.cougaar.cpe.ui.WorldDisplayPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.awt.*;

public class TargetGeneratorModel implements TargetGenerator
{
    private long nextClusterGenerationTime;
    private boolean isWaitingToGenerate = false;
    private float probUnitGeneration = 0.45f;
    private WorldState ws;
    private long lastGenerationTime = 0 ;

    public static final class Cluster {
        public Cluster(float xCenter, float clusterSize, long start, long end, float probGeneration) {
            this.xCenter = xCenter;
            this.clusterSize = clusterSize;
            this.start = start;
            this.end = end;
            this.probGeneration = probGeneration;
        }

        public String toString()
        {
            return "[ xCenter=" + xCenter + ",size=" + clusterSize + ",start=" + (start /1000) + ",end=" + (end/1000) + "]" ;
        }

        float xCenter ;
        float clusterSize ;
        long start ;
        long end ;
        long lastGenerationTime ;
        float probGeneration ;
    }

    /**
     * Configuring the target generator model with default parameters
     */
    public TargetGeneratorModel()
    {
    }

    private String getNodeValueForTag(Document doc, String tagName, String namedItem ) {
        NodeList nodes = doc.getElementsByTagName( tagName );

        String value = null ;
        // Get target plan log
        for (int i=0;i<nodes.getLength();i++) {
            Node n = nodes.item(i) ;
            value = n.getAttributes().getNamedItem( namedItem ).getNodeValue() ;
        }
        return value;
    }

    public void initialize(Document doc)
    {
        Node root = doc.getDocumentElement() ;
        if( root.getNodeName().equals( "TargetGeneratorConfig" ) ) {
            String probGenerationValue = getNodeValueForTag(doc, "ProbGeneration", "value" ) ;
            if ( probGenerationValue != null ) {
                probUnitGeneration = Float.parseFloat( probGenerationValue ) ;
            }
        }
        else {
            throw new RuntimeException( "No root document element TargetGeneratorConfig found." ) ;
        }

    }

    public TargetGeneratorModel( WorldState ws, int seed, int numClustersActive, float probGeneration ) {
        this.ws = ws ;
        random = new Random( seed ) ;
        this.probUnitGeneration = probGeneration ;
        maxClustersActive = numClustersActive ;
    }

    /**
     * Reset this generator to the current time base.
     */
    public void resetTime( long time) {
        nextClusterGenerationTime = time + nextClusterGenerationTime - lastGenerationTime ;
    }

    public void execute() {
        execute( ws );
    }

    public void execute( WorldState ws ) {

        for (Iterator iter = clusters.iterator(); iter.hasNext(); ) {
            Cluster cluster = ( Cluster) iter.next();
            if ( ws.getTime() > cluster.end ) {
                //System.out.println("Removing cluster " + cluster );
                iter.remove();
            }

            // Add some elements to this cluster.
            if ( ws.getTime() - lastGenerationTime > clusterGeneratorDelay ) {
                float next = random.nextFloat() ;
                if ( next < cluster.probGeneration ) {
                    float location = cluster.xCenter + ( float ) random.nextGaussian() * cluster.clusterSize ;
                    if ( location > ws.getLowerX() && location < ws.getUpperX() ) {
                        ws.addTarget( location, ws.getUpperY(), 0, -VGWorldConstants.getTargetMoveRate() ) ;
                    }
                    lastGenerationTime = ws.getTime() ;
                }
            }
        }

        // Try and generate the next cluster.
        if ( clusters.size() < maxClustersActive && isWaitingToGenerate == false ) {
            lastGenerationTime = ws.getTime() ;
            nextClusterGenerationTime = ws.getTime() + nextInUniformDistribution( minClusterTimeDelay, maxClusterTimeDelay ) ;
            isWaitingToGenerate = true ;
        }

        if ( isWaitingToGenerate && ws.getTime() >= nextClusterGenerationTime ) {
            // Create a cluster.
            if ( direction > 0  ) {
                if ( ws.getUpperX() - previousClusterLocation < minClusterDistance ) {
                    direction = -1 ;
                }
            }
            else if ( direction < 0 ) {
                if ( previousClusterLocation - ws.getLowerX() < minClusterDistance ) {
                    direction = 1 ;
                }
            }

            float nextLocation = 0 ;
            if ( direction > 0 ) {
                nextLocation = (float) nextInUniformDistribution( previousClusterLocation + minClusterDistance, ws.getUpperX() ) ;
            }
            else {
                nextLocation = (float) nextInUniformDistribution( ws.getLowerX(), previousClusterLocation - minClusterDistance)  ;
            }

            float xPosition= nextLocation ;
            //float xPosition = (float) nextInUniformDistribution( ws.getLowerX(), ws.getUpperX() ) ;
            float clusterSize = nextInUniformDistribution( minClusterSize, maxClusterSize ) ;
            nextClusterGenerationTime = ws.getTime() + nextInUniformDistribution( minClusterTimeDelay, maxClusterTimeDelay ) ;
            previousClusterLocation = nextLocation ;
            Cluster c = new Cluster( xPosition, clusterSize,
                                    ws.getTime() ,
                                    Math.round( (nextClusterGenerationTime + nextInUniformDistribution( minClusterTime, maxClusterTime ) )/ 1000 ) * 1000,
                                    probUnitGeneration ) ;
            System.out.println("Generating a cluster at " + c ) ;
            clusters.add( c ) ;
            isWaitingToGenerate = false ;
        }
    }

    public int nextInUniformDistribution( int lower, int upper ) {
        return Math.round( random.nextFloat() * ( upper - lower ) + lower ) ;
    }

    public float nextInUniformDistribution( float lower, float upper ) {
        return random.nextFloat() * ( upper - lower ) + lower ;
    }

    public double nextInUniformDistribution( double lower, double upper ) {
        return random.nextFloat() * ( upper - lower ) + lower ;
    }

    /**
     * These sizes are actually std. deviation numbers.
     */
    float maxClusterSize = 4.5f, minClusterSize = 2.5f ;

    /**
     * Space out elements within the cluster by one delta t.
     */
    int clusterGeneratorDelay = 5000 ;
    int minClusterTime = 35000, maxClusterTime = 60000 ;
    int minClusterTimeDelay = 40000, maxClusterTimeDelay = 95000 ;

    float previousClusterLocation = 0 ;
    int direction = 1 ;
    float minClusterDistance = 6 ;

    int maxClustersActive = 3 ;

    Random random ;
    ArrayList clusters = new ArrayList() ;

    public static final void main( String[] args ) {
        ReferenceWorldState ws = new ReferenceWorldState( 36, 28, 4, -1.5, 5 ) ;
        TargetGeneratorModel model = new TargetGeneratorModel(ws,0xcafebabe, 5, 0.5f) ;
        for (int i=0;i<250;i++) {
            model.execute();
            ws.updateWorldState();
        }

        JFrame frame = new JFrame( "Test" ) ;
        WorldDisplayPanel panel = new WorldDisplayPanel( ws ) ;
        Container c = frame.getContentPane() ;
        c.setLayout( new BorderLayout() );
        c.add( panel, BorderLayout.CENTER ) ;
        frame.setSize( 800, 600 );
        frame.setVisible( true );
    }
}
