/*
  * Yunho Hong
  * email : yyh101@psu.edu
  * PSU, August , 2003
*/

package org.cougaar.tools.alf.predictor.plugin;

import org.cougaar.core.util.UID;
import org.cougaar.logistics.plugin.manager.RelayAdapter;

public class ARPluginMessage extends RelayAdapter {

  public int num_of_tasks;
  private long time;

  private InventoryInfo byteArrayOutputStream = null;

  private String agentName = null;

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

    if (!equal(arPluginMessage)) {
      setContent(arPluginMessage.getInventoryInfo());
      return true;
    } else {
      return (super.contentChanged(newARPluginMessage));
    }
  }
}