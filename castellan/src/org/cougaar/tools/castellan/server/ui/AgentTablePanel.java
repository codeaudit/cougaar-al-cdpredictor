/*
 * AgentTablePanel.java
 *
 * Created on October 1, 2001, 4:31 PM
 */

package org.cougaar.tools.castellan.server.ui;
import org.cougaar.tools.castellan.server.* ;
import javax.swing.* ;
import java.awt.* ;
import javax.swing.event.* ;
import javax.swing.table.* ;
import java.util.* ;
import java.net.* ;
import java.awt.event.* ;

/**
 * Displaye a table indicating
 *
 * @author  wpeng
 * @version
 */
public class AgentTablePanel extends javax.swing.JPanel implements AppComponent {
    
    public static class AgentTableModel implements TableModel {
        public static final String[] columns = { "Name", "Clock Skew", "IP Address", "# Messages", "Active" } ;
        
        AgentTableModel() {
        }
        
        public void setData( Vector v ) {
            this.v = v ;
            // Sort alphabetically?
            for (int i=0;i<list.size();i++) {
                TableModelListener tml = ( TableModelListener ) list.get(i) ;
                tml.tableChanged( new TableModelEvent( this ) );
            }
        }
        
        public java.lang.Object getValueAt(int row, int column ) {
            ClientThread ct = ( ClientThread ) v.elementAt( row ) ;
            switch ( column ) {
                case 0 :
                    return ct.getClusterName() ;
                case 1 :
                    if ( ct.getPingCount() == 0 ) {
                        return "?" ;
                    }
                    return Long.toString( ct.getClockSkewMean() ) ;
                case 2 :
                    Socket s = ct.getSocket() ;
                    if ( s == null ) {
                        return "None" ;
                    }
                    return s.getInetAddress() ;
                case 3 :
                    return Long.toString( ct.getNumEvents() ) ;
                case 4 :
                    return "?" ;
            }
            return null ;
            
        }
        
        public java.lang.String getColumnName(int param) {
            return columns[param] ;
        }
        
        public int getRowCount() {
            if ( v == null ) {
                return 0 ;
            }
            return v.size() ;
        }
        
        public boolean isCellEditable(int param, int param1) {
            return false ;
        }
        
        public void setValueAt(java.lang.Object obj, int param, int param2) {
        }
        
        public java.lang.Class getColumnClass(int param) {
            return String.class ;
        }
        
        public void addTableModelListener(TableModelListener tableModelListener) {
            list.add( tableModelListener ) ;
        }
        
        public int getColumnCount() {
            return columns.length ;
        }
        
        public void removeTableModelListener(TableModelListener tableModelListener) {
            list.remove( tableModelListener ) ;
        }
        
        ArrayList list = new ArrayList() ;
        Vector v = null ;
    }
    
    /** Creates new AgentTablePanel */
    public AgentTablePanel() {
        // Hardwire this
        SocketServerMTImpl impl = ( SocketServerMTImpl ) ServerApp.instance().getServerMessageTransport() ;
        this.ssImpl = impl ;
        buildLayout() ;
        timer = new javax.swing.Timer( 1000, new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                update() ;
            }
        } ) ;
        timer.start() ;
    }
    
    public static final String getAppComponentName() { 
        return "Clusters" ; 
    }
    
    protected void updateTable() {
        if ( ssImpl != null ) {
            int row = table.getSelectedRow() ;
            Vector e = ssImpl.getClients() ;
            agentTableModel.setData( e ) ;
            if ( row != -1 ) {
                table.getSelectionModel().addSelectionInterval( row, row ) ;
            }
        }
        else {
            agentTableModel.setData( null ) ;
        }
    }
    
    protected void buildLayout() {
        agentTableModel = new AgentTableModel() ;
        
        GridBagLayout gbl = new GridBagLayout() ;
        setLayout( gbl ) ;
        GridBagConstraints gbc = new GridBagConstraints() ;
        
        gbc.gridwidth = 4 ; gbc.fill = GridBagConstraints.BOTH ; gbc.insets = new Insets( 20, 20, 20, 20 ) ; gbc.weightx = 100; gbc.weighty = 100 ;
        table = new JTable(agentTableModel) ;
        JScrollPane sp = new JScrollPane( table ) ;
        table.sizeColumnsToFit( JTable.AUTO_RESIZE_OFF ) ;
        gbl.setConstraints( sp, gbc ) ;
        add( sp ) ;
        
        /*
        //JPanel insetPanel = new JPanel(
        gbc.gridwidth = 1; gbc.gridy = 1 ; gbc.weighty = 1 ; gbc.fill = GridBagConstraints.NONE ; gbc.insets = new Insets( 5, 10, 10, 5 ) ;
        JButton startButton = new JButton( "Start" ) ;
        gbl.setConstraints( startButton, gbc ) ;
        add( startButton ) ;
        
        gbc.gridx = 1 ;
        JButton stopButton = new JButton( "Stop" ) ; gbc.insets = new Insets( 5, 5, 10, 5 ) ;
        gbl.setConstraints( stopButton, gbc ) ;
        add( stopButton ) ;
        
        gbc.gridx = 2 ;
        JButton startAllButton = new JButton( "Start All" ) ;
        gbl.setConstraints( startAllButton, gbc ) ;
        add( startAllButton ) ;
        
        gbc.gridx = 3 ; gbc.insets = new Insets( 5, 5, 10, 10 ) ;
        JButton stopAllButton = new JButton( "Stop All" ) ;
        gbl.setConstraints( stopAllButton, gbc ) ;
        add( stopAllButton ) ;
         */
    }
    
    /** Set the current application.  */
    public void setApp(ServerApp app) {
        if ( app == null ) {
            ssImpl = null ;
        }
        else {
            ssImpl = ( SocketServerMTImpl ) app.getServerMessageTransport() ;
        }
        updateTable() ;
    }
    
    /** Refresh this component.  */
    public void update() {
        updateTable() ;
    }
    
    // Refresh rate for the display.
    int refreshRate = 1000 ;
    SocketServerMTImpl ssImpl ;
    JTable table ;
    AgentTableModel agentTableModel ;
    javax.swing.Timer timer ;
    
}
