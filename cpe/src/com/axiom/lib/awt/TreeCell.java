package com.axiom.lib.awt ;
import javax.swing.* ;
import java.awt.* ;
import java.util.* ;

public interface TreeCell {

    /**
    public void paint( Graphics g ) {
        FontMetrics fm = java.awt.Toolkit.getDefaultToolkit().getFontMetrics( getFont() ) ;

        g.setColor( Color.blue ) ;
        g.drawString( object.toString(), 2, fm.getHeight() ) ;
    }
    */

    public boolean isLeaf() ;

    public void addCell( TreeCell cell ) ;

    public int getNumCells() ;

    public TreeCell getCellAt( int i ) ;

    public void removeCellAt( int i ) ;

    public void removeAllCells() ;

    public void setExpanded( boolean expanded ) ;

    public boolean getExpanded() ;

    public boolean getSelected() ;

    public void setSelected( boolean selected ) ;
}
