package com.axiom.lib.awt ;
import javax.swing.* ;
import javax.swing.border.* ;
import com.axiom.lib.beans.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;
import java.lang.reflect.* ;

public class PropertyInspector extends JPanel {

    public PropertyInspector( InspectorFactory f ) {
        factory = f;
    }

    public synchronized void setObject( Object o  ) {
        this.object = o ;
        buildPanel() ;
    }

    void buildPanel() {
        removeAll() ;

        try {
            BeanInfo bi = Introspector.getBeanInfo(object.getClass());
            properties = bi.getPropertyDescriptors();
        }
        catch (IntrospectionException ex) {
            System.err.println("PropertySheet: Couldn't introspect: " + ex);
             return;
        }

        editors = new PropertyEditor[properties.length];
        values = new Object[properties.length];
        views = new Component[properties.length];
        labels = new JLabel[properties.length];

        PropertyChangeListener editedAdapter = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
	            PropertyInspector.this.wasModified(evt);
            }

            PropertyInspector sink;
        } ;

        for (int i = 0; i < properties.length; i++) {
            // Don't display hidden or expert properties.
            String name = properties[i].getDisplayName();
            Class type = properties[i].getPropertyType();
            Method getter = properties[i].getReadMethod();
            Method setter = properties[i].getWriteMethod();
            // Only display read && read/write properties.
            if (getter == null) {
                continue;
            }
            Component view = null;

	        try {
		        Object args[] = { };
		        Object value = getter.invoke(object, args);
	            values[i] = value;

	            PropertyEditor editor = null;
	            Class pec = properties[i].getPropertyEditorClass();
		        if (pec != null) {
		            try {
			        editor = (PropertyEditor)pec.newInstance();
		            } catch (Exception ex) {
			        // Drop through.
		            }
		        }

		        if (editor == null) {
		            editor = PropertyEditorManager.findEditor(type);
		        }
	            editors[i] = editor;

                if ( editor != null )
	                editor.setValue(value);
	            //editor.addPropertyChangeListener( editedAdapter ) ;

                // Now figure out how to display it...
                // Display primitives as their values, otherwise,
                // display their class name.
                if ( editor != null && ( type == Integer.class || type == Integer.TYPE ||
                     type == Float.class || type == Float.TYPE ||
                     type == Character.class || type == Character.TYPE ||
                     type == Boolean.class || type == Boolean.TYPE ||
                     type == Double.class || type == Double.TYPE ||
                     type == Long.class || type == Long.TYPE ) )
                {
                    if (editor.getAsText() != null ) {
		                view = new PropertyTextEditor(editor ) ;
                        view.setEnabled( false ) ;
                    }
                    else {
                        view = new ObjectRefComponent( values[i] ) ;
                    }
                }
                else {  // Otherwise, just create an ReferenceEditor
                    view = new ObjectRefComponent( values[i] ) ;
                }

                if ( view instanceof ObjectRefComponent ) {
                    view.addMouseListener( new MouseAdapter() {
                        public void mouseClicked( MouseEvent e ) {
                            objectReferenceMouseClicked( e ) ;
                        }
                    } );
                }
	        }
	        catch (InvocationTargetException ex) {
		        System.err.println("Skipping property " + name + " ; exception on target: " + ex.getTargetException());
		        ex.getTargetException().printStackTrace();
		        continue;
		    }
	        catch ( Exception e ) {
		        System.err.println("Skipping property " + name + " ; exception: " + e);
		        e.printStackTrace();
		        continue ;
	        }

            labels[i] = new JLabel(name) ;
            views[i] = view ;
        }

        /**
        int count ;
        for (int i=0;i<views.length;i++) {
            if ( labels[i] == null || views[i] == null )
                ;
            else
                count++ ;
        }
        
        Object[][] temp = new Object[count][3];
        
        for (int i=0;i<views.length;i++) {
            if ( labels[i] == null || views[i] == null )
                ;
            else {
                temp[i][0] = labels[i].getText() ;
            }                
        }

        table = new JTable( ) ;
        if ( model instanceof DefaultTableModel ) {
            ((DefaultTableModel)model).setDataVector( temp, columnIds ) ;
        }
        */

        GridBagLayout gbLayout = new GridBagLayout() ;
        setLayout( gbLayout ) ;
        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.weightx = gbc.weighty = 0 ;

        int k = 0 ;
        for (int i=0;i<views.length;i++) {
            if ( labels[i] == null || views[i] == null )
                ;
            else {
                gbc.gridy = k++ ;  // k is the kth row of grid bag layout.
                gbc.gridx = 0 ;
                gbc.fill = GridBagConstraints.NONE ;
                gbc.weightx = 0 ;
                gbc.anchor = GridBagConstraints.WEST ;
                labels[i].setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 )  ) ;
                labels[i].setBackground( Color.white ) ;
                gbLayout.setConstraints( labels[i], gbc ) ;
                add( labels[i] ) ;

                gbc.gridx = 1 ;
                JLabel tmp = new JLabel( properties[i].getPropertyType().getName().toString() ) ;
                tmp.setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 0 ) ) ;
                gbLayout.setConstraints( tmp, gbc ) ;
                add( tmp ) ;

                gbc.fill = GridBagConstraints.BOTH ;
                gbc.gridx = 2 ;
                gbc.weightx = 100 ;
                gbLayout.setConstraints( views[i], gbc ) ;
                add( views[i] ) ;
            }
        }

        // Add a dummy panel to take any additional space
        gbc.gridy = k ;
        gbc.weighty = 100 ;
        gbc.weightx = 100 ;
        gbc.fill = GridBagConstraints.BOTH ;
        JPanel dummy = new JPanel() ;
        dummy.setMinimumSize(new Dimension(0,0)) ;
        gbLayout.setConstraints( dummy, gbc ) ;
        add( dummy ) ;
    }

    void objectReferenceMouseClicked( MouseEvent e ) {
        Component src = e.getComponent() ;

        if ( src instanceof ObjectRefComponent && e.getClickCount() == 2 ) {
            ObjectRefComponent obref = ( ObjectRefComponent ) src ;
            if (factory != null ) {
                Object o = obref.getObject() ;
                factory.add( o ) ;
            }
        }
    }

    synchronized void wasModified( PropertyChangeEvent evt ) {
	    PropertyEditor editor = (PropertyEditor) evt.getSource();
	    for (int i = 0 ; i < editors.length; i++) {
	        if (editors[i] == editor) {
		        PropertyDescriptor property = properties[i];
		        Object value = editor.getValue();
		        values[i] = value;
		        Method setter = property.getWriteMethod();
		        if ( setter == null ) {
		            break ;
		        }

		        try {
		            Object args[] = { value };
		            args[0] = value;
		            setter.invoke(object, args);
		        } 
		        catch (InvocationTargetException ex) {
		            if (ex.getTargetException() instanceof PropertyVetoException) {
		            }
		            else {
		                System.err.println("InvocationTargetException while updating property \""
		                            + property.getName() + "\": \n" + ex.getTargetException());
                        ex.getTargetException().printStackTrace(); 
                    }
		        }
		        catch (IllegalAccessException e ) {   
		        }
		        break ;
            }
        }
        
        // Update the properies.
	    for (int i = 0; i < properties.length; i++) {
	        if (editors[i] == null) {
		        continue;
	        }

	        Object o;

	        try {
	            Method getter = properties[i].getReadMethod();
	            Object args[] = { };
	            o = getter.invoke(object, args);
	        } 
	        catch (Exception ex) {
		        o = null;
	        }
	        
	        if ( editors[i] != editor && o == values[i] || (o != null && o.equals(values[i]))) {
	            // The property is equal to its old value.
		        continue;
	        }
	        
	        // Make sure we have an editor for this property...
	        // The property has changed!  Update the editor.
	        //if ( editors[i] != editor ) {
	            values[i] = o;
	            editors[i].setValue(o);
	        //}
	        
	        if (views[i] != null) {
		        views[i].repaint();
	        }
	    }
        
    }

    Object object ;
    
    JTable table ;

    private PropertyDescriptor properties[];
    private PropertyEditor editors[];
    private Object values[];
    private Component views[];
    private JLabel labels[];
    private InspectorFactory factory ;
}
