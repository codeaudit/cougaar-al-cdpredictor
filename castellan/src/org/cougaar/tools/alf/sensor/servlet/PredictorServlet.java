
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

import org.cougaar.core.blackboard.BlackboardClient;
import org.cougaar.core.component.BindingSite;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.service.AgentIdentificationService;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.service.community.CommunityRoster;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.servlet.BaseServletComponent;
import org.cougaar.core.adaptivity.OMCRangeList;
import org.cougaar.core.adaptivity.InterAgentOperatingMode;
import org.cougaar.core.adaptivity.OMCRange;
import org.cougaar.lib.aggagent.test.TestRelay;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


public class PredictorServlet extends BaseServletComponent implements BlackboardClient {
  
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
      		this.agentId =  an;
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

    protected void postTurnONSuccess(PrintWriter out)
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER>Turn ON Successful !!</CENTER></H3>");
    	out.println("</body>");
    	out.println("</html>");
  	}

     protected void postTurnSLEEPSuccess(PrintWriter out)
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER>Predictor Sleeping Successful !!</CENTER></H3>");
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

    protected void postTurnONFailure(PrintWriter out)
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER> Turn ON UnSuccessful !!</CENTER></H3>");
    	out.println("</body>");
    	out.println("</html>");
  	}

    protected void postTurnSLEEPFailure(PrintWriter out)
  	{
    	out.println("<html>");
    	out.println("<head>");
    	out.println("<title>Predictor Switch</title>");
    	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
    	out.println("</head>");
    	out.println("<body bgcolor=\"#FFFFFF\">");
    	out.println("<H3><CENTER> Turn to SLEEP UnSuccessful !!</CENTER></H3>");
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
            int condition = 0;
            InterAgentOperatingMode iaom = new InterAgentOperatingMode("Servlet_Relay", new OMCRangeList(condition));
    		for (Iterator iterator = targets.iterator(); iterator.hasNext();)
			{
				String string_target = iterator.next().toString();
                MessageAddress target = MessageAddress.getMessageAddress(string_target);

              	//TestRelay relay = new TestRelay(myUIDService.nextUID(), agentId, target , "OFF" ,null);
                iaom.setTarget(target);
            	blackboard.openTransaction();
            	//blackboard.publishAdd(relay);
                blackboard.publishAdd(iaom);
            	blackboard.closeTransaction();
        	}
    		postSuccess(out);
		}
  	}

    public void printTurnON(PrintWriter out)
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
    		postTurnONFailure(out);
        	return;
    	}
    	else
    	{
    		for (Iterator iterator = targets.iterator(); iterator.hasNext();)
			{
				String string_target = iterator.next().toString();
                MessageAddress target = MessageAddress.getMessageAddress(string_target);
                int condition = 2;
                InterAgentOperatingMode iaom = new InterAgentOperatingMode("Servlet_Relay", new OMCRangeList(condition));
            	//TestRelay relay = new TestRelay(myUIDService.nextUID(), agentId, target , "ON" ,null);
                iaom.setTarget(target);
            	blackboard.openTransaction();
            	//blackboard.publishAdd(relay);
                blackboard.publishAdd(iaom);
            	blackboard.closeTransaction();
        	}
    		postTurnONSuccess(out);
		}
  	}

    public void printTurnSLEEP(PrintWriter out)
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
    		postTurnSLEEPFailure(out);
        	return;
    	}
    	else
    	{
    		for (Iterator iterator = targets.iterator(); iterator.hasNext();)
			{
				String string_target = iterator.next().toString();
                MessageAddress target = MessageAddress.getMessageAddress(string_target);
                int condition = 1;
                InterAgentOperatingMode iaom = new InterAgentOperatingMode("Servlet_Relay", new OMCRangeList(condition));
            	//TestRelay relay = new TestRelay(myUIDService.nextUID(), agentId, target , "SLEEP" ,null);
                iaom.setTarget(target);
            	blackboard.openTransaction();
            	//blackboard.publishAdd(relay);
                blackboard.publishAdd(iaom);
            	blackboard.closeTransaction();
        	}
    		postTurnSLEEPSuccess(out);
		}
  	}

    public void printTurnDefault(PrintWriter out)
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
    		postTurnSLEEPFailure(out);
        	return;
    	}
    	else
    	{
    		for (Iterator iterator = targets.iterator(); iterator.hasNext();)
			{
				String string_target = iterator.next().toString();
                MessageAddress target = MessageAddress.getMessageAddress(string_target);
                int condition = 3;
                InterAgentOperatingMode iaom = new InterAgentOperatingMode("Servlet_Relay", new OMCRangeList(condition));
            	//TestRelay relay = new TestRelay(myUIDService.nextUID(), agentId, target , "SLEEP" ,null);
                iaom.setTarget(target);
            	blackboard.openTransaction();
            	//blackboard.publishAdd(relay);
                blackboard.publishAdd(iaom);
            	blackboard.closeTransaction();
        	}
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
            printTurnDefault(out);
          }
          else if (action.equals(STATUS)) 
          {           
            publish(out);
          }
          else if (action.equals(STATUS1))
          {
            printTurnON(out);
          }
            else if (action.equals(STATUS2))
          {
            printTurnSLEEP(out);
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
  		out.println("<td><B>TURN OFF PREDICTOR</td>");
        out.println("</tr>");
  		out.println("<input type=\"submit\" name=\"" + ACTION_PARAM +
                "\" value=\"" + STATUS + "\">\n<BR>");
        out.println("<BR><tr>");
        out.println("<td><B>TOGGLE FOR COMMUNICATION LOSS</td>&nbsp");
        out.println("</tr>");
        out.println("<input type=\"submit\" name=\"" + ACTION_PARAM +
                "\" value=\"" + STATUS1 + "\">\n<BR>");
        out.println("<BR><tr>");
        out.println("<td><B>TURN PREDICTOR TO SLEEP MODE</td>&nbsp");
        out.println("</tr>");
        out.println("<input type=\"submit\" name=\"" + ACTION_PARAM +
                "\" value=\"" + STATUS2 + "\">\n<BR>");

  		printHtmlEnd(out);
  	}
  }
	
  public static final String ACTION_PARAM = "action";
  public static final String STATUS = "Submit OFF";
  public static final String STATUS1 = "Submit ON";
  public static final String STATUS2 = "Submit SLEEP";

  private HttpServletRequest request;
  private HttpServletResponse response;
  protected MessageAddress agentId;
  protected BlackboardService blackboard;
  protected LoggingService myLoggingService;
  protected CommunityService communityService;
  protected UIDService myUIDService;
  protected ServiceBroker serviceBroker;
  }
