package com.axiom.lib.awt ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;

public class WindowUtil {

    public static Frame getFrameForComponent( Component c ) {
        Component tmp = c ;
        while ( !(tmp.getParent() == null ) ) {
            tmp = tmp.getParent() ;
        }
        if ( tmp instanceof Frame ) {
            return ( Frame ) tmp ;
        }
        return null ;
    }

    public static void placePopupMenu( MouseEvent e, JPopupMenu menu ) {
        Component c = e.getComponent() ;
        Point p = c.getLocationOnScreen() ;
        Dimension d = menu.getSize() ;
        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize() ;
        p.x += e.getX() ; p.y += e.getY() ;
        if ( p.x + d.width > sd.width ) {
            p.x -= ( p.x + d.width - sd.width ) ;
        }
        if ( p.y + d.height > sd.height ) {
            p.y -= ( p.y + d.height - sd.height ) ;
        }
        menu.setLocation( p );
    }

    /**
     *  Centers a component within the bounds of its parent.  If the component
     *  is larger in width or height than the parent, it places it at 0.  This
     *  so that Frames are not placed so that they are inaccessible.
     */
    public static void center( Component c ) {
        int pwidth, pheight ;

        if ( c == null ) {
           return ;
        }

        Dimension d ;
        if ( c.getParent() == null ) {
            d = java.awt.Toolkit.getDefaultToolkit().getScreenSize() ;
        }
        else
            d = c.getParent().getSize() ;

        pwidth = d.width ; pheight = d.height ;

        // Get the size of the component c
        Dimension d2 = c.getSize() ;

        int x, y ;

        if ( d2.width > pwidth )
            x = 0 ;
        else
            x = ( pwidth - d2.width ) / 2 ;

        if ( d2.height > pheight ) {
            y = 0 ;
        }
        else {
            y = ( pheight - d2.height ) / 2 ;
        }
        c.setLocation( x, y ) ;
    }
    
    /**
     *  Get bounds of agents relative to c1's coordinate space.
     */
    
    public static Rectangle getRelativeBounds( Component c1, Component c2 ) {
        Component p ;
        if ( c2 == null )
           return null ;
        
        Rectangle childBounds = c2.getBounds() ;
           
        if ( c1 == null ) {
           Point p1 = c2.getLocationOnScreen();
           return new Rectangle( p1.x, p1.y, childBounds.width, childBounds.height ) ;
        }
        
        Point ploc = c1.getLocationOnScreen() ;
        Point cloc = c2.getLocationOnScreen() ;
        Rectangle result = new Rectangle( cloc.x - ploc.x, cloc.y - ploc.y, childBounds.width, childBounds.height ) ;        
        
        return result ;
        /**
        Rectangle childBounds = child.getBounds() ;
        Rectangle result = new Rectangle( 0, 0, childBounds.width, childBounds.height ) ;
        Component current = child ;
        
        while ( true ) {
           Rectangle r = current.getBounds() ;
           result.setLocation( result.x + r.x, result.x + r.y ) ;
           
           p = current.getParent() ;
           if ( p == parent ) {
              return result ;
           }
           else if ( p == null ) {
              return null ;
           }
           current = p ;
        }
        */
    }
    
}