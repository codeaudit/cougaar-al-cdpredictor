package com.axiom.lib.beans ;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.* ;

public class PropertySelector extends JComboBox implements ItemListener {
    
    public PropertySelector( PropertyEditor pe ) {
        editor = pe ;
	    String tags[] = editor.getTags();
	    for (int i = 0; i < tags.length; i++) {
	        addItem(tags[i]);
	    }
	    setSelectedItem( editor.getAsText() );
	    addItemListener(this);
    }

    // Instant action, no undo!
    public void itemStateChanged(ItemEvent evt) {
	    String s = ( String ) getSelectedItem();
	    if ( editor != null ) {
	        editor.setAsText(s);
	    }
    }

    /**
    public void repaint() {
        if ( editor != null ) {
	        setSelectedItem(editor.getAsText());
	    }
    }
    */
    
    PropertyEditor editor ;
}