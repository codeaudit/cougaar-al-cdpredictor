package com.axiom.escript ;
import java.io.DataInputStream ;
import java.io.* ;

/**
 *  Represents a single class loaded into memory.
 */
public class ClassObject {

    public static final int MAGIC = 0xcafebabe ;
    public static final int ACC_PUBLIC    = 0x0001 ;
    public static final int ACC_PRIVATE   = 0x0002 ;
    public static final int ACC_PROTECTED = 0x0004 ;
    public static final int ACC_STATIC    = 0x0008 ;
    public static final int ACC_FINAL     = 0x0010 ;
    public static final int ACC_SYNCHRONIZED = 0x0020 ;
    public static final int ACC_VOLATILE  = 0x0040 ;
    public static final int ACC_TRANSIENT = 0x0080 ;
    public static final int ACC_NATIVE    = 0x0100 ;
    public static final int ACC_ABSTRACT = 0x0400 ;

    protected void writeConstant( DataOutput dos, ConstantInfo info ) throws IOException {
        switch ( info.tag ) {
            case ConstantInfo.CONSTANT_CLASS :
            {
                ClassConstantInfo ci = ( ClassConstantInfo ) info ;
                dos.writeShort( ci.index );
            }
            break ;
            case ConstantInfo.CONSTANT_INTERFACEMETHODREF :
            case ConstantInfo.CONSTANT_FIELDREF :
            case ConstantInfo.CONSTANT_METHODREF :
            {
                RefConstantInfo ri = ( RefConstantInfo ) info ;
                dos.writeShort( ri.classIndex );
                dos.writeShort( ri.nameAndTypeIndex ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_DOUBLE :
            {
                DoubleConstantInfo di = ( DoubleConstantInfo ) info ;
                dos.writeDouble( di.value );
            }
            break ;
            case ConstantInfo.CONSTANT_FLOAT :
            {
                FloatConstantInfo fi = ( FloatConstantInfo ) info ;
                dos.writeFloat( fi.value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_INTEGER :
            {
                IntegerConstantInfo ii = ( IntegerConstantInfo ) info ;
                dos.writeInt( ii.value );
            }
            break ;
            case ConstantInfo.CONSTANT_LONG :
            {
                LongConstantInfo li = ( LongConstantInfo ) info ;
                dos.writeLong( li.value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_STRING :
            {
                StringConstantInfo si = ( StringConstantInfo ) info ;
                dos.writeShort( ( short ) si.stringIndex ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_UTF8 :
            {
                UTF8ConstantInfo ui = ( UTF8ConstantInfo ) info ;
                dos.writeShort( ( short ) ui.stringValue.length() );
                ClassReaderUtils.writeUTF( dos, ui.stringValue );
            }
            break ;
            case ConstantInfo.CONSTANT_NAMEANDTYPE :
            {
                NameAndTypeInfo ni = ( NameAndTypeInfo ) info ;
                dos.writeShort( ( short ) ni.nameIndex ) ;
                dos.writeShort( ( short ) ni.typeIndex ) ;
            }
            break ;
            default:
                throw new IOException( "Exception writing constant." ) ;
        }
    }

    public void write( DataOutput dos ) throws IOException {
        dos.writeInt(magic);
        dos.writeShort(minorversion);
        dos.writeShort(majorversion);
        dos.writeShort( ( short ) constantPoolCount ) ;
        int i = 1 ;

        // Write constant pool
        while ( i<constantPool.length ) {
            ConstantInfo info = constantPool[i] ;
            writeConstant( dos, info ) ;
            if ( info.tag == ConstantInfo.CONSTANT_DOUBLE ||
                 info.tag == ConstantInfo.CONSTANT_LONG ) {
                i+= 2 ;
            }
            else
                i++ ;
        }

        dos.writeShort( accessFlags );
        dos.writeShort( thisClass ) ;
        dos.writeShort( superClass ) ;
    }

    public void read( DataInput ds ) throws IOException {
        magic = ds.readInt() ;
        minorversion = ds.readUnsignedShort() ;
        majorversion = ds.readUnsignedShort() ;
        constantPoolCount = ds.readUnsignedShort() ;

        constantPool = new ConstantInfo[constantPoolCount];
        int i = 1 ;
        while (i<constantPoolCount) {
            ConstantInfo info = readConstant( ds ) ;

            constantPool[i] = info ;
            if ( info.tag == ConstantInfo.CONSTANT_DOUBLE ||
                 info.tag == ConstantInfo.CONSTANT_LONG ) {
                i+= 2 ;
            }
            else
                i++ ;
        }

        this.accessFlags = ds.readUnsignedShort() ;
        thisClass = ds.readUnsignedShort() ;
        classInfo = ( ( ClassConstantInfo ) constantPool[thisClass] );
        this.superClass = ds.readUnsignedShort() ;

        // Read interfaces
        int interfacesCount = ds.readUnsignedShort() ;
        interfaces = new int[interfacesCount];
        for (int j=0;j<interfacesCount;j++) {
            interfaces[j] = ds.readUnsignedShort() ;
        }

        // Read field info
        int fieldCount = ds.readUnsignedShort() ;
        fields = new FieldInfo[fieldCount];
        for (int j=0;j<fieldCount;j++) {
            fields[j] = readFieldInfo( ds ) ;
        }

        // Read methods
        int methodCount = ds.readUnsignedShort() ;
        methods = new MethodInfo[methodCount];
        for (int j=0;j<methodCount;j++) {
            methods[j] = readMethodInfo( ds ) ;
        }

        // Read class attributes
        int attributesCount = ds.readUnsignedShort() ;
        attributes = new AttributeInfo[attributesCount];

        for (int j=0;j<attributesCount;j++) {
            int nameIndex = ds.readUnsignedShort() ;
            int length = ds.readInt() ;
            UTF8ConstantInfo uinfo = ( UTF8ConstantInfo ) constantPool[nameIndex] ;

            if ( uinfo.stringValue.equals( "InnerClasses" ) ) {
                InnerClassesAttribute ia = new InnerClassesAttribute() ;
                ia.read( this, ds );
                attributes[j] = ia ;
            }
            else {
                ds.skipBytes( length ) ;
            }
        }

    }

    public ClassConstantInfo getClassConstantInfo() { return classInfo ; }

    public ConstantInfo resolveConstant( int index ) {
        return this.constantPool[ index ] ;
    }

    protected FieldInfo readFieldInfo( DataInput ds ) throws java.io.IOException {
        FieldInfo info = new FieldInfo() ;
        info.read( this, ds ) ;
        return info ;
    }

    protected MethodInfo readMethodInfo( DataInput ds ) throws java.io.IOException {
        MethodInfo info = new MethodInfo() ;
        info.read( this, ds ) ;
        return info ;
    }

    protected ConstantInfo readConstant( DataInput ds ) throws IOException {

        int tag = ds.readUnsignedByte() ;

        ConstantInfo info = null ;

        switch ( tag ) {
            case ConstantInfo.CONSTANT_CLASS :
            {
                int index = ds.readUnsignedShort() ;
                info = new ClassConstantInfo( index ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_INTERFACEMETHODREF :
            case ConstantInfo.CONSTANT_FIELDREF :
            case ConstantInfo.CONSTANT_METHODREF :
            {
                int index1 = ds.readUnsignedShort() ;
                int index2 = ds.readUnsignedShort() ;
                info = new RefConstantInfo( tag, index1, index2 ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_DOUBLE :
            {
                double value = ds.readDouble() ;
                info = new DoubleConstantInfo( value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_FLOAT :
            {
                float value = ds.readFloat() ;
                info = new FloatConstantInfo( value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_INTEGER :
            {
                int value = ds.readInt() ;
                info = new IntegerConstantInfo( value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_LONG :
            {
                long value = ds.readLong() ;
                info = new LongConstantInfo( value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_STRING :
            {
                int index = ds.readUnsignedShort() ;
                info = new StringConstantInfo( index ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_UTF8 :
            {
                int count = ds.readUnsignedShort() ;
                String value = ClassReaderUtils.readUTF( ds, count ) ;
                info = new UTF8ConstantInfo( value ) ;
            }
            break ;
            case ConstantInfo.CONSTANT_NAMEANDTYPE :
            {
                int nameIndex = ds.readUnsignedShort() ;
                int typeIndex = ds.readUnsignedShort() ;
                info = new NameAndTypeInfo( nameIndex, typeIndex ) ;
            }
            break ;
            default:
                throw new IOException( "Exception reading constant." ) ;

        }
        return info ;
    }

    public void paramString( StringBuffer buf ) {

    }

    int magic ;
    int majorversion ;
    int minorversion ;
    int constantPoolCount ;

    ConstantInfo[] constantPool ;

    int accessFlags ;
    int thisClass ;
    int superClass ;

    int[] interfaces ;

    FieldInfo[] fields ;

    MethodInfo[] methods ;

    AttributeInfo[] attributes ;

    String sourceFile ;

    ClassConstantInfo classInfo ;

    public static void main( String[] args ) {
        try {
        FileInputStream fs =
            new FileInputStream( "D:\\axiom\\com\\axiom\\escript\\ClassObject.class" ) ;
        DataInputStream ds = new DataInputStream( fs ) ;
        ClassObject clazz = new ClassObject() ;
        clazz.read(ds) ;
        System.out.println( clazz ) ;
        }
        catch ( IOException e ) {
            System.out.println( e ) ;
        }
    }
}