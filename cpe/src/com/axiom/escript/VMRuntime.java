package com.axiom.escript ;
import java.util.Hashtable ;

public class VMRuntime {

    public static class ThreadInfo {
        Thread thisThread ;
    }

    public static class StackFrame {
        void pushOpStackItem( FrameEntry item ) {
            opstack[opstackBottom++] = item ;
        }

        FrameEntry popOpStackItem() {
            return opstack[--opstackBottom];
        }

        /** Entries to the frame. */
        FrameEntry[] entries ;
        FrameEntry[] opstack ;

        int opstackBottom = 0;
        int entryBottom = 0;

        MethodInfo method ;

        /** External "stub" object, visible to the external VM. */
        Object extern ;

        /** Class of stub object. */
        /** Internal JObject representation */
        JObject intern ;
        int pc = 0 ;
    }

    public static class JObject {
        Object[] fields ;
    }

    public static abstract class FrameEntry {
        int tag ;
    }

    public static class BooleanEntry extends FrameEntry {
        public BooleanEntry(boolean value) {
            tag = SigPrimitive.SIG_JBOOLEAN ;
            this.value = value ;
        }
        boolean value ;
    }

    public static class ByteEntry extends FrameEntry {
        public ByteEntry( int value ) {
           tag = SigPrimitive.SIG_JBYTE ;
        }
        byte value ;
    }

    public static class CharEntry extends FrameEntry {
        public CharEntry( int value ) {
            tag = SigPrimitive.SIG_JCHAR ;
            this.value = ( char ) value ;
        }
        char value ;
    }

    public static class IntEntry extends FrameEntry {
        public IntEntry( int value ) {
            tag = SigPrimitive.SIG_JINT ;
            this.value = value ;
        }
        int value ;
    }

    public static class ObjectEntry extends FrameEntry {
        public ObjectEntry( Object value ) {
            tag = SigPrimitive.SIG_JOBJECT ;
            this.value = value ;
        }
        Object value ;
    }

    static void callMethod( StackFrame frame ) {

    }

    static void interploop( StackFrame frame ) {

        while ( true ) {
           byte c = frame.method.code.code[ frame.pc++];
           // Massive switch statement for each bytecode

        }
    }

    /**
     *  Enter the VM with a method call, including a set of parameters.
     *
     *  @param stub  Object for which method will be called.
     *  @param params Array of frame entry parameters.
     *  @param info  Method to call on stub.
     */
    public static void entryPoint( Object stub, FrameEntry[] params, int methodIndex ) {
        //StackFrame frame = new StackFrame() ;
        //frame.entries = new FrameEntry[ info.code.maxLocals ] ;
        //frame.opstack = new FrameEntry[ info.code.maxStack ];
        //System.arraycopy( params, 0, frame.entries, 0, params.length ) ;
        //frame.extern = stub ;

        // Map the stub object into the appropriate classObject
        // ClassObject co = EScriptClassLoader.getInternalClassForClass( stub.getClass() ) ;
        // Resolve the method index into a
        // MethodInfo mi = co.methodInfo[methodIndex] ;
    }

    public static ClassObject getClassObjectForStubClass( Class stub ) {
        return ( ClassObject ) classObjectTable.get( stub ) ;
    }

    public static void putClassObjectForStubClass( Class stub, ClassObject cl ) {
        classObjectTable.put( stub, cl ) ;
    }

    static Hashtable classObjectTable = new Hashtable() ;
}