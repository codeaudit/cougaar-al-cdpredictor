package com.axiom.lib.awt ;
import javax.swing.* ;
import javax.swing.table.* ;
import java.lang.reflect.* ;
import java.beans.* ;
import java.awt.* ;
import java.awt.event.* ;

public class ArrayInspector extends JPanel {

    public ArrayInspector( InspectorFactory factory, Object o ) {
        this.factory = factory ;
        setTarget( o ) ;
    }

    public void setTarget( Object o ) {
        tclass = o.getClass() ;
        if ( !tclass.isArray() ) {
            throw new IllegalArgumentException( "Cannot inspect non-array." ) ;
        }

        this.target = o ;
        makePanels() ;
    }

    protected void makePanels() {
        setLayout( new BorderLayout() ) ;
        Class ctype = tclass.getComponentType() ;

        int length = Array.getLength( target ) ;

        JLabel title = new JLabel( ctype.getName() + "[" + length + "]" ) ;
        add( title, BorderLayout.NORTH ) ;

        Object[][] data = new Object[length][3];

        isObject = false ;
        if ( ctype == Byte.TYPE ) {
            byte[] b = ( byte[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Character.TYPE ) {
            char[] b = ( char[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Integer.TYPE ) {
            int[] b = ( int[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Float.TYPE ) {
            float[] b = ( float[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Double.TYPE ) {
            double[] b = ( double[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Long.TYPE ) {
            long[] b = ( long[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else if ( ctype == Short.TYPE ) {
            short[] b = ( short[] ) target ;
            for (int i=0;i<length;i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = String.valueOf( b[i] ) ;
            }
        }
        else {
            isObject = true ;
            for (int i=0; i<length; i++) {
                data[i][0] = Integer.toString( i ) ;
                data[i][1] = ctype.getName() ;
                data[i][2] = Array.get( target, i ) ;
            }
        }

        table =  new JTable() { public boolean isCellEditable( int row, int column ) { return false ; } } ;
        TableModel model = new DefaultTableModel( data, columns ) ;
        table.setModel( model ) ;

        add( new JScrollPane( table ) , BorderLayout.CENTER ) ;

        if ( isObject ) {
           table.addMouseListener( new MouseAdapter() {
               public void mouseClicked( MouseEvent e ) {
                   doMouseClicked( e ) ;
               } } ) ;
        }
    }

    public void doMouseClicked( MouseEvent e ) {
        if ( e.getClickCount() >= 2 ) {
            int svalue = table.getSelectedRow() ;
            Object o = table.getValueAt(svalue,2) ;

            if ( factory != null ) {
               factory.add( o ) ;
            }
        }
    }

    private static final String[] columns = { "Name", "Type", "Value" };

    JTable table ;
    Class tclass ;
    Object target ;
    boolean isObject ;
    InspectorFactory factory ;
}