package com.axiom.lib.beans ;
import java.awt.* ;
import javax.swing.* ;
import java.beans.* ;
import java.lang.reflect.* ;

public class PropertySheet extends JComponent {

    public PropertySheet( Frame frame, Container container ) {
        this( container ) ;
        //this.frame = frame ;
    }

    public PropertySheet( Container container ) {
        this.container = container ;
        //this.frame = frame ;
    }
    
    public synchronized void setObject( Object o  ) {
        this.object = o ;
        buildPanel() ;
    }
        
    void buildPanel() {
        removeAll() ;

        BeanInfo bi = null ;
        Class c = null ;

        try {

            try {
             c = object.getClass().getClassLoader().loadClass( object.getClass().getName() + "BeanInfo" ) ;
            }
            catch ( ClassNotFoundException e ) {
            }
            
            if ( c != null ) {
               try {
               BeanInfo b = ( BeanInfo ) c.newInstance() ;
               properties = b.getPropertyDescriptors() ;
               }
               catch ( Exception e ) {
                  c = null ;
               }
            }

            if ( c == null ) {
              bi = Introspector.getBeanInfo(object.getClass());
              properties = bi.getPropertyDescriptors();
            }
        }
        catch (IntrospectionException ex) {
            System.err.println("PropertySheet: Couldn't introspect: " + ex);
             return;
        }

        // DEBUG
        /*
        System.out.println( "Sheet for " + c.getName() + " with loader " + c.getClassLoader() ) ;
        System.out.println( "There are a total of " + properties.length + " properties." ) ;
        for (int i=0;i<properties.length;i++) {
            System.out.println( properties[i] );
            PropertyDescriptor pi = properties[i] ;
            Class clazz = pi.getPropertyType() ;
            System.out.println( "Property " + pi.getName() + " is of type " + clazz +
                " with loader " + clazz.getClassLoader() ) ;
        }
        */

        editors = new PropertyEditor[properties.length];
        values = new Object[properties.length];
        views = new Component[properties.length];
        labels = new JLabel[properties.length];

        PropertyChangeListener editedAdapter = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
	            PropertySheet.this.wasModified(evt);
            }
        } ;

        for (int i = 0; i < properties.length; i++) {
            // Don't display hidden or expert properties.
            if (properties[i].isHidden() || properties[i].isExpert()) {
                continue;
            }
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
			           System.out.println( ex );
		            }
		        }
		        
		        if (editor == null) {
		            editor = PropertyEditorManager.findEditor(type);
		        }
	            editors[i] = editor;
		        
	            // If we can't edit this component, skip it.
	            if (editor == null) {
		            // If it's a user-defined property we give a warning.
		            /**
		            String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
		            if (getterClass.indexOf("java.") != 0) {
		                System.err.println("Warning: Can't find public property editor for property \""
				        + name + "\".  Skipping.");
		            }
		            */
		            continue;
	            }

                /*
		        // Don't try to set null values:
		        if (value == null) {
		            // If it's a user-defined property we give a warning.
		            
		            
		            //String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
		            //if (getterClass.indexOf("java.") != 0) {
		            //    System.err.println("Warning: Property \"" + name 
				    //    + "\" has null initial value.  Skipping.");	
		            //}
		            //
		            continue;
		        }		        
		        */

                // Listen to the editor
	            editor.setValue(value);
	            editor.addPropertyChangeListener( editedAdapter ) ;
	            
	            // editor.addPropertyChangeListener(adaptor);
		        // Now figure out how to display it...
		        if (editor.isPaintable() && editor.supportsCustomEditor()) {
		            view = new PropertyComponent( editor);
		        } else if (editor.getTags() != null) {
		            view = new PropertySelector(editor);
		            if ( setter == null ) 
		               view.setEnabled( false ) ;		            
		        } else if (editor.getAsText() != null && type == String.class ) {
		            view = new PropertyStringEditor(editor ) ;
		            if ( setter == null ) {
		               ( (PropertyStringEditor ) view).setNoEnable( true ) ;   
		            }
		        } else if (editor.getAsText() != null ) {
		            String init = editor.getAsText();
		            view = new PropertyTextEditor(editor);
		            if ( setter == null ) 
		               view.setEnabled( false ) ;
		        } else {
		            /**
		            System.err.println("Warning: Property \"" + name 
				    + "\" has non-displayable editor.  Skipping.");
				    */
		            continue;
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
                labels[i].setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 0 )  ) ;
                gbLayout.setConstraints( labels[i], gbc ) ;
                add( labels[i] ) ;
                
                gbc.fill = GridBagConstraints.BOTH ;
                gbc.gridx = 1 ;
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
		                            + property.getName() + "\" : \n" + ex.getTargetException());
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

    private BeanInfo beanInfo ;
    private PropertyDescriptor properties[];
    private PropertyEditor editors[];
    private Object values[];
    private Component views[];
    private JLabel labels[];
    
    private Frame frame ;

    private Container container ;
}