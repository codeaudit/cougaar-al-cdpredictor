package org.cougaar.cpe.mplan;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.TargetEntity;
import org.cougaar.cpe.model.Entity;
import org.cougaar.cpe.model.VGWorldConstants;

import java.util.*;

import com.axiom.lib.util.MultiHashSet;

public class VGStandardSearchHeuristic implements Comparator
{
    ArrayList cliques = new ArrayList() ;
    ArrayList targets = new ArrayList() ;

    /**
     * Find a set of cliques which covers the set of targets listed.
     * @param ws
     */
    protected void computeCliques( ArrayList targets ) {
        Collections.sort( targets, leftToRightComparator );

        MultiHashSet idToCliqueMap = new MultiHashSet() ;

        // Find cliques
        for (int i = 0; i < targets.size(); i++) {
            TargetEntity targetEntity = (TargetEntity)targets.get(i);
            ArrayList clique;
            cliques.add( clique = new ArrayList() );
            for (int j=i+1;j<targets.size();j++) {
                TargetEntity tentity = (TargetEntity) targets.get(j) ;
                if ( tentity.getX() - targetEntity.getX() < VGWorldConstants.getUnitRangeWidth() ) {
                     clique.add( tentity ) ;
                }
                else {
                    break ;
                }
            }
        }

        // Now, sort the cliques by size and merge them.  Don't keep cliques
        // which are subsets of other cliques.
        Collections.sort( cliques, arrayListSizeComparator );

        for (int i = 0; i < cliques.size(); i++) {
           ArrayList arrayList = (ArrayList)cliques.get(i);
           for (int j = 0; j < arrayList.size(); j++) {
              TargetEntity targetEntity = (TargetEntity)arrayList.get(j);
              idToCliqueMap.put( targetEntity.getId(), arrayList ) ;
           }
        }

        // Now, find the set of M non-intersecting cliques.  Use a greedy algorthm (e.g)
        // choose the biggest cliques.
    }

    Comparator leftToRightComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            Entity e1 = (Entity) o1, e2 = (Entity) o2 ;
            double order = e1.getX() - e2.getX() ;
            if ( order < 0 ) {
                return -1 ;
            }
            else if ( order > 0 ) {
                return 1 ;
            }
            return 0 ;
        }
    } ;

    Comparator arrayListSizeComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            ArrayList a1 = (ArrayList) o1, a2 = (ArrayList) o2 ;
            return a2.size() - a1.size() ;
        }
    } ;

    //public int compare(Object n1, Object n2 ) {
        // Compute the _maximum_ number of critical targets are coverable given the
        // current configuration and proportionately, how many are being covered.

        // This is done by
        // grouping critical targets into "cliques" (e.g. clusters that can
        // be covered bv a single unit and then finding which choice of cliques ( n chose m,
        // where n is the number of cliques anb m is the number of units) can be covered by
        // which units.

        // If there are units not assigned to critical sectors, compute their distance to the
        // nearest unassigned critical clique.

    // }


    public int compare(Object n1, Object n2)
    {

        WorldStateNode wsn1 = (WorldStateNode) n1, wsn2 = (WorldStateNode) n2 ;
        if ( wsn1.score < wsn2.score ) {
            return 1 ;
        }
        else if ( wsn1.score > wsn2.score ) {
            return -1 ;
        }



        return 0 ;
    }
}
