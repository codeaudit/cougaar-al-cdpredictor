package com.axiom.lib.sys ;
import java.io.* ;
import com.axiom.escript.* ;

/**
 *  ScriptLoader ;oads classes from a target directory.  It is useful
 *  when running scripts, which may be changed during the lifetime of a
 *  program.
 *
 *  <p> Each ScriptLoader is associated with a target directory
 *  in which scripts and all its associated classes may be found.
 *  Classes outside this directory cannot be dynamically reloaded.
 */
public class ScriptLoader extends ClassLoader {
    public ScriptLoader( File packageDir ) {
        this.file = packageDir ;
    }

    public Class loadClass( String name ) throws ClassNotFoundException {
        Class r = null ;
        try {
            r = this.findClass( name ) ;
        }
        catch ( Exception e ) {
        }

        if ( r == null ) {
            return super.loadClass( name ) ;
        }
        return r ;
    }

    public Class loadClass( String name, boolean resolve ) throws ClassNotFoundException {

        Class r = null ;
        try {
            r = this.findClass( name ) ;
        }
        catch ( Exception e ) {
        }

        if ( r == null ) {
            return super.loadClass( name, resolve ) ;
        }
        return r ;
    }

    protected Class findClass( String name ) throws ClassNotFoundException {
        //System.out.println( "Finding class for " + name ) ;
        // Truncate the name of the class and search for it here
        String fullName = name ;
        int ind = name.lastIndexOf(".") ;
        if ( ind != -1 ) {
            name = name.substring( ind + 1 ) ;
        }

        // Check to see if the class matches the fullName
        File f = new File( file, name + ".class" ) ;
        String foundName ;
        try {
            ClassObject c = new ClassObject() ;
            FileInputStream fs = new FileInputStream( f ) ;
            DataInputStream dis = new DataInputStream( fs ) ;
            c.read( dis );
            UTF8ConstantInfo info = ( UTF8ConstantInfo ) c.resolveConstant( c.getClassConstantInfo().getNameIndex() ) ;
            foundName = ClassReaderUtils.convertFromStoredClassName( info.getValue() );
            // System.out.println( "Found " + foundName ) ;
        }
        catch ( Exception e ) {
            return null ;
        }

        if ( !foundName.equals(fullName) ) {
            return null ;
        }

        try {
            FileInputStream fis = new FileInputStream( f ) ;
            byte[] b = new byte[ fis.available() ] ;
            fis.read( b ) ;
            //System.out.println( "Class " + fullName + " (re)defined." ) ;
            return defineClass( fullName, b, 0, b.length ) ;
        }
        catch ( Exception e ) {
            return null ;
        }
    }

    File file ;
}
