/*
 * TimeRangeSelector.java
 *
 * Created on August 29, 2001, 11:34 AM
 */

package org.hydra.libui;
import javax.swing.* ;
import javax.swing.event.* ;
import java.util.* ;
import java.awt.* ;
import org.hydra.pdu.* ;

/**
 *
 * @author  wpeng
 * @version 
 */
public class TimeRangeSelector extends javax.swing.JPanel {

    /**
     * @param startTime The interval start time in msec.
     * @param endTime The interval end time in msec.
     */
    public TimeRangeSelector( long startTime, long endTime ) {
        start = startTime ; end = endTime + 1000 ;  // The end time is always rounded up.
        makePanels() ;
        //doStartTimeChanged() ;
        //doEndTimeChanged() ;
        setSelectedStartTime( start ) ;
        setSelectedEndTime( end ) ;
        doStartTimeChanged() ;
        doEndTimeChanged() ;
    }
    
    public long getSelectedStartTime() { 
        return startSlider.getValue() * 1000L;
    }
    
    public long getSelectedEndTime() {
        return endSlider.getValue() * 1000L;
    }
    
    public void setSelectedEndTime( long time ) {
        endSlider.setValue( ( int ) ( time / 1000L ) ) ;   
    }
    
    public void setSelectedStartTime( long time ) {
        startSlider.setValue( ( int ) ( time / 1000L ) ) ;   
    }
    
    private JSlider makeTimeSlider() {
        // System.out.println( "Start time= " + start + ",end time=" + end ) ;
        // System.out.println( "Elapsed time = " +  elapsed  + " seconds." ) ;
        int min = 0, max ;
        
        // error =  ( start - ( start /1000 ) * 1000 ) ) ;
        JSlider slider = new JSlider( (int) ( start / 1000 ) , (int) ( end / 1000 ), (int) ( start / 1000 ) ) ;
        int majorTickSpacing = -1, minorTickSpacing = -1;

        Hashtable table = new Hashtable() ;
        // Add start and end times (in original date.)

        double elapsed = ( double ) ( end - start ) / 1000 ;        
        if ( elapsed / 5 < 8 ) {
            majorTickSpacing = 5 ;
            minorTickSpacing = 1 ;
            for (int i=majorTickSpacing;i<= elapsed;i+= majorTickSpacing) {
                table.put( new Integer(i), new JLabel( Integer.toString( i ) ) ) ;
            }
        }
        if ( elapsed / 10 < 8 ) {
            majorTickSpacing = 10 ;  // 10 seconds
            minorTickSpacing = 1 ; // 1 second
            for (int i=majorTickSpacing;i<= elapsed;i+= majorTickSpacing) {
                table.put( new Integer(i), new JLabel( Integer.toString( i ) ) ) ;
            }
        }
        else if ( elapsed / 30 < 8 ) {
            majorTickSpacing = 30 ;  // 30 seconds
            minorTickSpacing = 5 ; // 5 seocnds
            for (int i=majorTickSpacing;i<= elapsed;i+= majorTickSpacing) {
                table.put( new Integer(i), new JLabel( Integer.toString( i ) ) ) ;
            }
        }
        else if ( elapsed / 60 < 8 ) {
            majorTickSpacing = 60 ;  // 1 minute intervals.
            minorTickSpacing = 10 ; // 10 second intervals
            for (int i=majorTickSpacing;i<= elapsed;i+= majorTickSpacing) {
                table.put( new Integer(i), Integer.toString( i / 60 ) );
            }
        }
        else if ( elapsed / 300 < 8 ) {
            majorTickSpacing = 300 ;  // 5 minute intervals
            minorTickSpacing = 60 ; // 1 minute intervals
        }
        else if ( elapsed / 600 < 8 ) {
            majorTickSpacing = 600 ;  // 10 minute interavals
            minorTickSpacing = 60 ;   // 1 minute intervals 
        }
        else if ( elapsed / 1800 < 8 ) { // 30 minute intervals
            majorTickSpacing = 1800 ;
            minorTickSpacing = 300 ;
        }    
        if ( majorTickSpacing > 0 ) {
            slider.setMajorTickSpacing( majorTickSpacing ) ;
            slider.setMinorTickSpacing( minorTickSpacing ) ;
        }
        slider.setLabelTable( table ) ;
        // slider.setPaintLabels( true ) ;
        slider.setPaintTicks( true ) ;
        return slider ;        
    }
    
    protected void doStartTimeChanged() {
        int value = startSlider.getValue() ;
        tfSelectedStart.setText( PDU.formatTimeAndDate( value * 1000L ) ) ;
        if ( endSlider.getValue() < value ) {
            setSelectedEndTime( getSelectedStartTime() ) ;   
        }
        long elapsed = ( getSelectedEndTime() - getSelectedStartTime() ) ;
        tfSelectedElapsedTime.setText( PDU.formatElapsedTime( elapsed ) ) ;
    }
    
    protected void doEndTimeChanged() {
        int value = endSlider.getValue() ;
        tfSelectedEnd.setText( PDU.formatTimeAndDate( value * 1000L ) ) ;        
        if ( startSlider.getValue() > value ) {
            setSelectedStartTime( getSelectedEndTime() ) ;   
        }
        long elapsed = ( getSelectedEndTime() - getSelectedStartTime() ) ;
        tfSelectedElapsedTime.setText( PDU.formatElapsedTime( elapsed ) ) ;
    }
    
    private void makePanels() {
        JLabel startLabel = new JLabel( "Start" ) ;
        startLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 5 ) ) ;
        JLabel endLabel = new JLabel( "End" ) ;
        endLabel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 5 ) ) ;
        
        startTime = new JLabel( "Start Time: " ) ;
        startTime.setBorder( BorderFactory.createEmptyBorder( 5, 20, 5, 5 ) ) ;
        endTime = new JLabel( "End Time: " ) ;
        endTime.setBorder( BorderFactory.createEmptyBorder( 5, 20, 5, 5 ) ) ;
        tfStart = new JTextField( PDU.formatTimeAndDate(  start  ) ) ;
        tfStart.setEditable( false ) ;
        tfEnd = new JTextField( PDU.formatTimeAndDate(  end  ) ) ;
        tfEnd.setEditable( false ) ;
        
        JLabel selectedStartTime = new JLabel( "Selected start:" ) ;
        selectedStartTime.setBorder( BorderFactory.createEmptyBorder( 5, 20, 5, 5 ) ) ;
        JLabel selectedEndTime = new JLabel( "Selected end:" ) ;        
        selectedEndTime.setBorder( BorderFactory.createEmptyBorder( 5, 20, 5, 5 ) ) ;
        tfSelectedStart = new JTextField( )  ;
        tfSelectedStart.setEditable( false ) ;
        tfSelectedEnd = new JTextField( ) ;
        tfSelectedEnd.setEditable( false ) ;
        
        JLabel selectedElapsedTime = new JLabel( "Selected elapsed time: " ) ;
        tfSelectedElapsedTime = new JTextField( ) ;
        tfSelectedElapsedTime.setEditable( false ) ;

        // Make the sliders
        startSlider = makeTimeSlider() ;
        startSlider.setBorder( BorderFactory.createEmptyBorder(10, 5, 10, 10 ) ) ;
        startSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
               doStartTimeChanged() ;   
            }
        } ) ;
        
        endSlider = makeTimeSlider() ;
        endSlider.setBorder( BorderFactory.createEmptyBorder(10, 5, 10, 10 ) ) ;
        endSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
               doEndTimeChanged() ;   
            }
        } ) ;
                       
        // Start GridBagLayout phase
        
        GridBagLayout gbl = new GridBagLayout() ;
        setLayout( gbl ) ;
        GridBagConstraints gbc = new GridBagConstraints() ;
        
        gbc.anchor = GridBagConstraints.CENTER ;
        gbc.gridx = 0 ; gbc.gridy = 0 ;
        gbc.gridwidth = 2 ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ;
        
        GridBagConstraints gbc2 = new GridBagConstraints();
        GridBagLayout gbl2 ;
        JPanel panel1 = new JPanel(gbl2 = new GridBagLayout() ) ;
        panel1.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) ;
        gbl.setConstraints( panel1, gbc ) ;
        add( panel1 ) ;
        
        gbc2.gridx = 0 ; gbc2.gridy = 0 ; gbc.fill = GridBagConstraints.NONE ;
        // Make the panel
        gbc2.anchor = GridBagConstraints.EAST ;
        gbl2.setConstraints( startTime, gbc2 ) ;
        panel1.add( startTime ) ;

        gbc2.gridx = 1; gbc2.fill = GridBagConstraints.HORIZONTAL ; gbc2.anchor = GridBagConstraints.CENTER ; 
        gbl2.setConstraints( tfStart, gbc2 ) ;
        panel1.add( tfStart ) ;
        
        gbc2.gridx = 2 ; gbc2.fill = GridBagConstraints.NONE ; gbc2.anchor = GridBagConstraints.EAST; 
        gbl2.setConstraints( endTime, gbc2 ) ;
        panel1.add( endTime ) ;

        gbc2.gridx = 3; gbc2.fill = GridBagConstraints.HORIZONTAL ; gbc2.anchor = GridBagConstraints.CENTER ;
        gbl2.setConstraints( tfEnd, gbc2 ) ;        
        panel1.add( tfEnd ) ;
        
        gbc2.gridx = 0 ; gbc2.gridy = 1 ; gbc.fill = GridBagConstraints.NONE ; gbc.fill = GridBagConstraints.NONE ;
        // Make the panel
        gbc2.anchor = GridBagConstraints.EAST ;
        gbl2.setConstraints( selectedStartTime, gbc2 ) ;
        panel1.add( selectedStartTime ) ;

        gbc2.gridx = 1; gbc2.fill = GridBagConstraints.HORIZONTAL ; gbc2.anchor = GridBagConstraints.CENTER ;
        gbl2.setConstraints( tfSelectedStart, gbc2 ) ;
        panel1.add( tfSelectedStart ) ;
        
        gbc2.gridx = 2 ; gbc2.fill = GridBagConstraints.NONE ; gbc2.anchor = GridBagConstraints.EAST ; 
        gbl2.setConstraints( selectedEndTime, gbc2 ) ;
        panel1.add( selectedEndTime ) ;

        gbc2.gridx = 3; gbc2.fill = GridBagConstraints.HORIZONTAL ; gbc2.anchor = GridBagConstraints.CENTER ; 
        gbl2.setConstraints( tfSelectedEnd, gbc2 ) ;        
        panel1.add( tfSelectedEnd ) ;
        
        // Add a panel
        gbc.gridx = 0 ; gbc.gridy = 1 ; gbc.gridwidth = 2 ; gbc.anchor = GridBagConstraints.CENTER ; gbc.fill = GridBagConstraints.HORIZONTAL ;
        JPanel panel3 = new JPanel( new FlowLayout() ) ;
        selectedElapsedTime.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 15 ) ) ;
        panel3.add( selectedElapsedTime ) ;
        panel3.add( tfSelectedElapsedTime ) ;
        tfSelectedElapsedTime.setPreferredSize( new Dimension( 130, 16 ) ) ;
        gbl.setConstraints( panel3, gbc ) ;
        add( panel3 ) ;
        
        // Add the selected time panel.
        
        // This is the set of sliders and labels.
        
        gbc.gridwidth = 1 ; gbc.gridx = 0 ; gbc.gridy = 2 ; gbc.fill = GridBagConstraints.NONE ; gbc.weightx = 10 ;
        gbl.setConstraints( startLabel, gbc ) ;
        add( startLabel ) ;
        
        gbc.gridy = 3 ;
        gbl.setConstraints( endLabel, gbc ) ;
        add( endLabel ) ;
        
        // Add sliders
        gbc.gridx = 1 ; gbc.gridy = 2 ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 1000 ;
        gbc.weighty = 10 ;
        gbl.setConstraints( startSlider, gbc ) ;
        add( startSlider ) ;
        
        gbc.gridy = 3 ;
        gbl.setConstraints( endSlider, gbc ) ;
        add( endSlider ) ;
        
        // This is a dummy panel to take up additional space
        JPanel dummy = new JPanel() ;
        gbc.weighty = 1000 ; gbc.weightx = 1000 ; gbc.gridwidth = 2 ; gbc.gridy = 4 ;
        gbc.fill = GridBagConstraints.BOTH ;
        gbl.setConstraints( dummy, gbc ) ;
        add( dummy ) ;
    }

    JLabel startTime, endTime, selectedStartTime, selectedEndTime ;
    JTextField tfStart, tfEnd, tfSelectedStart, tfSelectedEnd, tfSelectedElapsedTime ;
    JSlider startSlider, endSlider ;
    long start, end ;
}
