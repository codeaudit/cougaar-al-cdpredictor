package org.cougaar.cpe.agents.plugin.control;

import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.util.*;
import org.cougaar.tools.techspecs.qos.ControlMeasurement;
import org.cougaar.core.mts.MessageAddress;

import org.cougaar.cpe.agents.messages.ControlMessage;
/*
 * @author nathan
 * talks to Matlab and Arena to help the QueueingModel plugin 
 */
public class QueueingParameters extends ControlMeasurement{
	public QueueingParameters(long ts, HashMap candidateOpmodes, double[][] estimatedtt ) {
		super("SystemPurturbed","Control",MessageAddress.getMessageAddress("BDE1"),ts,candidateOpmodes,estimatedtt);
		this.cm= new ControlMessage("SystemPurturbed","Control");
		cm.putControlSet(candidateOpmodes);
		actualMPF=0;
		modelMG1MPF=0;
		modelWhittMPF=0;
		score=0;
	}
	
	public QueueingParameters(long ts, HashMap candidateOpmodes, HashMap estimatedtt ) {
			super("SystemPurturbed","Control",MessageAddress.getMessageAddress("BDE1"),ts,candidateOpmodes,estimatedtt);
			this.cm= new ControlMessage("SystemPurturbed","Control");
			cm.putControlSet(candidateOpmodes);
		}
	
	public void getMG1Estimate(){
		modelMG1MPF=0; //set to calculated value later
	}
	
	public void getWhittEstimate(){
		modelWhittMPF=0; //set to calculated value later
	}
	
	public void toString(StringBuffer buf){
		super.toString(buf);
		buf.append("MPF= "+actualMPF);
	}
	
	
	private ControlMessage cm;
	private double actualMPF, modelMG1MPF, modelWhittMPF;
	private double score;	
}