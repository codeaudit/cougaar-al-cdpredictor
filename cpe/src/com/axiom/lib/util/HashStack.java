package com.axiom.lib.util ;

import java.util.Hashtable ;

/**
 *  Implements a hash stack, in which objects in the stack are accessible
 *  through a key. It is useful for caching because it
 *  it possible to move an item from anywhere in the stack to the top
 *  or bottom quickly by accessing it via a key.
 */
public class HashStack implements java.io.Serializable {
    public HashStack( int maxSize ) {
        this.maxStackSize = maxSize ;
    }
    
    public HashStack() {   
    }

    static class StackItem implements java.io.Serializable {
        StackItem( Object key, Object o ) {
          this.key = key ;
          this.obj = o ;
        }
        
        Object key ;
        
        Object obj ;
        
        StackItem next = null ;
        StackItem prev = null ;

        static final long serialVersionUID = -5644161508980760731L ;
    }
    
    /**
     *   Sets the maximum stack size.  If the stack size is less than the
     *   current stack size, the last items are discarded from the stack.
     */     
    public void setMaxSize( int size ) {
        this.maxStackSize = size ;
        if ( hashTable.size() > maxStackSize ) {
            StackItem cursor = last ;
            long count = hashTable.size() - maxStackSize ;
            while ( true ) {
               if ( cursor == null || count == 0 )
                   break ;
               hashTable.remove( cursor.key ) ;
               cursor = cursor.prev ;
               count--;
            }
            last = cursor ;
        }
    }

    public long getMaxSize() {
        return maxStackSize ;
    }

    public int getSize() {
        return hashTable.size() ;
    }
    
    /**
     *  Convert to string form.
     */
    public String toString() {
        StringBuffer result = new StringBuffer() ;
        StackItem cursor = first ;
        while ( cursor != null ) {
            result.append( cursor.obj.toString() + " " ) ;
            cursor = cursor.next ;
        }
        return result.toString() ;
    }
       
    /**
     *  Add an object.  If it exists, do nothing and return false.  If it does not
     *  exist, add it to the top of the stack and grow the stack by one.  If the
     *  stack exceeds the maximum stack size, the bottom item on
     *  the stack is removed.
     */
    public synchronized boolean add( Object key, Object item ) {
        StackItem s = (StackItem) hashTable.get( key ) ;
        if ( s == null ) {
            // Insert at beginning
            addFirst( key, item ) ;
            hashTable.put( key, first ) ;
            if ( hashTable.size() > maxStackSize ) {
               removeLast() ;   
            }
            return true ;
        }
       
        return false ;
    }
    
    public synchronized Object remove( Object key ) {
        StackItem s = ( StackItem ) hashTable.remove( key ) ;
        if ( s != null ) {
           remove( s ) ;
           return s.obj ;
        }
        return null ;
    }

    public synchronized void clear() {
        hashTable.clear() ;
        first = last = null ;
    }
    
    /**
     *  Get an object with key.
     */
    public synchronized Object get( Object key ) {
       StackItem s = ( StackItem ) hashTable.get( key );
       if ( s == null )
          return null ;
       return s.obj ;
    }

    /**
     *  Remove the last (bottom) item on the stack.
     */
    public synchronized void removeLast() {
        if ( last != null ) {
            hashTable.remove( last.key ) ;
            remove( last ) ;
        }
    }    

    /* If the object exists, it is moved to
     * the top of the stack.
     */
    public synchronized Object touch( Object key ) {
       StackItem s = ( StackItem ) hashTable.get( key );
       if ( s == null )
          return null ;
       remove( s ) ;  // Remove from list
       addFirst( s ) ;  // Add add beginning of list 
       return s.obj ;        
    }
        
    protected void addFirst( Object key, Object o ) {
        StackItem s = new StackItem( key, o );
        if ( first == null ) {
            first = s ;
            last = first ;
        }
        else {
            first.prev = s ;
            s.next = first ;
            first = s ;
        }
    }
    
    protected void addFirst( StackItem s ) {
        s.next = first ;
        s.prev = null ;
        first = s;
        if ( last == null )
           last = first ;
    }

    protected void remove( StackItem s ) {
        if ( s.prev != null ) {
           s.prev.next = s.next ;
        } 
        if ( s.next != null ) {
           s.next.prev = s.prev ;   
        }
        if ( first == s ) {
           first = s.next ;   
        }
        if ( last == s ) {
           last = s.prev ;   
        }
        // For the sake of safety, in case (for some bizarre reason) s is going to be reused
        s.next = null ;
        s.prev = null ;
    }

    public Object getFirst() {
        return first.obj ;
    }

    public Object getLast() {
        return last.obj ;
    }

    public Object getFirstKey() {
        return first.key ;
    }

    public Object getLastKey() {
        return last.key ;
    }
    
    protected void addLast( Object key, Object o ) {
        if ( first == null ) {
            first = new StackItem( key, o ) ;
            last = first ;
        }
        else {
            StackItem s = new StackItem( key, o );
            last.next = s ;
            s.prev = last ;
            last = s;
        }
        
    }

    public static void main( String[] argv ) {
        String moose = "moose", mouse = "mouse", meese = "meese", elk = "elk";
        
        HashStack hs = new HashStack( 3 ) ;
        
        hs.add( moose, moose ) ;
        hs.add( mouse, mouse ) ;
        hs.add( meese, meese ) ;
        
        System.out.println( "Getting mouse : " + hs.get( mouse ) ) ;
        
        System.out.println( "Stack: \n" + hs + "\n" ) ;
        
        hs.touch( mouse ) ;
        
        System.out.println( "\nStack: \n" + hs + "\n" ) ;
        
        hs.add( elk, elk ) ;
        System.out.println( "\nStack: \n" + hs + "\n" ) ;
        
        Object o = hs.get( moose ) ;
    }

    /**
     *  Reference to first object in the linked list, corresponding to the
     *  top of the stack.
     */
    protected StackItem first ;
    
    /**
     *  Reference to the last object in the linked list, corresponding to the
     *  bottom of the stack.
     */
    protected StackItem last ;
   
    /**
     *  The maximum stack size.  By default, it is set to Long.MAX_VALUE, which is
     *  essentially infinite, since the count of items in the Hashtable cannot
     *  exceed Integer.MAX_VALUE.
     */
    protected long maxStackSize = Long.MAX_VALUE ;
    
    protected Hashtable hashTable = new Hashtable() ;

     static final long serialVersionUID = 5372076969231110327L;
}