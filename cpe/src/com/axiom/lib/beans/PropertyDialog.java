package com.axiom.lib.beans ;

import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;
import javax.swing.* ;
import com.axiom.lib.awt.* ;

public class PropertyDialog extends JDialog implements ActionListener {
    
    public PropertyDialog( Frame frame, PropertyEditor e, int x, int y ) {
        super( frame, e.getClass().getName(), true ) ;
        content = e.getCustomEditor() ;

        getContentPane().setLayout( new BorderLayout() ) ;
        // getContentPane().setBackground( Color.white ) ;
        getContentPane().add( content, BorderLayout.CENTER ) ;
        
        panel = new OKGroupPanel2( this, OptionGroup.OK_BUTTON ) ; 
        getContentPane().add( panel, BorderLayout.SOUTH ) ;
        
	    setLocation(x, y);
        setSize( 450, 400 ) ;
	    show();
    }
    
    /**
    public void doLayout() {
        Dimension contentSize = content.getPreferredSize() ;
        Dimension panelSize = panel.getPreferredSize() ;

        Insets ins = getInsets();        
        int width = contentSize.width > panelSize.width ? contentSize.width : panelSize.width ;
        int height = contentSize.height + panelSize.height ;
        
        setSize( width + ins.left + ins.right, height + ins.top + ins.bottom ) ;

    }
    */
    
    public void actionPerformed( ActionEvent e ) {
        dispose() ;
    }
    
    Component panel ;
    
    Component content ;
}