package org.cougaar.tools.alf.sensor.plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.util.log.Logging;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.util.UID;
import org.cougaar.logistics.plugin.manager.RelayAdapter;

public class ARPluginMessage extends RelayAdapter {

  public int num_of_tasks;
  private long time;

//  private ByteArrayOutputStream byteArrayOutputStream = null;
  private InventoryInfo byteArrayOutputStream = null;

  private String agentName = null;
  private String myReportingSensorClassName = null;
  private String myLoadStatus = null;
  private transient String myToString = null;

//  public ARPluginMessage(ByteArrayOutputStream byteArrayOutputStream , String agentName, UID uid) {
  public ARPluginMessage(InventoryInfo byteArrayOutputStream , String agentName, UID uid) {
    super();
	this.byteArrayOutputStream = byteArrayOutputStream;
    this.agentName = agentName;
    setUID(uid);
  }

  public String getAgentName() {
    return agentName;
  }

  public void setTime(long time) {
	this.time = time;
  }

  public long getTime() {
	return time;
  }

  public void setContent(InventoryInfo byteArrayOutputStream ) {
	this.byteArrayOutputStream = byteArrayOutputStream;
  }

//  public ByteArrayOutputStream getInventoryInfo() {
//	return byteArrayOutputStream;
//  } 

  public InventoryInfo getInventoryInfo() {
	return byteArrayOutputStream;
  } 

  public boolean equal(ARPluginMessage newARPluginMessage)	{

	  if (time == newARPluginMessage.getTime())  {
		  return true;
	  }

	return false;

  }

  protected boolean contentChanged(RelayAdapter newARPluginMessage) {
    ARPluginMessage arPluginMessage = (ARPluginMessage) newARPluginMessage;

    // Only the load status should actually change   
    if (!equal(arPluginMessage)) {
      setContent(arPluginMessage.getInventoryInfo());
      return true;
    } else {
      return (super.contentChanged(newARPluginMessage));
    }
  }
}