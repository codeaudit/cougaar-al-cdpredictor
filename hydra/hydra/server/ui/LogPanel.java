package org.hydra.server.ui ;

import org.hydra.server.* ;
import org.hydra.planlog.* ;
import javax.swing.* ;
import java.awt.* ;
import javax.swing.event.* ;
import javax.swing.table.* ;
import java.util.* ;
import java.net.* ;
import java.awt.event.* ;
import java.io.* ;
import org.hydra.pdu.* ;

public class LogPanel extends JPanel implements AppComponent {

    interface LogObserver {
        public EventLog getLog() ;
        public void update() ;
    }

    public class DatabaseLogPanel extends JPanel implements LogObserver {
        DatabaseLogPanel( DatabaseEventLog log ) {
            this.log = log ;
            buildLayout() ;
        }

        public void buildLayout() {
            GridBagLayout gbl = new GridBagLayout() ;
            setLayout( gbl ) ;

            GridBagConstraints gbc = new GridBagConstraints() ;
            gbc.insets = new Insets( 10, 10, 10, 10 ) ;
            gbc.weightx = 1 ;
            gbc.fill = GridBagConstraints.NONE ;

            gbc.gridwidth = 2 ;
            gbc.anchor = gbc.WEST ;
            JLabel nlabel = new JLabel( "Database Event Log" ) ;
            gbl.setConstraints( nlabel, gbc );
            add( nlabel ) ;

            gbc.gridx = 0 ; gbc.gridy = 2 ; gbc.gridwidth = 1 ;
            JLabel label1 = new JLabel( "Name" ) ;
            gbl.setConstraints( label1, gbc ) ;
            add( label1 ) ;
            
            //gbc.gridy = 3 ;
            //JLabel label2 = new JLabel( "# Asset Events" ) ;
            //gbl.setConstraints( label2, gbc );
            //add( label2 ) ;

            // Add an empty panel
            gbc.gridx = 1; gbc.gridy = 2 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
            gbc.weightx = 100 ;
            nameLabel = new JTextField( log.getName() ) ;
            nameLabel.setEditable( false );
            gbl.setConstraints( nameLabel, gbc );
            add( nameLabel ) ;

            JPanel panel = new JPanel() ;
            gbc.fill = GridBagConstraints.BOTH ;
            gbc.gridx = 0; gbc.gridy = 10 ;
            gbc.gridwidth = 2 ;
            gbc.weightx = 100 ; gbc.weighty = 100 ;
            gbl.setConstraints( panel, gbc );
            add( panel ) ;            
        }

        DatabaseEventLog log ;

        JTextField nameLabel ;

        public void update() { } 

        public EventLog getLog() { return log ; }
    }

    public class InMemoryLogPanel extends JPanel implements LogObserver {

        InMemoryLogPanel( InMemoryEventLog log ) {
            this.log = log ;
            buildLayout() ;
        }

        public EventLog getLog() { return log ; }

        protected void doLoadAction() {
            JFileChooser fc = new JFileChooser() ;
            int result = fc.showOpenDialog( this ) ;

            File f = fc.getSelectedFile() ;
            if ( f == null || result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION  ) {
                return ;
            }
            
            try {
                log.clear() ;
                app.println( "Loading messages from " + f ) ;
                FileInputStream fis = new FileInputStream( f ) ;
                ObjectInputStream ois = new ObjectInputStream( fis ) ;
 
                int count = 0 ;
                while ( fis.available() > 0 ) {
                    PDU pdu = ( PDU ) ois.readObject() ;
                    log.add( pdu ) ;
                    count ++ ;
                }               
                app.println( "A total of " + count + " messages were loaded." ) ;
            }
            catch ( Exception e ) {
                e.printStackTrace() ;
            }
        }
        
        protected void doClearAction() {
            log.clear() ;
            update() ;
        }
        
        protected void doSaveAction() {
            JFileChooser fc = new JFileChooser() ;
            int result = fc.showSaveDialog( this ) ;

            File f = fc.getSelectedFile() ;
            if ( f == null || result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION  ) {
                return ;
            }

            if ( f.exists() ) {
                int option = JOptionPane.showConfirmDialog( this, "Overwrite existing file " + f , "Start logging", JOptionPane.YES_NO_OPTION ) ;
                if ( option == JOptionPane.NO_OPTION ) {
                    return ;
                }
            }
            else {
                if ( f.exists() && !f.canWrite() ) {
                    JOptionPane.showMessageDialog( this, "Cannot write to existing file " + f,  "Start logging", JOptionPane.ERROR_MESSAGE ) ;
                    return ;
                }
            }

            FileOutputStream fos = null ;
            try {
                 fos = new FileOutputStream( f ) ;
                ObjectOutputStream oos = new ObjectOutputStream( fos ) ;
                Iterator iter =
                    log.getEventsBetween( log.getFirstEventTime(), log.getLastEventTime() ) ;
                while ( iter.hasNext() )
                {
                    PDU pdu = ( PDU ) iter.next() ;
                    oos.writeObject( pdu) ;
                }
            }
            catch ( Exception e ) {
                e.printStackTrace() ;
            }
            
            try {
                if ( fos != null ) {
                    fos.close() ;   
                }
            }
            catch ( Exception e ) {
                
            }
        }

        void buildLayout() {
            GridBagLayout gbl = new GridBagLayout() ;
            setLayout( gbl ) ;

            GridBagConstraints gbc = new GridBagConstraints() ;
            gbc.insets = new Insets( 15, 15, 5, 10 ) ;
            gbc.weightx = 1 ;
            gbc.fill = GridBagConstraints.NONE ;
            gbc.gridwidth = 2 ;
            gbc.anchor = gbc.WEST ;
            JLabel nlabel = new JLabel( "In-Memory Event Log" ) ;
            gbl.setConstraints( nlabel, gbc );
            add( nlabel ) ;

            gbc.anchor = GridBagConstraints.EAST ;
            gbc.gridy = 2 ; gbc.gridwidth = 1 ;
            gbc.insets = new Insets( 5, 15, 5, 5 ) ;
            JLabel label1 = new JLabel( "# Events" ) ;
            gbl.setConstraints( label1, gbc ) ;
            add( label1 ) ;

            gbc.gridy = 3 ;
            JLabel label2 = new JLabel( "# Asset Events" ) ;
            gbl.setConstraints( label2, gbc );
            add( label2 ) ;
            
            gbc.gridy = 4 ;
            JLabel label3 = new JLabel( "First Event" ) ;
            gbl.setConstraints( label3, gbc ) ;
            add( label3 ) ;
            
            gbc.gridy = 5 ;
            JLabel label4 = new JLabel( "Last Event" ) ;
            gbl.setConstraints( label4, gbc ) ;
            add( label4 ) ;
            
            // Make the second column
            gbc.gridy = 2 ; gbc.gridx = 1 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
            gbc.insets = new Insets( 5, 5, 5, 15 ) ;
            gbc.weightx = 100 ;
            numEventsLabel = new JTextField() ;
            numEventsLabel.setEditable( false );
            gbl.setConstraints( numEventsLabel, gbc );
            add( numEventsLabel ) ;

            gbc.gridy = 3 ; gbc.gridx = 1 ; gbc.fill = GridBagConstraints.HORIZONTAL ;
            numAssetEventsLabel = new JTextField() ;
            numAssetEventsLabel.setEditable( false );
            gbl.setConstraints( numAssetEventsLabel, gbc );
            add( numAssetEventsLabel ) ;
            
            gbc.gridy = 4; gbc.gridx = 1 ;
            startTime = new JTextField() ;
            startTime.setEditable( false ) ;
            gbl.setConstraints( startTime, gbc ) ;
            add( startTime ) ;
            
            gbc.gridy = 5; gbc.gridx = 1 ;
            endTime = new JTextField() ;
            endTime.setEditable( false ) ;
            gbl.setConstraints( endTime, gbc ) ;    
            add( endTime ) ;

            //
            // Add the button panel
            JPanel panel2 = new JPanel( new FlowLayout() ) ;
       
            gbc.gridwidth = 2 ;
            gbc.insets = new Insets( 5, 15, 15, 15 ) ;
            gbc.gridx = 0 ; gbc.gridy = 9 ; gbc.weightx = 0 ;
            gbc.anchor = GridBagConstraints.WEST ; gbc.fill = GridBagConstraints.NONE ;
            gbl.setConstraints( panel2, gbc );
            add( panel2 ) ;
            
            JButton saveButton = new JButton( "Save log..." ) ;
            JButton clearButton = new JButton( "Clear log" ) ;
            JButton loadButton = new JButton( "Load..." ) ;
            panel2.add( saveButton ) ;
            panel2.add( clearButton ) ;
            panel2.add( loadButton ) ;

            //
            // Add an empty panel
            JPanel panel = new JPanel() ;
            gbc.fill = GridBagConstraints.BOTH ;
            gbc.gridx = 0; gbc.gridy = 10 ;
            gbc.gridwidth = 2 ;
            gbc.weightx = 100 ; gbc.weighty = 100 ;
            gbl.setConstraints( panel, gbc );
            add( panel ) ;

            saveButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    doSaveAction() ;
                }
            } ) ;
            
            clearButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    doClearAction() ;
                }
            } ) ; 
            
            loadButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    doLoadAction() ;
                }
            } ) ;
        }

        public void update() {
            numEventsLabel.setText( Integer.toString( log.getNumEvents() ) );
            numAssetEventsLabel.setText( Integer.toString( log.getNumAssetEvents() ) );
            startTime.setText( PDU.formatTimeAndDate( log.getFirstEventTime() ) );
            endTime.setText( PDU.formatTimeAndDate( log.getLastEventTime() ) ) ;
        }

        JTextField numEventsLabel, numAssetEventsLabel, startTime, endTime ;
        InMemoryEventLog log ;
    }


    public LogPanel() {
        buildLayout() ;
        timer = new javax.swing.Timer( 1000, new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( isVisible() ) {
                    update() ;
                }
            }
        } ) ;
        timer.start() ;
    }

    public void setApp(ServerApp app) {
        this.app = app ;
    }

    public static final String getAppComponentName() { 
        return "Log" ; 
    }

    protected void makeNewPanel( EventLog log ) {
        if ( log instanceof InMemoryEventLog ) {
            logComponent = new InMemoryLogPanel( ( InMemoryEventLog ) log ) ;
        }
        else if ( log instanceof DatabaseEventLog ) {
            logComponent = new DatabaseLogPanel( ( DatabaseEventLog ) log ) ;
        }
    }

    public void update() {
        // System.out.println( "Update event log " + app.getEventLog() ) ;

        if ( app == null || app.getEventLog() == null ) {
            if ( logComponent != emptyPanel ) {
                if ( logComponent != null ) {
                    remove( logComponent ) ;
                }
                logComponent = emptyPanel ;
                add( logComponent, BorderLayout.CENTER ) ;
                validate();
            }
            return ;
        }

        EventLog log = app.getEventLog() ;

        if ( logComponent == emptyPanel ) {
            remove( logComponent ) ;
            makeNewPanel( log ) ;
            add( logComponent, BorderLayout.CENTER ) ;
            ( ( LogObserver ) logComponent ).update();
        }
        else {
            LogObserver lo = ( LogObserver ) logComponent ;
            if ( lo == null || lo.getLog() != log ) {
               if ( logComponent != null ) {
                    remove( logComponent ) ;
               }
               makeNewPanel( log ) ;
               add( logComponent, BorderLayout.CENTER ) ;
               ( ( LogObserver ) logComponent ).update();
            }
            else {
               lo.update();
            }
        }
    }

    protected void buildLayout() {
        setLayout( new BorderLayout() ) ;
        emptyPanel = new JPanel() ;
        emptyPanel.setLayout( new BorderLayout() ) ;
        emptyPanel.add( new JLabel( "No log is active."), BorderLayout.CENTER ) ;
    }

    protected JPanel emptyPanel ;
    protected javax.swing.Timer timer ;
    protected Component logComponent ;
    protected ServerApp app ;
}