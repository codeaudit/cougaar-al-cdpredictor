package com.axiom.lib.util;
import java.util.*;

/** Compares two strings.
 *
 */

class StringComparator implements Comparator {
  public int compare( Object obj1, Object obj2 ) {
    
  //  if ( !(obj1 instanceof String ) ) return -1;
  //  if ( !(obj2 instanceof String ) ) return -1;
    String str1 = obj1.toString();
    String str2 = obj2.toString();
    return str1.compareTo( str2 );    

  /*
    int minlength = (str1.length()< str2.length())? str1.length(): str2.length();
    for (int i=0;i< minlength;i++) {
      if ( str1.charAt(i) < str2.charAt(i) )
         return -1;
      else if ( str1.charAt(i) > str2.charAt(i) )
         return 1;
    }
    if ( str1.length() < str2.length() )
       return -1;
    else if ( str1.length() > str2.length() )
       return 1;
    else
       return 0;
    */
  }
}

class StringEqComparator implements Comparator {
  public int compare( Object obj1, Object obj2 ) {
    if ( !(obj1 instanceof String ) ) return -1;
    if ( !(obj2 instanceof String ) ) return -1;

    if ( obj1.equals( obj2 ) )
       return 0;
    else
       return 1;
  }
}

class EqualsComparator implements Comparator {
    public int compare( Object obj1, Object obj2 ) {
       if ( obj1.equals( obj2 ) ) 
          return 0 ;
       else
          return 1 ;
    }
}

/** Generalized set which extends the Vector class. Supports union, intersect, sort, etc.
 * operations.  This class will be switched to extend the RogueWave class Dlist if needed.
 *
 * @author   Wilbur S. Peng
 * @version  0.01
 */

public class GenSet extends Vector implements Sortable {
  protected static final long serialVersionUID = 1L;
    
  public static Comparator equalsComparator = new EqualsComparator() ;
  public static Comparator stringComparator = new StringComparator();
  public static Comparator stringEqComparator = new StringEqComparator();
  
  public GenSet() {}
  
  public GenSet( int size, int inc ) {
    super( size, inc ); 
  }
  
  public GenSet( Object obj ) { insert(obj) ; }

  /** Behaves the same as the RogueWave DList.insert.  Puts object at end of list
   *
   */
   
  public Object insert( Object obj ) {
     addElement( obj );
     return obj;
  }

  /** Behaves same as RogueWave DList.append.  Puts object at end of list.
   *
   */

  public Object append( Object obj ) {
     addElement( obj );
     return obj;
  }

  /** Behaves same as RogueWave DList.prepend.  Puts object at front of list.
   *
   */

  public Object prepend( Object obj ) {
     insertElementAt( obj, 0 );
     return obj ;
  }

  public Object first() {
     if ( size() == 0 ) return null ;
     return elementAt(0);
  }

  public Object removeFirst() {
     if ( size() == 0 ) return null ;
     Object result = elementAt(0);
     removeElementAt(0);
     return result ;
  }

  public Object removeLast() {
     if ( size() == 0 ) return null ;
     Object result = elementAt( size() - 1);
     removeElementAt( size() - 1 );
     return result ;
  }

  public Object insertAt(int i, Object obj) {
     insertElementAt( obj, i );
     return obj ;
  }

  /** Added to emulate RogueWave CollectionBase
   *
   */

  public void removeAll() {
     removeAllElements();
  }

  /** Overrides <tt>clone()</tt> method.  Currently, performs "shallow" copy of this GenSet.
   *
   */

  public Object clone() {
    GenSet result = new GenSet();
    
    for ( Enumeration e=this.elements();e.hasMoreElements();) {
      Object obj = e.nextElement() ;
      result.append( obj );
    }
    return result ;
  } 
  
  /** The two sets are strongly equivalent, e.g. they reference exactly the same
   * memory objects the same number of times.  This implementation could be speeded up significantly.
   *
   */
   
  public boolean strongEquals( GenSet set ) {
     if ( set.size() != size() )
       return false ;

     for ( Enumeration e=set.elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;

       boolean found = false ;
       for ( Enumeration er= elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( obj1 == obj2  ) {
            found = true ;
            break;
         }
       }

       if ( found == false )
          return false ;
     }
  
     for ( Enumeration e=elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;

       boolean found = false ;
       for ( Enumeration er= set.elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( obj1 == obj2 ) {
            found = true ;
            break;
         }
       }

       if ( found == false )
          return false ;
     }

     //Do the reverse
     return true ;
  }

  /** Nondestructive (?) union.  Finds the union of two sets.  Does not 
   *  insure any particular order.  Uses Comparator object to compare each object reference in the set.
   *  Likely to be slow, as it requires for two GenSets with n and m elements each, n*m comparisons.
   *  Duplicates may be removed.
   *
   * @param set GenSet object to be unioned with this object
   * @param comparator Object implementing comparator interface
   * @return resultant union
   */

  public GenSet union(GenSet set, Comparator comparator ) {
     GenSet result = new GenSet();

     for ( Enumeration e=elements();e.hasMoreElements();) {
       result.append( e.nextElement() );
     }

     for ( Enumeration e=set.elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;

       boolean found = false ;
       for ( Enumeration er= elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( comparator.compare( obj1, obj2 ) == 0 ) {
            found = true ;
            break;
         }
       }

       if ( found == false )
         result.append( obj1 );
     }

     return result ;
  }

  /** Find intersection of set with this set.  Also implemented on the slow side.
   *
   * @param comparator Comparator object
   */

  public GenSet intersection(GenSet set, Comparator comparator ) {
     GenSet result = new GenSet();

     for ( Enumeration e=set.elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;

       boolean found = false ;
       for ( Enumeration er= elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( comparator.compare( obj1, obj2 ) == 0 ) {
            found = true ;
            break;
         }
       }

       if ( found == true )
         result.append( obj1 );
     }

     return result ;
  }
  
  /** Difference method.
   *
   */
  
  public GenSet difference( GenSet set, Comparator comparator ) {
     GenSet result = new GenSet();
     
     for ( Enumeration e=set.elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;
       boolean found = false ;
        
       for ( Enumeration er= elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( comparator.compare( obj1, obj2 ) == 0 ) {
            found = true ;
            break;
         }
       }

       if ( found == false )
         result.append( obj1 );
     }
     
     for ( Enumeration e=elements();e.hasMoreElements();) {
       Object obj1 = e.nextElement() ;
       boolean found = false ;
        
       for ( Enumeration er= set.elements(); er.hasMoreElements(); ) {
         Object obj2 = er.nextElement() ;

         if ( comparator.compare( obj1, obj2 ) == 0 ) {
            found = true ;
            break;
         }
       }

       if ( found == false )
         result.append( obj1 );
     }
   
     return result ;
  }

  /** Nondestructive append. Merge this two GenSets simply by appending them together
   * This is slower than the destructive method, <tt>splice</tt>, which has
   * not yet been implemented.
   */

  public GenSet append(GenSet set ) {
     GenSet result = new GenSet();
     
     for (Enumeration e=elements();e.hasMoreElements();) {
        result.append(e.nextElement());
     }
     if (set == null)
       return result ;
     for (Enumeration e= set.elements();e.hasMoreElements();) {
        result.append(e.nextElement());
     }
     
     return result ;
  }
  
  /** Finds objects which are duplicate in this GenSet.
   *
   */
   
  public GenSet getDuplicates( Comparator comparator ) {
     int i=0;
     GenSet results = new GenSet();
     
     for (Enumeration e1= elements();e1.hasMoreElements();) {
        Object obj1 = e1.nextElement();
        int count = 0, j = 0;
        for (Enumeration e2 = elements();e2.hasMoreElements();) {
            Object obj2 = e2.nextElement();
            if ( (i!=j) && ( comparator.compare( obj1, obj2 ) == 0 ))
              if ( i < j ) {
                results.append( obj1 );
                break ;
              }
            j++;
        }
        i++;
     }
     
     return results ;
  }
  
  /** Remove all elements from this GenSet which test as duplicates using Comparator
   *
   */

  public void removeDuplicates( Comparator comparator ) {
     GenSet source = (GenSet) clone();  // Make a clone of this set
     GenSet temp = new GenSet();
     GenSet swap ;
     Object element ;
     
     removeAll();  // Clear this vector

     while ( source.size() > 0 ) {
       element = source.removeFirst();
       this.insert( element );    // Insert in this list
       while ( source.size() > 0 ) {
          Object obj = source.removeFirst();
          if ( comparator.compare( obj, element ) != 0 )
             temp.insert( obj );
       }
       swap = source ;
       source = temp;
       temp = swap;
     }

  }

  /** Determines whether an object obj is a member of this GenSet.
   * This has been deprecated in favor of the <tt>memberOf</tt> method.
   *
   */

  public boolean hasMember( Object obj, Comparator comparator ) {
    for (Enumeration e = elements(); e.hasMoreElements(); ) {
       Object obj1 = e.nextElement();
   
       if ( comparator.compare( obj1, obj ) == 0 )
           return true ;
    }
    return false ;
  }

  /** Determines whether an object obj is a member of this GenSet
   *
   */

  public boolean memberOf( Object obj, Comparator comparator ) {
    for (Enumeration e = elements(); e.hasMoreElements(); ) {
       Object obj1 = e.nextElement();
   
       if ( comparator.compare( obj1, obj ) == 0 )
           return true ;
    }
    return false ;
  }

  /** Sorts this GenSet using comparator.  Currently implemented using brain dead bubble sort.
   * 
   */
  public void sort( Comparator comparator ) {
    Object[] array = new Object[ size() ] ;
    Object temp;

    int i=0;

    // Copy into an array
    for (i=0;i<size();i++)
      array[i] = elementAt(i);

    for (i=0;i<array.length ;i++) {
       for ( int j=i+1;j<array.length; j++ ) {
          if ( comparator.compare( array[j], array[i] ) < 0 ) {
             temp  = array[j];
             array[j] = array[i];
             array[i] = temp;
          }
       }
    }

    for (i=0;i<array.length; i++) {
       setElementAt( array[i], i );
    }
  }
  
  private int findpivot( int i, int j, Comparator comparator ) {
    Object first = elementAt(i);
    for (int k=i+1;k<j;k++)
       if ( comparator.compare( elementAt(k), first ) < 0 ) 
           return k ;
       else if ( comparator.compare( elementAt(k),first ) > 0 ) 
           return i ;
           
    return -1;
  }
  
  protected int partition( int i, int j, Object pivot, Comparator comparator ) {
     int l = i, r = j ;
     
     while ( true ) {
       swap( l, r ) ;  
       while ( comparator.compare( elementAt( l ), pivot ) < 0 )
            l++;
       while ( r >= 0 && comparator.compare( elementAt( r ), pivot ) >= 0 )
            r--;
       if ( l > r )
         break ;
     }
     return l ;
  }

  public void swap( int i, int j ) {
    Object temp = elementAt( i );
    setElementAt( elementAt(j), i );
    setElementAt( temp, j );
  }

  public void quicksort( Comparator comparator ) {
    quicksort(0,size()-1, comparator) ; 
  }
    
  protected void quicksort(int i, int j, Comparator comparator ) {
    
    if ( j == i )
      return ;
      
    if ( j == i + 1 ) {
      if ( comparator.compare( elementAt(i), elementAt(j) ) > 0 )  // element i > element j
         swap( i, j );
    }
    else {
      int pivotindex = findpivot(i,j, comparator);
   
      if ( pivotindex != -1 ) {
         Object pivot = elementAt( pivotindex ) ;    
         int k = partition( i, j, pivot, comparator );
         quicksort( i, k-1, comparator ) ;
         quicksort( k, j, comparator );
      }
    }

    /*
    System.out.println("Result of sorting: " + i + " " + j );
    for (int l=i;l<=j;l++)
      System.out.print( " " + elementAt(l) );
    System.out.println();
    */
  }

  /** Test module for exercising GenSet class.   
   *
   */

  public static void main(String argv[]) {
    GenSet testSet = new GenSet();
    StringComparator stringComparator = new StringComparator();

    testSet.append("The");
    testSet.append("quick");
    testSet.append("brown");
    testSet.append("fox");
    testSet.append("jumps");
    testSet.append("ABCDE");
    testSet.append("12345");
    testSet.append("quicker");

    GenSet set2 = new GenSet();
    set2.append("The");
    set2.append("quicker");
    set2.append("over");
    set2.append("the");
    set2.append("lazy");
    set2.append("dog");

    /*
    System.out.println( "Comparing quick and brown:" );
    System.out.println( stringComparator.compare( "Quick", "Brown" ) );
    */

    System.out.println( "Unsorted list" );
    System.out.println( testSet.toString() );
    
    GenSet testSet2 = (GenSet) testSet.clone() ;
    
    testSet2.sort( stringComparator );
    System.out.println( "Sorted list using sort." );
    System.out.println( testSet2.toString() );
    
    testSet.quicksort( stringComparator );
    System.out.println( "Sorted list using quicksort." );
    System.out.println( testSet.toString() );

    System.out.println( "Union of GenSets");
    System.out.println( "Set 1: " + testSet.toString() );
    System.out.println( "Set 2: " + set2.toString() );

    GenSet unionSet = testSet.union( set2, stringComparator );
    GenSet intersectSet = testSet.intersection( set2, stringComparator );
    System.out.println( "Union of set1 and set2:" );
    System.out.println( unionSet.toString() );
    System.out.println( "Intersection of set1 and set2:");
    System.out.println( intersectSet.toString() );
    System.out.println( "Difference of set1 and set2:");
    System.out.println( testSet.difference( set2, stringComparator ) );

    GenSet set3 = new GenSet();
    set3.append("A");
    set3.append("a");
    set3.append("A");
    set3.append("big");
    set3.append("bog");
    set3.append("big");
    set3.append("a");
    set3.append("dog");
    
    String s1 = "dog", s2 = "fox", s3= "canal", s4 = "plan";
    
    GenSet set5 = new GenSet();
    GenSet set6 = new GenSet();
    GenSet set7 = new GenSet();
    set5.append(s1);
    set5.append(s2);
    set5.append(s3);
    set6.append(s3);
    set6.append(s2);
    set6.append(s1);
    set7.append(s2);
    set7.append(s3);
    set7.append(s4);
    
    System.out.println();
    System.out.println("Set 5: " + set5.toString() );
    System.out.println("Set 6: " + set6.toString() );
    System.out.println("Set 7: " + set7.toString() );

    System.out.println("Result of set5.strongEquals( set6 ): " +  set5.strongEquals( set6 ) );
    System.out.println("Result of set6.strongEquals( set5 ): " + set6.strongEquals( set5 ) );
    System.out.println("Result of set7.strongEquals( set6 ): " + set7.strongEquals( set6 ) );

    System.out.println( "Set 3: " + set3.toString() );
    System.out.println( "Duplicates in Set 3: " + set3.getDuplicates(stringComparator) );
    set3.removeDuplicates( stringComparator );
    System.out.println( "Set 3 after removing duplicates:" + set3.toString() );

    GenSet set4 = new GenSet();
    set4.append("The");
    set4.append("quick");
    set4.prepend("brown");
    set4.prepend("fox");
    System.out.println( "Set 4: " + set4.toString() );
    set4.removeLast();
    set4.removeFirst();
    System.out.println( "Set 4: " + set4.toString() );
    
    

  } 
}
