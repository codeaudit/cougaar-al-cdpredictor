package org.dbinterface;

import org.dbinterface.*;

import org.hydra.pdu.*;

import java.util.*;

import java.sql.*;

import org.gjt.mm.mysql.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Title: DB Interface
 * @author Yunho Hong
 * @version 1.0
 */

public interface PlanDatabase {

  public static final int TYPE_DIRECTIVE = -1;
  public static final int TYPE_NONE = 0 ;
  public static final int TYPE_TASK = 1 ;
  public static final int TYPE_ALLOCATION = 2 ;
  public static final int TYPE_ASSET = 3 ;
  public static final int TYPE_EXPANSION = 4 ;
  public static final int TYPE_AGGREGATION = 5 ;
  public static final int TYPE_ALLOCATION_RESULT = 6 ;
  public static final int TYPE_PLAN_ELEMENT = 7 ;

  /** DATABASE NAME */
//  public static String DB;

  /**
   * ENTITIES: Database table name
   */
  public final static String TASK            = "TASK";
  public final static String PLAN_ELEMENT    = "PLAN_ELEMENT";
  public final static String ASSET           = "ASSET";
  public final static String ALLOC_RESULT    = "ALLOC_RESULT";
  public final static String PREFERENCE      = "PREFERENCE";
  public final static String SCORETABLE      = "SCORE";

  public final static String EVENTPDU           = "EVENTPDU";

  /**
   *  RELATIONSHIP: Database table name
   */
  public final static String ALLOC_TASK_ON   = "ALLOC_TASK_ON";
  public final static String PROCESSED_BY    = "PROCESSED_BY";

  /**
   * Miscellanenous
   */
  public final static String SIDTABLE        = "SIDTABLE";

  public void establishConnection();
  public void createDatabase(String DataBaseName);
  public void closeConnection() ;
  public void createTables(String DefFileName);
  public void inputTestData();
  public void showsTables();
  public void useDataBase(String DataBaseName);

  public void makeSIDTable(String iniFile);

  /**
   * Newly added functions for hydra, Sept. 10, 2001
   * Data input function for all objects for newcastellan
   */

  public void storeEventPDUIntoDb(EventPDU pdu);
  public void storePDUByTime(EventPDU pdu, int TYPE);
  public void storeTaskPDUIntoDb(TaskPDU pdu);
  public void storeAssetPDUIntoDb(AssetPDU pdu);
  public void storeAllocationPDUIntoDb(AllocationPDU pdu);
  public void storeExpansionPDUIntoDb(ExpansionPDU pdu);
  public void storeAggregationPDUIntoDb(AggregationPDU pdu);
  public void storeAllocationResultPDUIntoDb(AllocationResultPDU pdu);

  /**
   * Create time series data from the dumped data
   */
  //  1. Number of tasks to be allocated
  public void generateTimeSeries_Numberoftasks(String Cluster);

  //  2. Time to allocate : time taken for a task to be allocated to a cluster or an asset
  public void generateTimeSeries_TimetoAllocate(String Cluster);

  //  3. Time to respond : time taken to report result about the allocated task.
  public void generateTimeSeries_TimetoRespond(String Cluster);

  //  4. Task arrival rate
  public void generateTimeSeries_TaskArrivalRate(String Cluster);

  //  5. Task interarrival time
  public void generateTimeSeries_TaskInterarrivalTime(String Cluster);

  //  6. Local generation rate
  public void generateTimeSeries_LocalGenerationRate(String Cluster);

  //  7. Rescind rate
  public void generateTimeSeries_RescindRate(String Cluster);

  //  8. Allocation rate
  public void generateTimeSeries_AllocationRate(String Cluster);

  //  9. Allocation success rate
  public void generateTimeSeries_AllocationSuccessRate(String Cluster);

  // 10. Tasks generated without allocation result
  public void generateTimeSeries_TaskNoAR(String Cluster);

  // 11. Percentage for the above aspsects
  // public void generateTimeSeries_Numberoftasks(String Cluster);

  /**
   * Data search function
   */
  public Vector  getParentTask(int TaskUID, int SID, long Time);          /* Return the lists of Parent Task UID at specific time */
  public Vector  getChildTask(int TaskUID, int SID, long Time);           /* Return the lists of Child Task UID at specific time */
  public String  getAllocatedAsset(int TaskUID, int SID, long Time);      /* Return AssetUID where Task is allocated at specific time */

  public EventList getEventsBetween(long start, long end, int type);
  public int getNumEventsBetween( long start, long end ) ;
  
  public int getNumUniqueUIDs( long start, long end, int type ) ;
  public int getNumUniqueUIDs( long start, long end ) ;
  
  public long getFirstEventTime();
  public long getLastEventTime();

  /* TBD.    The additional functions for data analysis should be devised*/
  public void startLogging();

  public void compare(Iterator i, long start, long end, int type);


  }
