package org.cougaar.tools.alf.sensor.plugin;

import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.util.UID;
import org.cougaar.core.agent.service.alarm.PeriodicAlarm;
import org.cougaar.core.service.community.CommunityService;
import org.cougaar.core.service.*;
import org.cougaar.util.*;
import org.cougaar.multicast.AttributeBasedAddress;
import org.cougaar.planning.ldm.plan.*;
import org.cougaar.glm.ldm.oplan.Oplan;
import org.cougaar.planning.ldm.measure.FlowRate;
import org.cougaar.planning.ldm.measure.CountRate;
import org.cougaar.glm.ldm.plan.AlpineAspectType;
import org.cougaar.glm.plugins.TimeUtils.*;
import org.cougaar.tools.alf.sensor.*;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.logistics.plugin.inventory.MaintainedItem;
import org.cougaar.glm.ldm.asset.Organization;
import org.cougaar.glm.ldm.plan.QuantityScheduleElement;
import org.cougaar.planning.ldm.plan.Schedule;
import org.cougaar.glm.ldm.asset.Inventory;
import org.cougaar.glm.ldm.asset.SupplyClassPG;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.mts.SimpleMessageAddress;

import org.cougaar.logistics.plugin.inventory.InventoryPolicy;
import org.cougaar.logistics.plugin.inventory.LogisticsInventoryPG;
import org.cougaar.glm.ldm.Constants;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
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
	IncrementalSubscription oPlanSubscription;
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
/*
        public void expire() {
//            expired = true;
			reset(currentTimeMillis());
			myBS.signalClientActivity(); 
			myLoggingService.shout("CALL PREDICTOR IN PERIODICALARM AT "+currentTimeMillis()/86400000);	
			called = true;
//			myBS.openTransaction();
//			callPredictor();
//				myBS.closeTransaction();

//			cancel();
        }
*/
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


    /** Selects the LogisticsOPlan objects   **/
/*
	private static class LogisticsOPlanPredicate implements UnaryPredicate {
      public boolean execute(Object o) {
        return o instanceof LogisticsOPlan;
      }
    }
*/
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
//            String type = getAssetType(logInvpg);
//
//			if (supplyType.equals(type)) {			
              return true;
//            }
          }
        }
        return false;
      }

//	  private String getAssetType(LogisticsInventoryPG invpg) {
//        Asset a = invpg.getResource();
//        if (a == null) return null;
//        SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
//        return pg.getSupplyType();
//      }

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
//			System.out.println("type = " + type);
            if (type.equals(this.type)) {
//              if (logger.isInfoEnabled()) {
//                logger.info("Found an inventory policy for " + this.type + "agent is: " + getMyOrganization());
//              }
              return true;
            } else {
//              if (logger.isDebugEnabled()) {
//                logger.debug("Ignoring type of: " + type + " in " + getMyOrganization() + " this type is: " + this.type);
//              }
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

	long baseTime = 13005; // August 10th 2005 

	boolean oplan_is_not_detected = true;
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
//		logisticsOPlanSubscription	= (IncrementalSubscription) bs.subscribe(new LogisticsOPlanPredicate());
//	    inventorySubscription		= (IncrementalSubscription) bs.subscribe(new InventoryPredicate("Ammunition"));
		relationSubscription = (IncrementalSubscription) bs.subscribe(relationPredicate);

		String dir = System.getProperty("org.cougaar.workspace");
		
		long forName = System.currentTimeMillis()/300000;

		try	{
			rstInv = new java.io.BufferedWriter ( new java.io.FileWriter(dir+"/"+ cluster + forName +".inv.txt", true ));
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't write data collecting file, io error" );
	    }						


//		CommunityService communityService = (CommunityService) getBindingSite().getServiceBroker().getService(this, CommunityService.class, null);
//		alCommunities = communityService.listParentCommunities(cluster, "(CommunityType=AdaptiveLogistics)");		

		createIndex();

		// build customer list
		customers = new Vector();
		buildCustomerList();

		// making a message
		InventoryInfo iInfo = new InventoryInfo(30,cluster,currentTimeMillis());
		arPluginMessage = new ARPluginMessage(transform(iInfo),cluster,uidservice.nextUID());
		arPluginMessage.setTime(currentTimeMillis());
		sendInventoryInfo(arPluginMessage);


//		bs.publishAdd(arPluginMessage);
		myLoggingService.shout("SupplierSideARPlugin start at " + cluster); 
		bs.setShouldBePersisted(false);

    }
	
	Vector customers;

	public void execute()
    {
        Iterator iter;
		
//		Collection tempcustomers = buildCustomerList();
//		if (tempcustomers.size()>0)	{
//			customers.addAll(tempcustomers);	
//		}

		long nowTime = currentTimeMillis();

		if (Rantime != (long) nowTime / 86400000)	{

			alarm = new TriggerFlushAlarm(nowTime+86400000);
			as.addAlarm(alarm);

			Rantime = (long) nowTime / 86400000;
		}

//		Collection c = bs.query(new InventoryPredicate("Ammunition"));
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

			long checkingTime = System.currentTimeMillis();

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

//				String type = "";
//				String outputStr ="";
				String nomenclature ="";
//				String typeId ="";
				Schedule invLevels = null;

				Asset a = logInvpg.getResource();
				if (a != null) {
					SupplyClassPG pg = (SupplyClassPG)  a.searchForPropertyGroup(SupplyClassPG.class);
//					type = pg.getSupplyType();
//					typeId = a.getTypeIdentificationPG().getTypeIdentification();
					nomenclature = a.getTypeIdentificationPG().getNomenclature(); 
				}

				if (getIndex(nomenclature)==-1)	{
					continue;
				}

				invLevels = logInvpg.getBufferedInvLevels(); 
		
				try
				{

					for (Enumeration en = invLevels.getAllScheduleElements() ; en.hasMoreElements() ;) {
				      QuantityScheduleElement invLevel = (QuantityScheduleElement) en.nextElement();
					  if (invLevel.getStartTime()/86400000 == (long) nowTime/86400000)
					  {
//							rstInv.write(nowTime/86400000 +"\t"+ checkingTime + "\t" + modifier + "\t" + type + "\t"+ nomenclature+ "\t" + typeId + "\t" 
//									+invLevel.getStartTime()/86400000+"\t"+invLevel.getEndTime()/86400000+"\t"+invLevel.getQuantity() +"\n");
							rstInv.write(nowTime/86400000 +"\t"+ checkingTime + "\t" + modifier + "\t" + nomenclature+ "\t"  
									+invLevel.getStartTime()/86400000+"\t"+invLevel.getEndTime()/86400000+"\t"+invLevel.getQuantity() +"\n");
							// for relay
							invInfo.setInventoryLevel(getIndex(nomenclature),invLevel.getQuantity());
							isChanged = true;
							break;
					  }
					}

					rstInv.flush();
				}
				catch (java.io.IOException ioexc)
				{
					System.err.println ("can't write file, io error" );
			    }					
			} // for
		
			if (isChanged)
			{
				arPluginMessage.setContent(transform(invInfo));
				arPluginMessage.setTime(currentTimeMillis());
				sendInventoryInfo(arPluginMessage);
				//bs.publishChange(arPluginMessage);
			}
	}

	private int getIndex(String nomenclature) {

		Integer index = (Integer) itemIndex.get(nomenclature);

		if (index!=null)
		{
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
//			ostream.close();

/// Test purpose
/*			
			ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
			ObjectInputStream pp = new ObjectInputStream(istream);

			iInfo = (InventoryInfo)pp.readObject();

			istream.close();

			if (iInfo != null)	{
			
				for (int i=0;i<iInfo.getNumberOfItems(); i++)	{
					myLoggingService.shout("[SupplierSideARPlugin]Inventory level "+i+" , " +  iInfo.getInventoryLevel(i));
				}
			
			} else {
				myLoggingService.shout("[SupplierSideARPlugin]InventoryInfo has problem.");
			}
*/
///// Test end
		
		}
		catch (java.io.IOException e)
		{
			myLoggingService.shout("[SupplierSideARPlugin]Error 1");	
		}
//		catch (java.lang.ClassNotFoundException e2)
//		{
//			myLoggingService.shout("[SupplierSideARPlugin]Error 3");	
//		}		

		return ostream;

	}

	public void sendInventoryInfo(ARPluginMessage arPluginMessage) 
	{
		if (!isPublishedBefore)
		{
			if (arPluginMessage == null)	{
				myLoggingService.shout("[SupplierSideARPlugin]arPluginMessage is null");
			}

//			Collection customers = getCustomerList();
			if (customers.size() == 0)
			{
				return;
			}

			for (Iterator iterator = customers.iterator(); iterator.hasNext();) {
				
				String agent = (String) iterator.next();
				myLoggingService.shout("[SupplierSideARPlugin]"+ cluster + " : I'd like to send message to "+agent);
//				AttributeBasedAddress aba = new AttributeBasedAddress().getAttributeBasedAddress(community, "Role", "AdaptiveLogisticsManager");
//				loadIndicator.addTarget(new AttributeBasedAddress(community,"Role","AdaptiveLogisticsManager"));
//				if (aba == null)	{
//					myLoggingService.shout("no destination");
//				}
//			for (Iterator iterator = alCommunities.iterator(); iterator.hasNext();) {
//				String community = (String) iterator.next();
//				arPluginMessage.addTarget(new AttributeBasedAddress().getAttributeBasedAddress(community, "Role", Constants.Role.AMMUNITIONCUSTOMER));
//				arPluginMessage.addTarget(new AttributeBasedAddress().getAttributeBasedAddress(community, "Role", "AmmunitionCustomer"));
//			}

//			for (Iterator iterator = alCommunities.iterator(); iterator.hasNext();) {
//				String community = (String) iterator.next();
//				arPluginMessage.addTarget(new AttributeBasedAddress().getAttributeBasedAddress(community, "Role", Constants.Role.FUELSUPPLYCUSTOMER));
//			}

//			new AttributeBasedAddress().getAttributeBasedAddress(community, "Role", "AdaptiveLogisticsManager")
				
//				arPluginMessage.addTarget(AttributeBasedAddress().getAttributeBasedAddress(community, "Role", "AdaptiveLogisticsManager");
				arPluginMessage.addTarget(SimpleMessageAddress.getSimpleMessageAddress(agent));
			}
//			arPluginMessage.addTarget(SimpleMessageAddress.getSimpleMessageAddress("47-FSB"));
			bs.publishAdd(arPluginMessage);
			myLoggingService.shout("[SupplierSideARPlugin]"+ cluster + ": add arPluginMessage to be sent to " + arPluginMessage.getTargets());
			isPublishedBefore = true;

		} else {
		
//	        arPluginMessage.setLoadStatus(loadlevel);
			bs.publishChange(arPluginMessage);
			myLoggingService.shout("[SupplierSideARPlugin]"+cluster + ": change inventory level to be sent to " + arPluginMessage.getTargets());
		}	
	}

//	private Collection getCustomerList() {
//		return ;
//	}
/*
	private Collection buildCustomerList() {

		Vector customerList = new Vector();

		for (Enumeration et = relationSubscription.getAddedList(); et.hasMoreElements();) {
			HasRelationships org = (HasRelationships) et.nextElement();
	        RelationshipSchedule schedule = org.getRelationshipSchedule();

		    Collection ammo_customer = schedule.getMatchingRelationships(Constants.Role.AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
//		    Collection ammo_customer = schedule.getMatchingRelationships(AMMUNITIONCUSTOMER); //Get a collection of ammunition customers
			//Collection food_customer = schedule.getMatchingRelationships(Constants.Role.FOODCUSTOMER);
	        Collection fuel_customer = schedule.getMatchingRelationships(Constants.Role.FUELSUPPLYCUSTOMER);
//	        Collection fuel_customer = schedule.getMatchingRelationships(FUELSUPPLYCUSTOMER);
		    //Collection packpol_customer = schedule.getMatchingRelationships(Constants.Role.PACKAGEDPOLSUPPLYCUSTOMER);
			//Collection spareparts_customer = schedule.getMatchingRelationships(Constants.Role.SPAREPARTSCUSTOMER);
			//Collection subsistence_customer = schedule.getMatchingRelationships(Constants.Role.SUBSISTENCESUPPLYCUSTOMER);

            for (Iterator iter = ammo_customer.iterator(); iter.hasNext();) {
                Relationship orgname = (Relationship) iter.next();
                Asset subOrg = (Asset) schedule.getOther(orgname);
                String role = schedule.getOtherRole(orgname).getName();
                String org_name = subOrg.getClusterPG().getMessageAddress().toString();
				customerList.add(org_name);
            }
		    
            for (Iterator iter = fuel_customer.iterator(); iter.hasNext();) {
                Relationship orgname = (Relationship) iter.next();
                Asset subOrg = (Asset) schedule.getOther(orgname);
                String role = schedule.getOtherRole(orgname).getName();
                String org_name = subOrg.getClusterPG().getMessageAddress().toString();
				customerList.add(org_name);
            }
		}

		return customerList;
	}
*/

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