package org.cougaar.tools.techspecs;

import org.cougaar.tools.techspecs.events.ActionEvent;
import org.cougaar.tools.techspecs.results.ActionResult;
import org.cougaar.core.adaptivity.OperatingMode;
import org.cougaar.core.adaptivity.OMCRangeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;

/**
 * A table based method to compute the results of actions in a manner which is dependent on op modes.
 * It takes a set of allowable inputs and maps them to allowable outputs and resource consumption results
 * as a function of a set of operating modes.
 *
 * <p> Currently, inputs are determined to be allowable if and only if they are of the correct class or type.
 * Other guard conditions are possible if there is a way to express them in the future.
 */
public class TableActionModelFunction extends ActionModelFunction
{
    /**
     * @param parentSpec
     * @param allowableInputs A list of ActionModelTableEntry objects which describe the allowable inputs for
     *  this action.
     */
    public TableActionModelFunction(ActionSpec parentSpec, ArrayList allowableInputs )
    {
        super(parentSpec);
        this.actionModelTable = allowableInputs ;
    }

    /**
     * A mapping from action events and opModes to nextAction events and results.
     */
    public static class ActionModelTableEntry implements Serializable {

        public ActionModelTableEntry(ActionEventSpec spec, OMCRangeList[] opModeValues, ActionResult[] result)
        {
            this.opModeValues = opModeValues;
            this.result = result;
            this.spec = spec;
        }

        /**
         * The triggering action specification.
         */
        protected ActionEventSpec spec ;

        /**
         * The actual operating mode values to match, in the order of the op mode dependencies. These can be null if we don't care.
         * They also should be disjoint to avoid being inconsistent or redundant.
         */
        protected OMCRangeList[] opModeValues ;

        /**
         * The resources consumed and events emitted on the basis of the op mode and input action in question.
         */
        protected ActionResult[] result ;
    }

    /**
     * Is the specified input accepted by this action?
     *
     * @param input
     * @return
     */
    public boolean accept(ActionEvent input)
    {
        for (int i=0;i<actionModelTable.size();i++) {
            ActionModelTableEntry entry = (ActionModelTableEntry) actionModelTable.get(i) ;
            if ( entry.spec.matches( input ) ) {
                return true ;
            }
        }
        return false ;
    }

    /**
     * This method does a simple matching operation between the triggering event, the current set of operating
     * modes and the current state.
     *
     * @param s The start state.
     * @param opModes
     * @param input
     * @param result A list of emitted actions to be filled in.
     * @return
     */
    public boolean process(RoleStateSpec s, OperatingMode[] opModes, ActionEvent input, ArrayList result)
    {
//        // Match the input against the table to determine the action result
//        if ( !accept( input ) ) {
//            return false ;
//        }

        // For each possible entry combo, match against the event input.
        for (int i=0;i<actionModelTable.size();i++) {
            ActionModelTableEntry entry = (ActionModelTableEntry) actionModelTable.get(i) ;
            // Does the input action match the result?
            if ( !entry.spec.matches( input ) ) {
                continue ;
            }
            boolean value = validateOperatingModes(entry, opModes);
            if ( value ) {
               for (int j=0;j<entry.result.length;j++) {
                   result.add( entry.result[j]) ;
               }
            }
        }

        return false ;
    }

    /**
     * Match a set of operating modes and their values and an entry.  If all operaing mode values
     * are allowable, true is returned.
     * @param entry
     * @param opModes
     * @return True if all opMode values match the OMCRangeList values accepted by the action model table entry.
     */
    private boolean validateOperatingModes(ActionModelTableEntry entry, OperatingMode[] opModes)
    {
        OMCRangeList[] opModeValues = entry.opModeValues ;
        for (int j=0;j<opModeValues.length;j++) {
            if ( opModes[j] == null ) {
                continue ;
            }
            if ( opModeValues[j] == null ) {
                continue ;
            }
            OMCRangeList rl = opModeValues[j] ;
            if ( !rl.isAllowed( opModes[j].getValue() ) ) {
                return false ;
            }
        }
        return true ;
    }

    /**
     * A list of OperatingMode values to be matched against the dependent op modes.
     * @param opModes
     * @return
     */
    public boolean verifyOpModes( OperatingMode[] opModes ) {
        if ( opModes.length != opModeSpecs.length ) {
            return false ;
        }

        for (int i = 0; i < opModes.length; i++)
        {
            OperatingMode opMode = opModes[i];
            OperatingModeSpec spec = opModeSpecs[i] ;
            if ( !opMode.getName().equals(spec.getName()) ) {
                return false ;
            }
        }
        return true ;
    }

    public OperatingModeSpec[] getOpModeSpecs()
    {
        return opModeSpecs;
    }


    public ArrayList getActionModelTable()
    {
        return actionModelTable;
    }

    protected ArrayList actionModelTable ;

    /**
     * These are all the operating modes which this action is dependent upon.
     */
    protected OperatingModeSpec[] opModeSpecs ;

}
