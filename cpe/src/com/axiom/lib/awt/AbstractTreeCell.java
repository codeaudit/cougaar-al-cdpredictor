package com.axiom.lib.awt ;
import javax.swing.* ;
import java.awt.* ;
import java.util.* ;

public abstract class AbstractTreeCell extends JComponent {

    protected AbstractTreeCell( TreeCell parent, Object o ) {
        this.parent = parent ;
        this.object = o ;
    }

    /**
    public void paint( Graphics g ) {
        FontMetrics fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics( getFont() ) ;

        g.setColor( Color.blue ) ;
        g.drawString( object.toString(), 2, fm.getHeight() ) ;
    }
    */

    public boolean isLeaf() {
        return false ;
    }

    public void addCell( Component cell ) {
        children.addElement( cell ) ;
    }

    public int getNumCells() {
        return children.size() ;
    }

    public Component getCellAt( int i ) {
        return ( Component ) children.elementAt(i) ;
    }

    public void removeCellAt( int i ) {
        children.removeElementAt( i ) ;
    }

    public void removeAllCells() {
        children.removeAllElements() ;
    }

    public void setExpanded( boolean expanded ) {
        isExpanded = expanded ;
    }

    public boolean getExpanded() {
        return isExpanded ;
    }

    public boolean getSelected() {
        return isSelected ;
    }

    public void setSelected( boolean selected ) {
        isSelected = selected ;
    }


    protected Vector children = new Vector() ;
    protected TreeCell parent ;
    protected Object object ;
    protected boolean isSelected = false ;
    protected boolean isExpanded = false ;
}