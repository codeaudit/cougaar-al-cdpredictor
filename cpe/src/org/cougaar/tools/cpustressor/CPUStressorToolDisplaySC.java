 /*
  * <copyright>
  *  Copyright 2002 (Intelligent Automation, Inc.)
  *  under sponsorship of the Defense Advanced Research Projects
  *  Agency (DARPA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED "AS IS" WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  *
  * </copyright>
  *
  * CHANGE RECORD
  *  6/24/02 Initial version  by IAI
  */

package org.cougaar.tools.cpustressor;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.cougaar.core.servlet.*;

import org.cougaar.planning.servlet.BlackboardServletSupport;

public class CPUStressorToolDisplaySC extends HttpServlet{

   private BlackboardServletSupport myBSSupport = null;

   CPUStressorControlImpl myMessage;
   String myToolOn = "?";
   String myTimeOn = "?";
   String myTimeOff = "?";

   /**
    * Method that recieves a BlackboardServletComponent
    **/
   public void setSimpleServletSupport( SimpleServletSupport support ){
      if( support instanceof BlackboardServletSupport ){
         myBSSupport = (BlackboardServletSupport) support;
      }
   }

   /*
   * This method is used to initialize the Servlet.  A ControlMsgImpl
   * is created for the Serlet.
   */
   public void init( ServletConfig conf ) throws ServletException{
      super.init(conf);

      // Create a control message with reasonable defaults for the display to use.
      myMessage = new CPUStressorControlImpl( false, 11111, 33333 );
   }

   /*
   * This method is called when anyone requests a URL that points to the servlet.
   * A form is presented for the user to specify control parameters for the CPUConsumer
   * tool.
   */
   public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
     // Get the "current/ initial values" for parameters that the form should
     // "initially" present.  A default message should have been created in the
     // servlet init() method; Othewise the string initial values will be used.
     if( myMessage != null ){
        if( myMessage.isToolON() ){
           myToolOn = "y";
        }
        else{
           myToolOn = "n";
        }
        myTimeOn = Long.toString( myMessage.getTimeON() );
        myTimeOff = Long.toString( myMessage.getTimeOFF() );
     }

      res.setContentType("text/html");
      PrintWriter out = res.getWriter();

      out.println("<html><body bgcolor=\"#cccccc\">");
      out.println("<div align=\"center\">");
      out.println("<h3>CPU Consumer Tool</h3>");
      out.println("</div>");

      // change me below: action
      out.println("<form method=\"post\" action=\"display\" name=\"ToolParameters\" id=\"ToolParameters\">");
      out.println("<table border=\"0\" cellpadding=\"10\" cellspacing=\"0\">");
      out.println("<tr>");
      out.println("    <td>Turn tool on?</td>");
      out.println("    <td><input type=\"text\" name=\"turnToolOn\" value="+ myToolOn + " size=\"2\" maxlength=\"2\"></td>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("    <td>On Time:</td>");
      out.println("    <td><input type=\"text\" name=\"timeOn\" value=" + myTimeOn  + " size=\"10\" maxlength=\"10\"> msec</td>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("    <td>Off Time:</td>");
      out.println("    <td><input type=\"text\" name=\"timeOff\" value=" + myTimeOff + " size=\"10\" maxlength=\"10\"> msec</td>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("    <td align=\"center\"><input type=\"submit\" name=\"Submit\" value=\"OK\"></td>");
      out.println("</tr>");
      out.println("</form>");
      out.println("</body>");
      out.println("</html>");
   }

      /*
      * This method is called when the form presented in the doGet() is submitted.
      * The parameters the user specified are parsed and converted into a ControlMsg.
      * This message is then added to the Blackboard for the CPUConsumerPlugin to grab.
      * The form is presented again to the user for their next set of modifications.
      */

      public void doPost(HttpServletRequest req, HttpServletResponse res)
          throws IOException {

        // Process user input
        myToolOn = req.getParameter("turnToolOn");
        myTimeOn = req.getParameter("timeOn");
        myTimeOff = req.getParameter("timeOff");

        // Parse the Strings into the appropriate control message attributes.
        try {
           myMessage.setTimeON(Long.parseLong(myTimeOn));
           myMessage.setTimeOFF(Long.parseLong(myTimeOff));
        }
        catch (Exception e) { }
        if( myToolOn.equalsIgnoreCase("Y") ){
           myMessage.turnToolON();
        }
        else{
           myMessage.turnToolOFF();
        }

        //Copy the contents of my message into a new message for the Blackboard.
        // Add the ControlMsg to the blackboard.
        try {
           CPUStressorControlImpl aMessage = (CPUStressorControlImpl) myMessage.clone();
           myBSSupport.getBlackboardService().openTransaction();
           myBSSupport.getBlackboardService().publishAdd(aMessage);
        }
        catch (Exception e) { }
        finally {
           myBSSupport.getBlackboardService().closeTransaction();
        }

        // Display the form again.  Should show the latest update parameters.
        doGet( req, res );
   }

}
