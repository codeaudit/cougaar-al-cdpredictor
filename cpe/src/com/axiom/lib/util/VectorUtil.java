package com.axiom.lib.util ;
import java.util.Vector ;

/**
 *  A few vector utility methods that didn't get included in the
 *  standard libraries.
 */

public final class VectorUtil {
    
   public static void addElements( Vector v1, Vector v2 ) {
      v1.ensureCapacity( v1.size() + v2.size() );
      for (int i=0;i<v2.size();i++) {
         v1.addElement( v2.elementAt(i) );
      }
   }
}