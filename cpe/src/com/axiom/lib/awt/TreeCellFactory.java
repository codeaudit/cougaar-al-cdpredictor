package com.axiom.lib.awt ;
import java.awt.Component ;
import javax.swing.* ;

public interface TreeCellFactory {

    /**
     *  Returns an (arbitrary) component for leaf nodes and a TreeCell component
     *  for non-leaves.  In other words, if TreeModel.isLeaf() is false, then
     *  TreeCellFactory.makeComponentForObject must return an object implementing
     *  the tree cell interface.
     */
    public Component makeComponentForObject( CellTree tree, Object o ) ;
}