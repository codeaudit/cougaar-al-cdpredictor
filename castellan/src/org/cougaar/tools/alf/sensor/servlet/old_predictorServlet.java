
/*
  * <copyright>
  *  Copyright 2003 (Intelligent Automation, Inc.)
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
  */
  
package org.cougaar.tools.alf.sensor.servlet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cougaar.core.blackboard.BlackboardClient;

import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.community.CommunityRoster;

import org.cougaar.core.servlet.BaseServletComponent;

import org.cougaar.core.mts.MessageAddress;

import org.cougaar.core.util.UID;

import org.cougaar.core.component.*;
import org.cougaar.core.service.*;

import org.cougaar.multicast.AttributeBasedAddress;

import org.cougaar.lib.aggagent.test.TestRelay;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.*;


public class predictorServlet extends BaseServletComponent implements BlackboardClient {	
  
	public String getBlackboardClientName() 
  	{
    	return toString();
  	}
  
	public long currentTimeMillis() 
  	{
    	throw new UnsupportedOperationException(
        this+" asked for the current time???");
  	}
  
  	public void setBlackboardService(BlackboardService blackboard) 
  	{
    	this.blackboard = blackboard;
  	}
  
  	public final void setAgentIdentificationService(AgentIdentificationService ais) 
  	{
    	MessageAddress an;
    	if ((ais != null) &&
           ((an = ais.getMessageAddress()) instanceof MessageAddress)) 
        {
      		this.agentId = (MessageAddress) an;
   	 	} 
   	 	else 
   	 	{
    		return;
    	}
    }
  
  	public void setBindingSite(BindingSite bs) 
  	{
    	super.setBindingSite(bs);
    	this.serviceBroker = bs.getServiceBroker();
  	}
 
 
 	public void unload() 
 	{
    	super.unload();
    	if (blackboard != null) 
    	{
      		serviceBroker.releaseService(
            this, BlackboardService.class, blackboard);
      		blackboard = null;
    	}
    }
  
  	protected MessageAddress getAgentID() 
  	{  	
    	return agentId;
  	}
  
  	protected String getPath() 
  	{	
    	return "/predictor";
  	}
   
  	protected Servlet createServlet() 
  	{
  	
    	return new MyServlet();
  	}
  
  	protected void exceptionFailure(PrintWriter out, Exception e) 
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<font size=+1>Failed due to Exception</font><p>");
    	out.println(e);
    	out.println("<p><pre>");
    	e.printStackTrace(out);
    	out.println("</pre>");
    	out.println("</body>");
    	out.println("</html>");
  	}

  	protected void postSuccess(PrintWriter out) 
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER>Turn Off Successful !!</CENTER></H3>");
    	out.println("</body>");
    	out.println("</html>");
  	}

  	protected void postFailure(PrintWriter out) 
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER> Turn Off UnSuccessful !!</CENTER></H3>");
    	out.println("</body>");
    	out.println("</html>");
  	}
  
  	protected void publish(PrintWriter out) 
  	{ 	 
		getComm(out); 	
    
  	}
  
  	public void getComm(PrintWriter out)
  	{
  		Collection targets = new HashSet();
  		myUIDService = (UIDService) serviceBroker.getService(this,UIDService.class, null);
  		communityService = (CommunityService) serviceBroker.getService(this, CommunityService.class, null);
    	if (communityService == null) 
    	{
    		myLoggingService.error("CommunityService not available.");
     		return;
   		}             
    	CommunityRoster cr = communityService.getRoster("ALSUPPLY-COMM");           
    	Collection alSupplyCommunities = cr.getMemberAgents();
    	targets.addAll(alSupplyCommunities);
    	CommunityRoster cr1 = communityService.getRoster("NCA-COMM");
    	Collection alSupplyCommunities1 = cr1.getMemberAgents();
    	targets.addAll(alSupplyCommunities1);
    	  
    	if(targets.isEmpty())
    	{
    		postFailure(out);
        	return;
    	}
    	else
    	{
    		for (Iterator iterator = targets.iterator(); iterator.hasNext();) 
			{
				MessageAddress target = (MessageAddress) iterator.next();
            	TestRelay relay = new TestRelay(myUIDService.nextUID(), agentId, target , "foo" ,null);
            	blackboard.openTransaction();
            	blackboard.publishAdd(relay);
            	blackboard.closeTransaction();
        	}
    		postSuccess(out);
		}
  	}
  
  private class MyServlet extends HttpServlet {
  		
  	public void doGet(
  		
      HttpServletRequest request,
      HttpServletResponse response) throws IOException 
      {
    	response.setContentType("text/html");
    	PrintWriter out = response.getWriter();   	
    	String action = request.getParameter(ACTION_PARAM);
    	
    	try 
    	{    
          if (action==null) 
          {
            printResponse(out);
          }          
          else if (action.equals(STATUS)) 
          {           
            publish(out);
          } 
        } 
        catch (Exception topLevelException) 
        {
          exceptionFailure(out, topLevelException);
        }
     }
    	
  
  
  	protected void printHtmlBegin(PrintWriter out) 
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
  	}

  	protected void printHtmlEnd(PrintWriter out)
  	{
    	out.println("</body>");
    	out.println("</html>");
  	}	
  
  	protected void printResponse(PrintWriter out)
  	{
  		printHtmlBegin(out);
  		out.println("<form method =\"get\" action=\"http://localhost:8800/$" + getAgentID() + getPath());
  		out.println("\">");
  		out.println("<tr>");
  		out.println("<td><B>Turn OFF Predictor Utility </td>");
  		out.println("</tr>");
  		out.println("<input type=\"submit\" name=\"" + ACTION_PARAM +
                "\" value=\"" + STATUS + "\">\n");  	
  		printHtmlEnd(out);
  	}
  
  }
	
  public static final String ACTION_PARAM = "action";
  public static final String STATUS = "Turn Off";
  	
  private HttpServletRequest request;
  private HttpServletResponse response;
  protected MessageAddress agentId;
  protected BlackboardService blackboard;
  protected LoggingService myLoggingService;
  protected CommunityService communityService;
  protected UIDService myUIDService;
  protected ServiceBroker serviceBroker;
}
