package org.cougaar.tools.alf.sensor.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cougaar.util.log.Logging;

public class InventoryInfo implements java.io.Serializable {
  
  private double inventoryLevel[] = null;
  private long inventoryTime = 0;
  private String agentName = null;
  private int numberOfItems = 0;

  public InventoryInfo(int numberOfItems, String agentName, long inventoryTime) {
    super();
	inventoryLevel = new double[numberOfItems]; 
	this.inventoryTime = inventoryTime;
	this.agentName = agentName;
	this.numberOfItems = numberOfItems;
  }

  public String getAgentName() {
    return agentName;
  }

  public long getTime() {
	return inventoryTime;
  }

  public double getInventoryLevel(int item) {
    return inventoryLevel[item];
  }

  public void setInventoryLevel(int item, double ilevel) {
    inventoryLevel[item] = ilevel;
  }

  public int getNumberOfItems() {
	return numberOfItems;
  }
}