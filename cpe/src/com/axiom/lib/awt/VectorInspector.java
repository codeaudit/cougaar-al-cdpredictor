package com.axiom.lib.awt ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.table.* ;
import java.util.Vector ;

public class VectorInspector extends JPanel {

    public VectorInspector( InspectorFactory factory, Object o ) {
        this.factory = factory ;
        setTarget( o ) ;
    }

    public void setTarget( Object o ) {
        if ( !( o instanceof java.util.Vector ) ) {
            throw new IllegalArgumentException( "Object is not of Vector type." ) ;
        }

        this.target = ( Vector ) o ;
        makePanels() ;
    }

    protected void makePanels() {
        setLayout( new BorderLayout() ) ;

        JLabel title = new JLabel( "Vector [" + target.size() + "]" ) ;
        add( title, BorderLayout.NORTH ) ;

        Object[][] data = new Object[target.size()][3];

        for (int i=0; i<target.size(); i++) {
            Object o = target.elementAt(i) ;
            data[i][0] = Integer.toString( i ) ;
            if ( o != null ) {
                data[i][1] = o.getClass().getName() ;
                data[i][2] = o ;
            }
        }

        table = new JTable() { public boolean isCellEditable( int row, int column ) { return false ; } } ;
        TableModel model = new DefaultTableModel( data, columns ) ;
        table.setModel( model ) ;
        
        table.setCellEditor( null ) ;
        add( new JScrollPane( table ) , BorderLayout.CENTER ) ;

        table.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                doMouseClicked( e ) ;
            } } ) ;
    }

    public void doMouseClicked( MouseEvent e ) {
        if ( e.getClickCount() >= 2 ) {
            int svalue = table.getSelectedRow() ;
            Object o = table.getValueAt(svalue,2) ;

            if ( o != null && factory != null ) {
               factory.add( o ) ;
            }
        }
    }

    private static final String[] columns = { "Name", "Type", "Value" };

    JTable table ;
    Vector target ;
    InspectorFactory factory ;
}