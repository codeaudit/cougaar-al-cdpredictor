package com.axiom.lib.image ;
import com.axiom.lib.util.* ;
import com.axiom.lib.io.* ;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.WindowAdapter ;
import java.awt.event.WindowEvent ;
import javax.swing.* ;

/** Window for displaying graphics.  Good for rapidly showing items.*/

class TestCanvas extends JPanel {
    public TestCanvas() {
        
    }
    
    public TestCanvas( Image image ) {
       img = image ;   
    }
    
    public void paint(Graphics g) {
	   int w = getSize().width;
	   int h = getSize().height;
	   if (img == null) {
	       super.paint(g);
	      g.setColor(Color.black);
	   } else {
	      g.drawImage(img, 0, 0, w, h, this);
	   }
    }
        
    public void setImage( Image img ) {
        this.img = img ;
    }
    
    Image img ;
}

public class TestWindow extends JFrame {
  public TestWindow( String name, ImageData data ) {
     super( name );
     Image image = data.makeImage() ;

     TestCanvas canvas = new TestCanvas(image);
     this.getContentPane().add( new JScrollPane( canvas ) ) ;
      this.setSize( 256, 256 ) ;
     setTitle("Untitled");
}

  public TestWindow( String name, Image image ) {
     super( name );
     TestCanvas canvas = new TestCanvas(image);
     this.getContentPane().add( new JScrollPane( canvas ) ) ;
     this.setSize( 256, 256 ) ;
     addWindowListener( new WindowAdapter() {
                          public void windowClosing( WindowEvent e ) {
                             setVisible( false ) ; 
                          } } );
  }

  public static void main(String[] argv) {
    File file = null ;

	if ( argv.length > 0 ) {
	   file = new File( argv[0] );
	}
    else if ( argv.length == 0 ) {
        JFileChooser fc = new JFileChooser() ;
        fc.addChoosableFileFilter( new StandardFilenameFilter( "Portable(PGM) Graymap Files", "*.pgm" ) ) ;
        fc.addChoosableFileFilter( new StandardFilenameFilter( "Portable(PPM) Pixmap Files", "*.ppm" ) ) ;
        fc.showDialog( null, "Open" ) ;
        file = fc.getSelectedFile() ;
    }

    if ( file == null ) {
   	  System.out.println("No file choosen.");
      System.exit(0);
    }
    
    if ( !file.exists() ) {
   	  System.out.println("File " + file + " does not exist.");
      System.exit(0);
    }

    String n = file.getName() ;

    Format format = Bitmap.guessType( n ) ;
    
    ImageData data = null ;

    FileInputStream fs ;

    try {
    fs = new FileInputStream( file  ) ;
    if ( format == PPMBitmap.FORMAT ) {
      data = PPMBitmap.read( fs ) ;
    }
    else if ( format == PGMBitmap.FORMAT ) {
      data = PGMBitmap.read( fs ) ;
    }
    else
      System.err.println( "Unknown file type for " + file.getName() );
    }
    catch ( Throwable t ) {
      t.printStackTrace() ;
    }
    
    Image image = data.makeImage() ;
                
    Frame frame = new Frame(file.toString());
    frame.addWindowListener( new WindowAdapter() {
                                public void windowClosing( WindowEvent e ) {
                                   System.exit(0) ;   
                                } } ) ;
    TestCanvas canvas = new TestCanvas();
    canvas.setImage( image );
    frame.add( canvas );
    frame.setSize( data.w, data.h ) ;
    frame.show();
  }
    
}