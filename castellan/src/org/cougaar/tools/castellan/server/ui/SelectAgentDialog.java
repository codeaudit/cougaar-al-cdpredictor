/*
* $Header: /opt/rep/cougaar.cvs/al-cdpredictor/castellan/src/org/cougaar/tools/castellan/server/ui/SelectAgentDialog.java,v 1.1 2002-06-10 23:14:12 cvspsu Exp $
*
* $Copyright$
*
* This file contains proprietary information of Intelligent Automation, Inc.
* You shall use it only in accordance with the terms of the license you
* entered into with Intelligent Automation, Inc.
*/

/*
* $Log: SelectAgentDialog.java,v $
* Revision 1.1  2002-06-10 23:14:12  cvspsu
* *** empty log message ***
*
*
*/
package org.cougaar.tools.castellan.server.ui;

import javax.swing.*;
import java.util.* ;

public class SelectAgentDialog extends JDialog
{
   // ATTRIBUTES
   int result = JOptionPane.CANCEL_OPTION ;
   String selectedAgentName = null;
   
   // CONSTRUCTORS
   public SelectAgentDialog( Iterator agentIter ){
   }

   // METHODS
   public int getResult() {
        return result ;   
    }
   public String getAgentName() {
        return selectedAgentName ;   
    }
}
