package com.axiom.lib.util;
import java.util.*;

/** A growable, indexable group of connection objects.  In a concession to 
 *  speed, this was implemented in lieu of using <code>java.util.Vector</code>.
 */

public class IntVector implements java.io.Serializable, Cloneable {
    /**
     * Constructs an empty IntVector with the specified initial capacity and
     * capacity increment. 
     *
     * @param   initialCapacity     the initial capacity of the IntVector.
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the IntVector overflows.
     */
    public IntVector(int initialCapacity, int capacityIncrement) {
	super();
	this.elementData = new int[initialCapacity];
	this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty IntVector with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the IntVector.
     * @since   JDK1.0
     */
    public IntVector(int initialCapacity) {
	this(initialCapacity, 0);
    }

    /**
     * Constructs an empty IntVector. 
     *
     * @since   JDK1.0
     */
    public IntVector() {
	this(14);
    }

    /**
     * Copies the components of this IntVector into the specified array. 
     * The array must be big enough to hold all the objects in this  IntVector.
     *
     * @param   anArray   the array into which the components get copied.
     * @since   JDK1.0
     */
    public final synchronized void copyInto(int anArray[]) {
	int i = elementCount;
	while (i-- > 0) {
	    anArray[i] = elementData[i];
	}
    }

    /**
     * Trims the capacity of this IntVector to be the IntVector's current 
     * size. An application can use this operation to minimize the 
     * storage of a IntVector. 
     *
     * @since   JDK1.0
     */
    public final synchronized void trimToSize() {
	int oldCapacity = elementData.length;
	if (elementCount < oldCapacity) {
	    int oldData[] = elementData;
	    elementData = new int[elementCount];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * Increases the capacity of this IntVector, if necessary, to ensure 
     * that it can hold at least the number of components specified by 
     * the minimum capacity argument. 
     *
     * @param   minCapacity   the desired minimum capacity.
     * @since   JDK1.0
     */
    public final synchronized void ensureCapacity(int minCapacity) {
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    int oldData[] = elementData;
	    int newCapacity = (capacityIncrement > 0) ?
		(oldCapacity + capacityIncrement) : (oldCapacity * 2);
    	    if (newCapacity < minCapacity) {
		newCapacity = minCapacity;
	    }
	    elementData = new int[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * Sets the size of this IntVector. If the new size is greater than the 
     * current size, new <code>null</code> items are added to the end of 
     * the IntVector. If the new size is less than the current size, all 
     * components at index <code>newSize</code> and greater are discarded.
     *
     * @param   newSize   the new size of this IntVector.
     */
    public final synchronized void setSize(int newSize) {
	if (newSize > elementCount) {
	    ensureCapacity(newSize);
	} else {
	    for (int i = newSize ; i < elementCount ; i++) {
		elementData[i] = 0;
	    }
	}
	elementCount = newSize;
    }

    /**
     * Returns the current capacity of this IntVector.
     *
     * @return  the current capacity of this IntVector.
     * @since   JDK1.0
     */
    public final int capacity() {
	return elementData.length;
    }
    
    
    /** Returns the connection array for speed.
     */
    public final int[] getInts() {
       return elementData ;   
    }

    /**
     * Returns the number of components in this IntVector.
     *
     * @return  the number of components in this IntVector.
     * @since   JDK1.0
     */
    public final int size() {
	return elementCount;
    }

    /**
     * Tests if this IntVector has no components.
     *
     * @return  <code>true</code> if this IntVector has no components;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public final boolean isEmpty() {
	return elementCount == 0;
    }

    /**
     * Returns an enumeration of the components of this IntVector.
     *
     * @return  an enumeration of the components of this IntVector.
     * @see     java.util.Enumeration
     */
     
    /**
    public final synchronized Enumeration elements() {
	return new IntVectorEnumerator(this);
    }
    */
    
    /**
     * Tests if the specified int is a component in this IntVector.
     *
     * @param   elem   an int.
     * @return  <code>true</code> if the specified int is a component in
     *          this IntVector; <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public final boolean contains(int elem) {
	return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <code>equals</code> method. 
     *
     * @param   elem   an int.
     * @return  the index of the first occurrence of the argument in this
     *          IntVector; returns <code>-1</code> if the int is not found.
     * @see     java.lang.int#equals(java.lang.Object)
     * @since   JDK1.0
     */
    public final int indexOf(int elem) {
	return indexOf(elem, 0);
    }
    
    
    /** Find a connection with the same indices as <code>conn</code>.
     */
    
    public int findInt( int element ) {
        for (int i=0;i<size();i++) {
           if ( elementData[i] == element )
               return i ;
        }
        return -1 ;
    }
        
    /**
     *  Remove connection from sending unit to receiving unit.
     */
    
    public boolean removeInt( int element ) {
        for (int i=0;i<size();i++) {
           if ( elementData[i] == element ) {
               removeAt(i) ;
               return true ;
           }
        }
        return false ;    
    }
    

    /**
     * Searches for the first occurence of the given argument, beginning 
     * the search at <code>index</code>, and testing for equality using 
     * the <code>equals</code> method. 
     *
     * @param   elem    an int.
     * @param   index   the index to start searching from.
     * @return  the index of the first occurrence of the int argument in
     *          this IntVector at position <code>index</code> or later in the
     *          IntVector; returns <code>-1</code> if the int is not found.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @since   JDK1.0
     */
    public final synchronized int indexOf(int elem, int index) {
	for (int i = index ; i < elementCount ; i++) {
	    if (elem == elementData[i] ) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified int in
     * this IntVector.
     *
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified int in
     *          this IntVector; returns <code>-1</code> if the int is not found.
     * @since   JDK1.0
     */
    public final int lastIndexOf(int elem) {
	return lastIndexOf(elem, elementCount-1);
    }

    /**
     * Searches backwards for the specified int, starting from the 
     * specified index, and returns an index to it. 
     *
     * @param   elem    the desired component.
     * @param   index   the index to start searching from.
     * @return  the index of the last occurrence of the specified int in this
     *          IntVector at position less than <code>index</code> in the IntVector;
     *          <code>-1</code> if the int is not found.
     * @since   JDK1.0
     */
    public final synchronized int lastIndexOf(int elem, int index) {
	for (int i = index ; i >= 0 ; i--) {
	    if (elem == elementData[i]) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Returns the component at the specified index.
     *
     * @param      index   an index into this IntVector.
     * @return     the component at the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if an invalid index was
     *               given.
     * @since      JDK1.0
     */
    public final synchronized int at(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
	}
	/* Since try/catch is free, except when the exception is thrown,
	   put in this extra try/catch to catch negative indexes and
	   display a more informative error message.  This might not
	   be appropriate, especially if we have a decent debugging
	   environment - JP. */
	try {
	    return elementData[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    throw new ArrayIndexOutOfBoundsException(index + " < 0");
	}
    }

    /**
     * Returns the first component of this IntVector.
     *
     * @return     the first component of this IntVector.
     * @exception  NoSuchElementException  if this IntVector has no components.
     * @since      JDK1.0
     */
    public final synchronized int firstElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[0];
    }

    /**
     * Returns the last component of the IntVector.
     *
     * @return  the last component of the IntVector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @exception  NoSuchElementException  if this IntVector is empty.
     * @since   JDK1.0
     */
    public final synchronized int lastElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[elementCount - 1];
    }

    /**
     * Sets the component at the specified <code>index</code> of this 
     * IntVector to be the specified int. The previous component at that 
     * position is discarded. 
     * <p>
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the IntVector. 
     *
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.IntVector#size()
     * @since      JDK1.0
     */
    public final synchronized void setAt(int obj, int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	elementData[index] = obj;
    }

    /**
     * Deletes the component at the specified index. Each component in 
     * this IntVector with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. 
     * <p>
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the IntVector. 
     *
     * @param      index   the index of the int to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.IntVector#size()
     * @since      JDK1.0
     */
    public final synchronized void removeAt(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	else if (index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	int j = elementCount - index - 1;
	if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
	}
	elementCount--;
	// elementData[elementCount] = null; /* to let gc do its work */
    }

    /**
     * Inserts the specified int as a component in this IntVector at the 
     * specified <code>index</code>. Each component in this IntVector with 
     * an index greater or equal to the specified <code>index</code> is 
     * shifted upward to have an index one greater than the value it had 
     * previously. 
     * <p>
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than or equal to the current size of the IntVector. 
     *
     * @param      obj     the component to insert.
     * @param      index   where to insert the new component.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        java.util.IntVector#size()
     * @since      JDK1.0
     */
    public final synchronized void insertAt(int obj, int index) {
	if (index >= elementCount + 1) {
	    throw new ArrayIndexOutOfBoundsException(index
						     + " > " + elementCount);
	}
	ensureCapacity(elementCount + 1);
	System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
	elementData[index] = obj;
	elementCount++;
    }

    /**
     * Adds the specified component to the end of this IntVector, 
     * increasing its size by one. The capacity of this IntVector is 
     * increased if its size becomes greater than its capacity. 
     *
     * @param   obj   the component to be added.
     * @since   JDK1.0
     */
    public final synchronized void add(int obj) {
	ensureCapacity(elementCount + 1);
	elementData[elementCount++] = obj;
    }

    /**
     * Removes the first occurrence of the argument from this IntVector. If 
     * the int is found in this IntVector, each component in the IntVector 
     * with an index greater or equal to the int's index is shifted 
     * downward to have an index one smaller than the value it had previously.
     *
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          IntVector; <code>false</code> otherwise.
     */
    public final synchronized boolean remove(int obj) {
	int i = indexOf(obj);
	if (i >= 0) {
	    removeAt(i);
	    return true;
	}
	return false;
    }

    /**
     * Removes all components from this IntVector and sets its size to zero.
     *
     */
    public final synchronized void removeAll() {
        elementCount = 0;
    }

    /**
     * Returns a clone of this IntVector.
     *
     * @return  a clone of this IntVector.
     */
    public synchronized Object clone() {
	try { 
	    IntVector v = (IntVector)super.clone();
	    v.elementData = new int[elementCount];
	    System.arraycopy(elementData, 0, v.elementData, 0, elementCount);
	    return v;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    /**
     * Returns a string representation of this IntVector. 
     *
     * @return  a string representation of this IntVector.
     * @since   JDK1.0
     */
    public final synchronized String toString() {
	int max = size() - 1;
	StringBuffer buf = new StringBuffer();
	buf.append("[");

	for (int i = 0 ; i <= max ; i++) {
	    String s = "" + elementData[i];
	    buf.append(s);
	    if (i < max) {
		buf.append(", ");
	    }
	}
	buf.append("]");
	return buf.toString();
    }    
    
    
    //
    // Protected fields.
    //
    
    /**
     * The array buffer into which the components of the IntVector are 
     * stored. The capacity of the IntVector is the length of this array buffer.
     *
     */
    protected int elementData[];

    /**
     * The number of valid components in the IntVector. 
     *
     */
    protected int elementCount;

    /**
     * The amount by which the capacity of the IntVector is automatically 
     * incremented when its size becomes greater than its capacity. If 
     * the capacity is <code>0</code>, the capacity of the IntVector is 
     * doubled each time it needs to grow. 
     *
     */
    protected int capacityIncrement;

    protected static final long serialVersionUID = 1L;

}