package com.axiom.lib.io;
import java.io.* ;

/**
 *  Performs DOS style wild-card matching.  All directories are accepted,
 *  along with files whose name matches the filter string.
 */
public class StandardFilenameFilter extends javax.swing.filechooser.FileFilter
    implements FilenameFilter
{
   public StandardFilenameFilter( String name, String filterString ) {
        this( filterString ) ;
        this.name = name ;
   }

   public StandardFilenameFilter( String s )
   {
     this( s, false );
   }
    
   public StandardFilenameFilter( String s, boolean isCaseSensitive ) {
     setFilterString( s );
     this.isCaseSensitive = isCaseSensitive ;
   }

   public String getDescription() {
        return toString() ;
   }

   public String toString() {
       if ( getName() != null )
        return getName() + "(" + getFilterString() + ")" ;
       else
        return getFilterString() ;
   }

   public boolean accept( File file ) {
       String s = file.getName() ;
       if ( file.isDirectory() )
          return true ;

       return accept( null, s ) ;
   }

   public boolean accept( File file, String name ) {
     int j=0, k=0, last = -1;
     if ( name.length() == 0 )
        return false ;
     if ( fs.length() == 0 )
        return false ;
     boolean startmatch = false;
     int smindex = 0;
     String sname, fst ;
     
     // Convert to lower case
     if (!isCaseSensitive) {
       sname = new String( name ).toLowerCase();
       fst = new String( fs ).toLowerCase() ;
     }
     else {
       sname = name ;
       fst = fs ;
     }
     
     while(true)
     {
//       System.out.println("j k " + j + " " + k);
       if ( fst.charAt(j) == '*') {
          while ( (j<(fst.length()-1)) && (fst.charAt(j)=='*') )
             j++;
          if ( j == (fst.length()-1) && fst.charAt(j)=='*')
             return true ;  // Anything can match here, we are done
          last = j - 1; // Save most recent wildcard character
          while ( (k<sname.length()) && (sname.charAt(k)!=fst.charAt(j)))
             k++;
          if ( k == sname.length())
             return false ;
          if (sname.charAt(k)==fst.charAt(j)) {
             startmatch = true;
             smindex = k ;    // Start match
          }
       }
       else {
          if ( fst.charAt(j++) == sname.charAt(k++) ) {
             if ( startmatch == false ) {
                 startmatch = true ;
                 smindex = k - 1;
             }
          }
          else {
             if ( last == -1)  // No * character has been found
               return false ;
             startmatch = false ;  // Return to last * character
             k = smindex + 1;      // Reset k counter to start match +1
             j = last ;
          }
       }

       // Check for termination
       if ( ( k == sname.length() ) && ( j < fst.length() ) )
       {
          if ( j == fst.length()-1 && fst.charAt(j)=='*' )
             return true ;
          else
             return false ;
       }
       if ( ( j == fst.length() ) && ( k < sname.length() ) ) {
          if ( last == -1 )
             return false ;
          // attempt to rematch, by setting j back to the last '*' and k back to beginning of the match
          j = last ;
          k = smindex + 1; 
       }
          
       if ( ( k == sname.length() ) && ( j == fst.length() ) )
          return true ;
     }
   }

   /** Strip out any superflous '*' characters in the filter string, e.g. 'p**g'.
    *  Additionally, "*.*" equivalent strings are converted to "*"  to mimic DOS
    *  semantics.
    */
   void setFilterString( String s ) {
     if ( s == "*.*" ) {
        fs = "*";
        return ;
     }
     char[] temp = new char[s.length()];
     int i=0,j=0;
     while (true) {
       if ( s.charAt(i) == '*' )
       {
          temp[j++] = '*';
          while ( i < s.length() && s.charAt(i)=='*' )  // skip all additional * characters
             i++;
       }
       else
         temp[j++]=s.charAt(i++);
       if ( i >= s.length() )
          break ;
     }
     if ( s == "*.*" ) {
        fs = "*";
        return ;
     }
     fs = new String(temp);
   }

   public String getName() { return name ; }

   public String getFilterString() { return fs; }

   private String fs ;

   private String name ;
   
   boolean isCaseSensitive ;

   public static void main( String[] argv ) {
     FilenameFilter filter1 = new StandardFilenameFilter("*.gif") ;
     
     System.out.println("Using filter string *.gif" );
     System.out.println("Trying moose");
     System.out.println( filter1.accept(null,"moose") );
     System.out.println("Trying moose.gif");
     System.out.println( filter1.accept(null,"moose.gif")) ;
     System.out.println("Trying moose.jpg");
     System.out.println( filter1.accept(null,"moose.jpg")) ;
     System.out.println("Trying moose.gif.gif");
     System.out.println( filter1.accept(null,"moose.gif.gif"));
     System.out.println("Trying moose.gif.gi");
     System.out.println( filter1.accept(null,"moose.gif.gi"));
     
     FilenameFilter filter2 = new StandardFilenameFilter("*.gif*");
     System.out.println("Trying moose");
     System.out.println( filter2.accept(null,"moose") );
     System.out.println("Trying moose.gif");
     System.out.println( filter2.accept(null,"moose.gif")) ;
     System.out.println("Trying moose.gif.gif");
     System.out.println( filter2.accept(null,"moose.gif.gif"));
     System.out.println("Trying moose.gif.gi");
     System.out.println( filter2.accept(null,"moose.gif.gi"));
          
   }
}
