package com.axiom.lib.beans ;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;
import javax.swing.* ;
import java.io.* ;

/**
 *  A standard file name editor.
 */
public class DefaultFileNameEditor extends JFileChooser
        implements PropertyEditor
{
    public DefaultFileNameEditor() {
        this.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                   doActionPerformed( e ) ;
                }
            } ) ;
    }

    public void setValue( Object o ) {
        if ( o instanceof File ) {
            this.file = ( File ) o ;
        }
        buildPanels() ;
    }

    public String getJavaInitializationString() { return null ; }

    public String[] getTags() { return null ; }

    public Object getValue() {
        return file ;
    }

    public boolean isPaintable() {
        return true ;
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        gfx.setColor( Color.white ) ;
        gfx.fillRect( box.x, box.y, box.width, box.height ) ;
        gfx.setColor( Color.black ) ;
        String temp = String.valueOf( file ) ;
        if ( temp == null )
            gfx.drawString( "<null>", 2, box.height ) ;
        else
            gfx.drawString( temp, 2, box.height ) ;
    }

    public void doActionPerformed( ActionEvent e ) {

        if ( e.getActionCommand() == this.APPROVE_SELECTION ) {
            File f = getSelectedFile() ;
            if ( f.canWrite() || !f.exists() ) {
                file = f ;
                firePropertyChange( "", null, null ) ;
            }
            else {
                JOptionPane.showMessageDialog( this, "Cannot write to file " + f,
                                               "DefaultFileNameEditor", JOptionPane.ERROR_MESSAGE ) ;
            }
        }
        else if ( e.getActionCommand() == this.CANCEL_SELECTION ) {
        }
    }

    public boolean supportsCustomEditor() {
        return true ;
    }

    public Component getCustomEditor() {
        return this ;
    }

    public void setAsText( String s ) {
        file = new File( s ) ;
    }

    public String getAsText() {
        if ( file != null )
            return file.getAbsolutePath() ;
        else
            return "" ;
    }

    protected void buildPanels() {
        if ( file != null ) {
            this.setCurrentDirectory( file.getParentFile() );
            this.setSelectedFile( file ) ;
        }
        this.setFileSelectionMode( FILES_ONLY ) ;
    }

    boolean initialized = false ;
    
    protected File file ;
}