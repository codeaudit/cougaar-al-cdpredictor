package com.axiom.lib.beans ;
import javax.swing.* ;
import javax.swing.table.* ;
import java.awt.* ;
import java.beans.* ;
import java.lang.reflect.* ;
import javax.swing.event.* ;
import java.util.* ;

public class PropertyEditorTable extends JTable {
    
    class PropertyCellEditor implements javax.swing.table.TableCellEditor {
        
        PropertyCellEditor() {
            PropertyDescriptor[] pd = info.getPropertyDescriptors() ;
            
            for (int i=0;i<pd.length;i++) {
               PropertyEditor editor = 
                   PropertyEditorManager.findEditor( pd[i].getPropertyType() ) ;                
            }
        }
        
        public void addCellEditorListener( CellEditorListener e ) {
            
        }
        
        public void removeCellEditorListener( CellEditorListener e ) {
        }
                
        public Component getTableCellEditorComponent(JTable table, Object value, 
                                                boolean isSelected, int row, int column)
        {
            
            return null ;
        }
        
        public boolean shouldSelectCell( EventObject e ) {
            return false ;
        }
        
        public boolean stopCellEditing() {
            return false ;
        }
        
        public void cancelCellEditing() {
        }
                
        public Object getCellEditorValue() { return null ; }
        
        public boolean isCellEditable( EventObject e ) { return false ; }
        
    }

    static final String PROPERTY_NAME = "Property Name";

    static final String VALUE = "Value" ;
    
    class PropertyTableModel implements TableModel {
        
        PropertyTableModel( Object o ) {
        }
        
        public synchronized void addTableModelListener( TableModelListener l ) {
            listeners.addElement( l ) ;
        }
        
        public synchronized void removeTableModelListener( TableModelListener l ) {
            listeners.removeElement( l ) ;
        }
        
        public Class getColumnClass( int i ) {
            // return Object.class ;
            return null ;
        }
        
        void setObject( Object o ) {
            this.object = o ;
        }
        
        public boolean isCellEditable( int row, int column ) {
            if ( column == 0 ) {
                return false ;
            }
            
            PropertyDescriptor pd = info.getPropertyDescriptors()[row] ;
            
            
            return true ;
        }
        
        public int getRowCount() { return info.getPropertyDescriptors().length ; }
        
        public Object getValueAt( int row, int column ) {
            if ( column == 0 ) {
                return info.getPropertyDescriptors()[row].getDisplayName() ;
            }
            
            return info.getPropertyDescriptors()[row] ;
        }
        
        public void setValueAt( Object o, int row, int column ) {
            
            if ( column == 0 )
               ;
            else {
                PropertyDescriptor pd = info.getPropertyDescriptors()[row]; 
                Method m = pd.getWriteMethod() ;
                // Write the object in directly, using the write methdo
            }
        }
        
        public int getColumnCount() { return 2 ; }
        
        public String getColumnName( int index ) {
            if ( index == 0 ) {
                return PROPERTY_NAME ;
            }
            else if ( index == 1 ) {
                return VALUE ;
            }
            return null ;
        }
        
        Object object ;
        Vector listeners = new Vector() ;
    }

     
    BeanInfo info ;
}