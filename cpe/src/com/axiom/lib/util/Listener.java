package com.axiom.lib.util ;

/**
 *  Base class for all listeners.  This is useful when objects make
 *  references to other objects which may be discarded.  This interface is
 *  often used in conjunction with the <code>Linkable</code> interface and
 *  design pattern.
 *
 */
public interface Listener {
    
    /**
     *  Notify listener that <code>object</code> is being
     *  unlinked from the current graph.
     *
     *  @param Object object which is being unlinked.
     */
    public void notifyUnlink( Object object ) ;
}