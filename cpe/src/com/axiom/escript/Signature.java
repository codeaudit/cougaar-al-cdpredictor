package com.axiom.escript ;

public abstract class Signature {

    public interface SigType {
        public static final int SIG_METHOD = 0;
        public static final int SIG_CLASS = 1;
        public static final int SIG_PRIM = 2 ;
        public static final int SIG_ARRAY = 3 ;
    }

    static class SigParser {
        SigParser( String s ) {
            this.s = s ;
        }

        public Signature parsePrimitive( ) {
            int result ;
            char c = s.charAt(count++) ;
            switch ( c ) {
                case 'B':
                result = SigPrimitive.SIG_JBOOLEAN ;
                break ;
                case 'C' :
                result = SigPrimitive.SIG_JCHAR ;
                break ;
                case 'D' :
                result = SigPrimitive.SIG_JDOUBLE ;
                break ;
                case 'I' :
                result = SigPrimitive.SIG_JINT ;
                break ;
                case 'J' :
                result = SigPrimitive.SIG_JLONG ;
                break ;
                case 'L' :
                result = SigPrimitive.SIG_JOBJECT ;
                break ;
                case 'S' :
                result = SigPrimitive.SIG_JSHORT ;
                break ;
                case 'Z' :
                result = SigPrimitive.SIG_JBOOLEAN ;
                break ;
                case 'V' :
                result = SigPrimitive.SIG_JVOID ;
                break ;
                default:
                throw new RuntimeException( "Invalid primitive identifier "+ s );
            }
            return new PrimitiveSig( result ) ;
        }

        Signature parseMethod() {
            MethodSig ms = new MethodSig() ;

            char c = s.charAt(count) ;
            if ( c != '(' ) throw new RuntimeException( "Method descriptor expected." ) ;
            
            count++;
            c = s.charAt(count) ;
            java.util.Vector v = new java.util.Vector() ;
            while ( c != ')' ) {
               Signature r = parse() ;
               v.addElement( r ) ;
               c = s.charAt(count) ;
            }
            ms.params = new Signature[v.size()];
            for(int i=0;i<v.size();i++) {
                ms.params[i] = ( Signature ) v.elementAt(i) ;
            }

            count++ ;
            ms.retval = parse() ;
            return ms ;
        }

        Signature parseClass() {
            char c = s.charAt(count) ;

            if ( c != 'L' ) throw new RuntimeException( "Class descriptor expected." ) ;
            count++;
            int end = s.indexOf(';',count) ;

            ClassSig cs = new ClassSig() ;
            cs.className = s.substring( count, end ) ;
            count = end + 1;
            return cs ; 
        }

        Signature parseArray() {
            ArraySig as = new ArraySig() ;

            char c = s.charAt(count) ;
            if ( c != '[' ) throw new RuntimeException( "Array descriptor expected." ) ;
            count++;
            as.subtype = parse() ;
            return as ;
        }

        Signature parse() {
            char c = s.charAt(count) ;
            switch( c ) {
                case '[' :
                    return parseArray() ;
                case 'L' :
                    return parseClass() ;
                case '(' :
                    return parseMethod() ;
                default :
                    return parsePrimitive() ;
            }
        }

        int count = 0 ;
        String s ;
    }

    public static class MethodSig extends Signature {
        public MethodSig() {
            this.tag = SigType.SIG_METHOD ;
        }
        Signature retval ;
        Signature[] params ;
    }

    public static class ArraySig extends Signature {
        public ArraySig() {
            this.tag = SigType.SIG_ARRAY ;
        }
        Signature subtype ;
    }

    public static class ClassSig extends Signature {
        public ClassSig() {
            this.tag = SigType.SIG_CLASS ;
        }
        String className ;
    }

    public static class PrimitiveSig extends Signature {
        public PrimitiveSig( int primType ) {
            this.tag = SigType.SIG_PRIM ;
            this.primType = primType ;
        }
        int primType ;
    }

    int tag ;

    public static void main( String[] args ) {
        SigParser sp = new SigParser( "(IDLjava/lang/Thread;)V" ) ;
        Signature s = sp.parse() ;
    }
}