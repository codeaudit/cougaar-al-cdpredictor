package com.axiom.lib.util;

/** Thrown due to index errors, etc.
 */
  
public class MatrixException extends RuntimeException {
  MatrixException() {  
  }
  MatrixException( String name ) {
    super( name );   
  }

}
