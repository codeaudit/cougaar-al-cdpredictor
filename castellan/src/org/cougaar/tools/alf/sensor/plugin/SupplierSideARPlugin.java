package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.*;
import org.cougaar.util.*;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.tools.alf.sensor.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.SupplyClassPG;
import org.cougaar.core.mts.SimpleMessageAddress;

import org.cougaar.logistics.plugin.inventory.InventoryPolicy;
import org.cougaar.logistics.plugin.inventory.LogisticsInventoryPG;

import java.util.*;
import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.text.DateFormat;
import java.text.ParseException;
import java.io.*;

/* 
 *	programed by Yunho Hong
 *	August 30, 2003
 *	Pennsylvania State University
 */

public class SupplierSideARPlugin extends ComponentPlugin
{

	IncrementalSubscription taskSubscription;
	IncrementalSubscription planelementSubscription;
	IncrementalSubscription inventorySubscription, relationSubscription;
	IncrementalSubscription inventoryPolicySubscription;

	private HashMap itemIndex = null;

	boolean called = false;
    class TriggerFlushAlarm implements PeriodicAlarm {

        public TriggerFlushAlarm(long expTime) {
            this.expTime = expTime;
        }

        public void reset(long currentTime) {
            expTime = currentTime + delay;
            expired = false;
        }

        public long getExpirationTime() {
            return expTime;
        }

        public void expire() {
            expired = true;
			bs.signalClientActivity(); 
			myLoggingService.shout("CALL plugin IN PERIODICALARM AT "+currentTimeMillis()/86400000);	
			cancel();
        }

        public boolean hasExpired() {
            return expired;
        }

        public boolean cancel() {
            boolean was = expired;
            expired = true;
            return was;
        }

        boolean expired = false;
        long expTime;
        long delay = 86400000;
    };


    /**  Passes Inventory assets that have a valid LogisticsInventoryPG   **/
    private static class InventoryPredicate implements UnaryPredicate {
      String supplyType;
    
      public InventoryPredicate(String type) {
        supplyType = type;
      }
    
      public boolean execute(Object o) {
        if (o instanceof Inventory) {
          Inventory inv = (Inventory) o;
          LogisticsInventoryPG logInvpg = (LogisticsInventoryPG) inv.searchForPropertyGroup(LogisticsInventoryPG.class);
          if (logInvpg != null) {
			  // Here, it will return true for any logistics inventory information. 
              return true;
          }
        }
        return false;
      }
	}

    private String getAssetType(LogisticsInventoryPG invpg) {
      Asset a = invpg.getResource();
      if (a == null) return null;
      SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
      return pg.getSupplyType();
    }

	private class InventoryPolicyPredicate implements UnaryPredicate {
		String type;

	    public InventoryPolicyPredicate(String type) {
		  this.type = type;
	    }

        public boolean execute(Object o) {
          if (o instanceof org.cougaar.logistics.plugin.inventory.InventoryPolicy) {
            String type = ((InventoryPolicy) o).getResourceType();
            if (type.equals(this.type)) {
              return true;
            } 
          }
          return false;
        }
	}

    UnaryPredicate relationPredicate = new UnaryPredicate() {
        public boolean execute(Object o) {
            if (o instanceof HasRelationships) {
                return ((HasRelationships) o).isLocal();
            } else {
                return false;
            }
        }
    };

	BlackboardService bs;
   	UIDService uidservice;

   	String cluster;  // the current agent's name
	
	double [] a;

	boolean isPublishedBefore = false;

	java.io.BufferedWriter rst = null, rstInv=null;
	LoggingService myLoggingService = null;
	ARPluginMessage arPluginMessage = null;

	Collection alCommunities = null;
    private long Rantime = 0;

    public void setupSubscriptions()   {

        bs = getBlackboardService();
		cluster = agentId.toString(); // the cluster where this Plugin is running.
		
		myLoggingService = (LoggingService) getBindingSite().getServiceBroker().getService(this, LoggingService.class, null);
        uidservice = (UIDService) getBindingSite().getServiceBroker().getService(this,UIDService.class, null);
        as = (AlarmService) getBindingSite().getServiceBroker().getService(this, AlarmService.class, null);

	    inventoryPolicySubscription = (IncrementalSubscription) bs.subscribe(new InventoryPolicyPredicate("Ammunition"));
		relationSubscription		= (IncrementalSubscription) bs.subscribe(relationPredicate);

		createIndex();

		// build customer list
		customers = new Vector();
		buildCustomerList();

		// making a message
		InventoryInfo iInfo = new InventoryInfo(30,cluster,currentTimeMillis());
		arPluginMessage = new ARPluginMessage(iInfo,cluster,uidservice.nextUID());
		arPluginMessage.setTime(currentTimeMillis());
		sendInventoryInfo(arPluginMessage);

		myLoggingService.shout("SupplierSideARPlugin start at " + cluster); 

    }
	
	Vector customers;

    public void execute()
    {
		long nowTime = currentTimeMillis();

		if (Rantime != (long) nowTime / 86400000)	{

			alarm = new TriggerFlushAlarm(nowTime+86400000);
			as.addAlarm(alarm);

			Rantime = (long) nowTime / 86400000;
		}

		Collection c = bs.query(new InventoryPredicate("All"));

		if (c!=null)	{	
			printInventory(c.size(), c.iterator(), "added",nowTime);		
		}

	}

	private void checkInventorySubscription(long nowTime, IncrementalSubscription inventorySubscription)
	{
		if (!inventorySubscription.isEmpty()) {
			Collection c1 = inventorySubscription.getAddedCollection();
			if (c1!=null)	{	printInventory(c1.size(), c1.iterator(), "added",nowTime);		}

			Collection c2 = inventorySubscription.getRemovedCollection();
			if (c2!=null)	{	printInventory(c2.size(), c2.iterator(), "removed",nowTime);	} 

			Collection c3 = inventorySubscription.getChangedCollection();
			if (c3!=null)	{	printInventory(c3.size(), c3.iterator(), "changed",nowTime);	} 
		}
	}

	private void printInventory(int nInventories, Iterator inventortyIter, String modifier, long nowTime)
	{
			boolean isChanged = false;
			InventoryInfo invInfo = new InventoryInfo(30,cluster,nowTime);

			for (int i = 0; i < nInventories; i++) {
				Inventory inv = (Inventory)inventortyIter.next();
				LogisticsInventoryPG logInvpg = (LogisticsInventoryPG) inv.searchForPropertyGroup(LogisticsInventoryPG.class);

				if (logInvpg != null) {
		            String type = getAssetType(logInvpg);
					if (!type.equals("Ammunition")&&!type.equals("BulkPOL")) {			
				      continue;
					}
		        } else {
					continue;
				}

				String nomenclature ="";
				Schedule invLevels = null;

				Asset a = logInvpg.getResource();
				if (a != null) {
					nomenclature = a.getTypeIdentificationPG().getNomenclature(); 
				}

				if (getIndex(nomenclature)==-1)	{
					continue;
				}

				invLevels = logInvpg.getBufferedInvLevels(); 

			} // for
		
			if (isChanged)	{
				arPluginMessage.setContent(invInfo);
				arPluginMessage.setTime(currentTimeMillis());
				sendInventoryInfo(arPluginMessage);
			}
	}

	private int getIndex(String nomenclature) {

		Integer index = (Integer) itemIndex.get(nomenclature);

		if (index!=null)	{
			return index.intValue();
		} 

		myLoggingService.shout(cluster+": no index " + nomenclature);		
		return -1;

	}

	private void createIndex() {

		itemIndex = new HashMap();

		itemIndex.put("120MM APFSDS-T M829A1",new Integer(0));
		itemIndex.put("120MM HE M934 W/MO FZ",new Integer(1));
		itemIndex.put("120MM HEAT-MP-T M830",new Integer(2));
		itemIndex.put("155MM ADAM M731",new Integer(3));
		itemIndex.put("155MM HE M107",new Integer(4));
		itemIndex.put("155MM ICM M483A1 2ND",new Integer(5));
		itemIndex.put("155MM ILLUM M485 SERI",new Integer(6));
		itemIndex.put("155MM M864 BASEBURNER",new Integer(7));
		itemIndex.put("155MM RAAMS M718A1",new Integer(8));
		itemIndex.put("155MM RAAMS M741A1",new Integer(9));
		itemIndex.put("155MM RAP M549 SERIES",new Integer(10));
		itemIndex.put("155MM SMOKE SCR M825/",new Integer(11));
		itemIndex.put("25MM APFSDS-T M919",new Integer(12));
		itemIndex.put("Ctg 120mm Illum XM930",new Integer(13));
		itemIndex.put("Ctg 120mm Smoke XM929",new Integer(14));
		itemIndex.put("CTG 25MM HEI-T M792",new Integer(15));
		itemIndex.put("CTG CAL .50 M8",new Integer(16));
		itemIndex.put("DEMO KIT MICLIC)",new Integer(17));
		itemIndex.put("FUZE MT",new Integer(18));
		itemIndex.put("FUZE PD M739",new Integer(19));
		itemIndex.put("FUZE PROX M732",new Integer(20));
		itemIndex.put("GRENADE: SMOKE IR M76",new Integer(21));
		itemIndex.put("PROP CHARGE M119A2",new Integer(22));
		itemIndex.put("PROP CHARGE M203",new Integer(23));
		itemIndex.put("PROP CHARGE M3A1",new Integer(24));
		itemIndex.put("PROP CHARGE M4A2",new Integer(25));
		itemIndex.put("SMOKE RP L8A1/L8A3",new Integer(26));
		itemIndex.put("TOW: BGM-71F",new Integer(27));
		itemIndex.put("JP8",new Integer(28));
		itemIndex.put("DF2",new Integer(29));

	}

	private ByteArrayOutputStream transform(InventoryInfo iInfo) {

		ByteArrayOutputStream ostream = null;
		try
		{
			ostream = new ByteArrayOutputStream();
			ObjectOutputStream p = new ObjectOutputStream(ostream);

			p.writeObject(iInfo);
			p.flush();

			myLoggingService.shout("[SupplierSideARPlugin] "+cluster+" Size of ostream = " + ostream.size());

		}
		catch (java.io.IOException e)
		{
			myLoggingService.shout("[SupplierSideARPlugin]Error 1");	
		}

		return ostream;

	}

	public void sendInventoryInfo(ARPluginMessage arPluginMessage) 
	{
		if (!isPublishedBefore)
		{
			if (arPluginMessage == null)	{
				myLoggingService.shout("[SupplierSideARPlugin]arPluginMessage is null");
			}

			if (customers.size() == 0)	{
				return;
			}

			for (Iterator iterator = customers.iterator(); iterator.hasNext();) {
				
				String agent = (String) iterator.next();
//				myLoggingService.shout("[SupplierSideARPlugin]"+ cluster + " : I'd like to send message to "+agent);

				arPluginMessage.addTarget(SimpleMessageAddress.getSimpleMessageAddress(agent));
			}

			bs.publishAdd(arPluginMessage);
//			myLoggingService.shout("[SupplierSideARPlugin]"+ cluster + ": add arPluginMessage to be sent to " + arPluginMessage.getTargets());
			isPublishedBefore = true;

		} else {
		
			bs.publishChange(arPluginMessage);
//			myLoggingService.shout("[SupplierSideARPlugin]"+cluster + ": change inventory level to be sent to " + arPluginMessage.getTargets());
		}	
	}

	private void buildCustomerList() {
		
		if (cluster.equalsIgnoreCase("123-MSB"))	{
			customers.add("47-FSB");
		} else if (cluster.equalsIgnoreCase("47-FSB"))	{
			customers.add("1-35-ARBN");
			customers.add("1-6-INFBN");
		}
	}

	AlarmService as;
    TriggerFlushAlarm alarm = null;
}