package org.cougaar.cpe.ui;

import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.OMCRange;
import org.cougaar.core.adaptivity.OMCPoint;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.blackboard.IncrementalSubscription;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class ControlsPanel extends JPanel {

    ArrayList mpAndControlsList = new ArrayList() ;
    HashMap conditionNameToControlMap = new HashMap() ;

    /**
     * Called periodically to update the local controls with any changes
     * in the OperatingModeConditions they represent.
     */

    public void updateValues( ) {
        // Just refresh with the current op mode condition values based on BB changes.
        for (Iterator iterator = conditionNameToControlMap.values().iterator();
             iterator.hasNext();) {
            JComboBox control = (JComboBox) iterator.next();
            OperatingModeCondition operatingModeCondition =
                    ( ( OpModeConditionListModel )
                    control.getModel() ).getOmc();
            control.setSelectedItem( operatingModeCondition.getValue() );
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
            System.out.println("Set selected item " + anItem + ", class=" + anItem.getClass()  );

            if ( anItem instanceof OMCPoint ) {
                OMCPoint pt = (OMCPoint) anItem ;
                omc.setValue( pt.getMin() );
            }
            else {
                Comparable c = (Comparable) anItem ;
                omc.setValue( c );
            }

//            if ( bs != null ) {
//                boolean wasOpen = true ;
//                if ( !bs.isTransactionOpen() ) {
//                    wasOpen = false;
//                    bs.openTransaction();
//                }
//                bs.publishChange( omc );
//                if ( !wasOpen) {
//                    bs.closeTransaction();
//                }
//            }
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

    public void setOperatingModes( ArrayList opModes ) {
        removeAll();
        GridBagLayout layout = new GridBagLayout() ;
        setLayout( layout );

        GridBagConstraints gbc = new GridBagConstraints() ;
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        gbc.weightx = 100 ; gbc.weighty = 1 ;

        for (int i = 0; i < opModes.size(); i++) {
            OperatingModeCondition condition = (OperatingModeCondition)opModes.get(i);
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
                gbc.fill = GridBagConstraints.HORIZONTAL ;
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
                    }
                } ) ;
                box.setSelectedItem( condition.getValue() );
                add( box ) ;
            }
            else {
                System.err.println("Cannot edit condition " + condition );
            }
        }

        gbc.gridx = 0 ; gbc.gridy ++ ; gbc.weighty = 100 ; gbc.fill = GridBagConstraints.BOTH ;
        JPanel dummyPanel = new JPanel() ;
        layout.setConstraints( dummyPanel, gbc );
        add( dummyPanel ) ;
    }


    BlackboardService bs ;
}
