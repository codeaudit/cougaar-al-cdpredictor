/*
 * ServerApp.java
 *
 * Created on August 23, 2001, 2:35 PM
 */

package org.hydra.server;
import org.hydra.server.ui.* ;
import org.hydra.planlog.* ;
import java.util.* ;
import java.io.* ;

/**
 *  Encapsulates all elements of the server application, including the UI
 *  message transport, database access, etc.
 *
 * @author  wpeng
 * @version 
 */
public class ServerApp {

    /** Creates new ServerApp */
    protected ServerApp() {
        loadSettings() ;
        
        ss = new SocketServerMTImpl() ;
        frame = new ServerUIFrame(this) ;
        frame.setVisible( true ) ;

        // Remover the default logging sink.
        setEventLog( new InMemoryEventLog() ) ;
        //sink = new ServerPDUSink( eventLog ) ;
        //ss.setPDUSink( sink ) ;
    }
    
    public synchronized File getTempFile( String ext ) {
        if ( getTempPath() == null ) {
            throw new RuntimeException() ;
        }
        
        File tp = new File( getTempPath() ) ;
        if ( tp.exists() && tp.isDirectory() ) {
            long num = System.currentTimeMillis() & 0xFFFF ;
            File f = new File( tp, Long.toString(num) + ext ) ;
            while (f.exists() ) {
                f = new File( tp, Long.toString(++num) + ext ) ;
            }
            return f ;
        }
        throw new RuntimeException() ;
    }   

    public void saveSettings() {
        props.setProperty( "dotPath", dotPath ) ;
        props.setProperty( "tempPath", tempPath ) ;
        props.setProperty( "dbPath", dbPath ) ;
        String chome = System.getProperty( "castellanhome" ) ;
        if ( chome == null ) {
            println( "No castellanhome property set.  Cannot find settings file." ) ;
            return ;
        }

        try {
            File f = new File( chome, "castellan.props" ) ;
            FileOutputStream fos = new FileOutputStream( f ) ;
            props.store( fos, "Castellan settings" ) ;
            fos.close() ;
        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }
    }
        
    protected void loadSettings() {
        System.out.println( "Loading settings..." ) ;
        try {
            props = new Properties() ;
            String chome = System.getProperty( "castellanhome" ) ;
            if ( chome == null ) {
                println( "No castellanhome property set.  Cannot find settings file." ) ;
                return ;
            }
            File f = new File( chome, "castellan.props" ) ;
            if ( !f.exists() || !f.isFile() ) {
                println( "Cannot find or read castellan.props settings file." ) ;
                return ;
            }
            FileInputStream fis = new FileInputStream( f ) ;
            props.load( fis ) ;
            fis.close() ;
            String s = props.getProperty( "dotPath" ) ;
            if ( s != null ) {
                dotPath = s ;
            }
            s =  props.getProperty( "tempPath" ) ;
            if ( s != null ) {
                tempPath = s ;
            }
            s = props.getProperty( "dbPath" ) ;
            dbPath = s ;
        }
        catch ( Exception e ) {
            e.printStackTrace() ;
        }
    }
    
    public void println( String s ) {
        if ( frame == null ) {
            System.out.println( s ) ;
        }
        else {
            frame.printMessage( s ) ;
            frame.printMessage( "\n" ) ;
        }
    }

    public void print( String s ) {
        if ( frame == null ) {
            System.out.println( s ) ;
        }
        else {
            frame.printMessage( s ) ;
        }
    }

    public ServerPDUSink getPDUSink() { return sink ; }

    public void setEventLog( EventLog log ) {
        eventLog = log ;
        if ( log == null ) {
            ss.setPDUSink( null);
        }
        else {
            sink = new ServerPDUSink( eventLog ) ;
            ss.setPDUSink( sink ) ;
        }
    }

    public EventLog getEventLog() { return eventLog ; }
    
    public ServerUIFrame getFrame() { return frame ; }
    
    public ServerMessageTransport getServerMessageTransport() {
       return ss ;   
    }
    
    public static ServerApp instance() { return instance ; }

    public static void main( String[] args ) {
        ServerApp app = new ServerApp() ;
        ServerApp.instance = app ;
    }
    
    public boolean isVerbose() { return verbose ; }
    
    public void setVerbose( boolean value ) { this.verbose = value ; }
    
    public void setTempPath( String path ) { tempPath = path ; }
    
    public void setDotPath( String path ) { dotPath = path ; }

    public void setDbPath( String path ) { dbPath = path ; }

    public String getTempPath() { return tempPath ; }

    public String getDotPath() { return dotPath ; }

    public String getDbPath() { return dbPath ; }

    boolean verbose = false ;
    String tempPath, dotPath, dbPath ;
    Properties props ;
    ServerPDUSink sink ;
    EventLog eventLog ;
    ServerUIFrame frame ; 
    SocketServerMTImpl ss ;
    protected static ServerApp instance ;
}
