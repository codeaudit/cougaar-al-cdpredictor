package com.axiom.lib.beans ;
import java.awt.* ;
import java.awt.event.* ;
import java.beans.* ;
import javax.swing.* ;
import java.io.* ;

/**
 *  A standard path editor for only directories.
 */
public class DirectoryPathEditor extends DefaultFileNameEditor
{
    public DirectoryPathEditor() {
        super() ;
        setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
    }

}
