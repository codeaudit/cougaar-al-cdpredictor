package com.axiom.escript ;
import java.util.Vector ;
import java.io.* ;
import java.util.Hashtable ;

/**
 *
 *  Classes are either internal or external.  External classes are managed by
 *  the native VM; internal classes are managed by the EScript VM. The interface
 *  between the external and internal VMs is through external "stub classes" and
 *  their instances.
 *
 *  <p> A stub class has the same fields and methods as the internal class, but forwards
 *  all method calls to the EScript VM.
 *
 *  <p> Stub classes should implement well-known interfaces for external linkages.
 *  For example, we can define a interface <code>MyInterface</code> whose class file is
 *  known to both the (external) source code and the (internal) EScript VM.  This provides
 *  the entry points into the Escript VM.
 *
 */
public abstract class EScriptClassLoader extends java.lang.ClassLoader {

    public static class ClassPair {
        public String name ;
        public Class stub ;
        public ClassObject classObject ;
    }

    public void addPath( String path ) {
       paths.addElement( path ) ;
    }

    public String parseClassName( String name ) {
        char[] p = new char[ name.length() ];
        for (int i=0;i<name.length();i++) {
           if ( name.charAt(i) == '.' ) {
                p[i] = java.io.File.separatorChar ;
           }
           else
                p[i] = name.charAt(i) ;
        }
        return new String( p ) ;
    }

    /**
     *  Returns a resolved, generated stub class and an associated ClassObject
     *  internal the the EScriptVM.
     */
    public ClassPair findClassAndStub( String name ) {


        return null ;
    }

    /**
     *  Reload class associated with <code>name</code>.  The old
     *  stub class is marked as dead and all listeners are notified
     *  that references to instances of the stub class are to be discarded.
     *
     *  @return The ClassPair of the class
     */
    public ClassPair reloadClassAndStub( String name ) {
        return null ;
    }

    public ClassObject getInternalClassForClass( Class clazz ) {
        return ( ClassObject ) classObjectTable.get( clazz ) ;
    }

    public Class findClass(String name) {
        // See if the class isn't already loaded
        Class c = ( Class ) cache.get( name ) ;

        if ( c != null ) {
            return c ;
        }

        // Break the name into path names
        String p = parseClassName( name ) ;
        File path = null ;

        // Search the current user defined class path.
        for (int i=0;i<paths.size();i++) {
            String pathName = paths.elementAt(i) + p ;
            path = new File( pathName ) ;
            if ( path.exists() && path.isFile() )
                break ;
            else
                path = null ;
        }

        if ( path == null )
            return null ;

        byte[] b = loadClassData( path);

        // Process this class by creating a new "stub".  Give it
        // a different name to distinguish it from any existing
        // class of the same name.
        ByteArrayInputStream bs = new ByteArrayInputStream( b ) ;
        DataInputStream ds = new DataInputStream( bs ) ;
        ClassObject classObject = new ClassObject() ;
        try {
            classObject.read( ds ) ;
        }
        catch ( IOException e ) {
            return null ;
        }

        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassData( File classFile ) {

        FileInputStream fs ;
        try {
        fs = new FileInputStream( classFile ) ;
        byte[] buf = new byte[ fs.available() ];

        fs.read( buf ) ;
        fs.close() ;
        return buf ;
        }
        catch ( IOException e ) {
            e.printStackTrace() ;
            return null ;
        }

    }

    Vector paths = new Vector();
    Hashtable cache = new Hashtable() ;
    Hashtable classObjectTable = new Hashtable() ;
}
