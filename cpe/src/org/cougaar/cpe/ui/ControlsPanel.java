package org.cougaar.cpe.ui;

import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OMCRange;
import org.cougaar.core.adaptivity.OMCPoint;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.blackboard.IncrementalSubscription;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class ControlsPanel extends JPanel {

    HashMap conditionNameToControlMap = new HashMap() ;

    /**
     * Called periodically to update the local controls with any changes
     * in the OperatingModeConditions they represent.
     */

    public void updateValues( ) {
        // Just refresh with the current op mode condition values based on BB changes.
        for (Iterator iterator = conditionNameToControlMap.values().iterator();
             iterator.hasNext();) {
            Component component = (Component) iterator.next();
            if ( component instanceof JComboBox ) {
                JComboBox control = (JComboBox) component ;
                OperatingModeCondition operatingModeCondition =
                    ( ( OpModeConditionListModel )
                    control.getModel() ).getOmc();
                control.setSelectedItem( operatingModeCondition.getValue() );
            }
            else if ( component instanceof MyTextField ) {
                MyTextField textField = (MyTextField) component ;
                if ( !textField.hasFocus() ) {
                    OperatingModeCondition condition = textField.condition ;
                    textField.setText( condition.getValue().toString() );
                }
            }
        }
    }

    public class OpModeConditionListModel implements ComboBoxModel {

        public OperatingModeCondition getOmc() {
            return omc;
        }

        private Object selectedItem ;

        public Object getSelectedItem() {
            Comparable c = omc.getValue() ;
            OMCRangeList rl = omc.getAllowedValues() ;
            OMCRange[] rs = rl.getAllowedValues() ;
            for (int i = 0; i < rs.length; i++) {
                OMCPoint r = (OMCPoint) rs[i];
                if ( r.contains( c ) ) {
                    return r ;
                }
            }
            return c ;
        }

        public void setSelectedItem(Object anItem) {
            if ( anItem instanceof OMCPoint ) {
                OMCPoint pt = (OMCPoint) anItem ;
                omc.setValue( pt.getMin() );
            }
            else {
                Comparable c = (Comparable) anItem ;
                omc.setValue( c );
            }
        }

        public OpModeConditionListModel(OperatingModeCondition omc) {
            this.omc = omc;
        }

        public void addListDataListener(ListDataListener l) {
            listeners.add( l ) ;
        }

        public Object getElementAt(int index) {
            OMCRangeList rl = omc.getAllowedValues() ;
            return rl.getAllowedValues()[index] ;
        }

        public int getSize() {
            OMCRangeList rl = omc.getAllowedValues() ;
            return rl.getAllowedValues().length ;
        }

        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l) ;
        }

        OperatingModeCondition omc ;
        ArrayList listeners = new ArrayList() ;
    }

    public ControlsPanel(BlackboardService bs) {
        this.bs = bs;
    }

    public void updateOperatingModes( ArrayList opModes ) {
         // For each control, update the display with the correct value.
        for (int i = 0; i < opModes.size(); i++) {
            OperatingModeCondition condition = (OperatingModeCondition)opModes.get(i);
            JComponent component = (JComponent) conditionNameToControlMap.get( condition.getName() ) ;
            if ( component instanceof JComboBox ) {
                JComboBox box = (JComboBox) component ;
                box.getModel().setSelectedItem( condition.getValue() );
            }
            else if ( component instanceof JTextField ) {
                JTextField field = (JTextField) component ;
                field.setText( condition.getValue().toString() );
            }
        }
    }

    class MyTextField extends JTextField {

        public MyTextField(OperatingModeCondition condition) {
            this.condition = condition;
        }

        public OperatingModeCondition getCondition() {
            return condition;
        }

        OperatingModeCondition condition ;
    }


    public void setOperatingModes( ArrayList opModes ) {
        removeAll();
        Collections.sort( opModes , new Comparator() {
            public int compare(Object o1, Object o2) {
                OperatingModeCondition omc1 = (OperatingModeCondition) o1, omc2 = (OperatingModeCondition) o2 ;
                return omc1.getName().compareTo( omc2.getName() ) ;
            }
        }) ;

        GridBagLayout layout = new GridBagLayout() ;
        setLayout( layout );

        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ; gbc.weighty = 1 ;
        conditionNameToControlMap.clear();

        for (int i = 0; i < opModes.size(); i++) {
            final OperatingModeCondition condition = (OperatingModeCondition)opModes.get(i);
            OMCRangeList values = condition.getAllowedValues() ;
            OMCRange[] allowed = values.getAllowedValues() ;
            boolean found = false ;

            // Insure that the OMC consist only of point elements.
            for (int j = 0; j < allowed.length; j++) {
                OMCRange omcRange = allowed[j];
                if ( !( omcRange instanceof OMCPoint ) ) {
                    found = true ;
                }
            }

            if ( !found ) {
                gbc.gridy ++ ; // Move y index.
                JLabel label = new JLabel( condition.getName() ) ;
                gbc.anchor = GridBagConstraints.WEST ;
                gbc.fill = GridBagConstraints.NONE ;
                gbc.weightx = 1 ; gbc.gridx = 0 ;
                gbc.insets = new Insets( 5, 10, 5, 5 ) ;
                layout.setConstraints( label, gbc );
                add( label ) ;

                gbc.fill = GridBagConstraints.HORIZONTAL ;
                gbc.gridx = 1;  gbc.weightx = 500 ;
                gbc.insets = new Insets( 5, 5, 5, 10 ) ;
                gbc.anchor = GridBagConstraints.CENTER ;
                JComboBox box = new JComboBox() ;
                layout.setConstraints( box, gbc );
                conditionNameToControlMap.put( condition.getName(), box ) ;
                box.setModel( new OpModeConditionListModel( condition ) );
                box.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent e) {
                        JComboBox box = (JComboBox) e.getSource() ;
                        OpModeConditionListModel model =  (OpModeConditionListModel) box.getModel() ;
                        // System.out.println( model.getOmc() + ", e=" + e );
                        OperatingModeCondition omc = model.getOmc() ;
                        OMCPoint point = (OMCPoint) box.getSelectedItem() ;
                        omc.setValue( point.getMin() );
                        publishChange( omc );
                    }
                } ) ;
                box.setSelectedItem( condition.getValue() );
                add( box ) ;
            }
            else {
                gbc.gridy ++ ; // Move y index.
                gbc.anchor = GridBagConstraints.WEST ;
                gbc.fill = GridBagConstraints.NONE ;
                gbc.weightx = 1 ; gbc.gridx = 0 ;
                gbc.insets = new Insets( 5, 10, 5, 5 ) ;

                JLabel label = new JLabel( condition.getName() ) ;
                layout.setConstraints( label, gbc );
                add( label ) ;

                gbc.fill = GridBagConstraints.HORIZONTAL ;
                gbc.gridx = 1;  gbc.weightx = 500 ;
                gbc.insets = new Insets( 5, 5, 5, 10 ) ;
                gbc.anchor = GridBagConstraints.CENTER ;
                final JTextField field = new MyTextField( condition );
                conditionNameToControlMap.put( condition.getName(), field ) ;
                field.addKeyListener( new KeyListener() {
                    public void keyPressed(KeyEvent e) {
                        if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                            doEnter( condition, field ) ;
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                    }

                    public void keyTyped(KeyEvent e) {
                    }
                });
                field.setText( condition.getValue().toString() );
                layout.setConstraints( field, gbc );
                add( field ) ;
            }
        }

        gbc.gridx = 0 ; gbc.gridy ++ ; gbc.weighty = 100 ; gbc.fill = GridBagConstraints.BOTH ;
        JPanel dummyPanel = new JPanel() ;
        layout.setConstraints( dummyPanel, gbc );
        add( dummyPanel ) ;
    }

    private void doEnter(OperatingModeCondition condition, JTextField field) {

//        System.out.println("Enter pressed for condition " + condition.getName() );
        OMCRangeList omcRangeList = condition.getAllowedValues() ;
        OMCRange[] ranges = omcRangeList.getAllowedValues() ;
        for (int i = 0; i < ranges.length; i++) {
            OMCRange range = ranges[i];
            if ( range.getMin() instanceof Integer ) {
                Integer intValue = Integer.valueOf( field.getText() ) ;
                if ( condition.getAllowedValues().isAllowed( intValue ) ) {
                    condition.setValue( intValue );
                    publishChange( condition );
                }
                else {
                    JOptionPane.showMessageDialog( this, "\"" + field.getText() + "\" is not valid for OperatingMode " +
                            condition.getAllowedValues() );
                }
            }
            else if ( range.getMin() instanceof Long ) {
                Long longValue = Long.valueOf( field.getText() ) ;
                if ( condition.getAllowedValues().isAllowed( longValue ) ) {
                    condition.setValue( longValue );
                    publishChange( condition );
                }
                else {
                    JOptionPane.showMessageDialog( this, "\"" + field.getText() + "\" is not valid for OperatingMode " +
                            condition.getAllowedValues() );
                }
            }
            else if ( range.getMin() instanceof Double ) {
                Double doubleValue = Double.valueOf( field.getText() ) ;
                if ( condition.getAllowedValues().isAllowed( doubleValue ) ) {
                    condition.setValue( doubleValue );
                    publishChange( condition );
                }
                else {
                    JOptionPane.showMessageDialog( this, "\"" + field.getText() + "\" is not valid for OperatingMode " +
                            condition.getAllowedValues() );
                }
            }
        }
    }

    protected void publishChange( Object o ) {
        bs.openTransaction();
        bs.publishChange( o );
        bs.closeTransaction();
    }


    BlackboardService bs ;
}
