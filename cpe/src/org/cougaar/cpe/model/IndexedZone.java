package org.cougaar.cpe.model;

/**
 * An indexed zone is just an interval expressed as a pair of index values.  For example,
 * the indexed zone (0,0) is interpreted as the interval from [lower,lower+zoneGridSize].  The indexed zone
 * (1,2) is the interval from [lower+zoneGridSize,lower+zoneGridSize*3].
 */
public class IndexedZone extends Zone
{
    public IndexedZone(int startIndex, int endIndex)
    {
        this.startIndex = startIndex;
        if ( endIndex < startIndex ) {
            throw new IllegalArgumentException( "End index must be >= startIndex" ) ;
        }
        this.endIndex = endIndex;
    }

    public String toString()
    {
        return "IndexedZone[" + startIndex + "," + endIndex + "]" ;
    }

    public boolean contains( IndexedZone iz ) {
        if ( iz.getStartIndex() >= getStartIndex() && iz.getEndIndex() <= getEndIndex() ) {
            return true ;
        }
        return false ;
    }

    public Object clone()
    {
        return new IndexedZone( startIndex, endIndex ) ;
    }

    public boolean equals(Object obj)
    {
        if ( !( obj instanceof IndexedZone ) ) {
             return false ;
        }
        IndexedZone z = (IndexedZone) obj ;
        return z.getStartIndex() == getStartIndex() && z.getEndIndex() == getEndIndex() ;
    }

    public int getEndIndex()
    {
        return endIndex;
    }

    public int getNumZone() {
        return endIndex - startIndex + 1 ;
    }

    public void setEndIndex(int endIndex)
    {
        this.endIndex = endIndex;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }

    public int startIndex, endIndex ;
}
