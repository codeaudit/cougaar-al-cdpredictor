package com.axiom.lib.mat ;
import javax.swing.* ;
import java.awt.event.* ;

public class MatTest extends JFrame {

    public MatTest() {
        super( "Test" ) ;
        setSize( 300, 200 ) ;
       JMenuBar mb = new JMenuBar() ;
       this.setJMenuBar( mb );
       JMenu testMenu = new JMenu("Test") ;
       mb.add( testMenu ) ;

       JMenuItem miTest = new JMenuItem( "Test" ) ;
       miTest.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doTest() ;
            }
       } ) ;
       testMenu.add( miTest) ;
    }

    protected void doTest() {
        //MatEng eng = MatEng.getInstance() ;
        eng.evalString( "moose=rand(5,5)" );
        eng.evalString( "surf( moose )" ) ;
    }

    public static void main( String[] args ) {
        //sychronized( eng ) {
        eng = MatEng.getInstance() ;
        eng.evalString( "mice=[1 2 3]" );
        //}
        MatTest mt = new MatTest() ;
        mt.setVisible( true );
    }

    volatile static MatEng eng ;
}