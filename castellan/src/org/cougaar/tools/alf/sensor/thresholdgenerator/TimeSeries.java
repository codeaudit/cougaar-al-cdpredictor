package org.cougaar.tools.alf.sensor.thresholdgenerator;

import org.cougaar.tools.alf.sensor.thresholdgenerator.*;

import java.util.*;
import java.sql.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

import org.gjt.mm.mysql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Title:        
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PSU
 * @author Yunho Hong
 * @version 1.0
 */

public class TimeSeries {

	/** JDBC Connection */
	protected java.sql.Connection con;

	/** Statement */
	protected java.sql.Statement PdStatement;
	protected java.sql.Statement PdStatement2;
	protected java.sql.Statement PdStatement3;
	
	long start_time, end_time;

	// Constructor 
	public void TimeSeries() {
		start_time = 0;
		end_time = 0;
	}

	public void useDataBase(String DatabaseName) {

//		System.out.println("Use database");
		 try {
           PdStatement.executeQuery("USE " + DatabaseName);
        } catch (SQLException E) {
//        	System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }

//		System.out.println("Find the start time of planning");
		try {
			
			java.sql.ResultSet rs1 = PdStatement.executeQuery("select min(teventtime) as teventtime from task where verb= 'GetLogSupport' and teventtime >= 0");
			
			if (rs1.next())		{	start_time = rs1.getLong("teventtime");		}

//			System.out.println("start_time = " + start_time);

        } catch (SQLException E) {
//        	System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }
	}
	
	public void closeConnection() {
		try {
	      con.close() ;
		}
	    catch ( Exception e ) {
		  e.printStackTrace() ;
	    }
	}
		
////// Generating time series by cluster
	// June 1, 2002
	//
	public float [][] generateTSNumTasksAtCluster(String cluster, int classno, int unittime){

		String table1 = "task_generation";
		String table2 = "tt";
//		String specify_cluster = " and source = '"+ cluster + "' ";
		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{
			// check temporary files and delete them
			java.sql.ResultSet rs = PdStatement.executeQuery("show tables");

//			System.out.println("classno = " + classno);

			while (rs.next())
			{
//				System.out.print(rs.getString(1) + " ");
				
				if (table1.equalsIgnoreCase(rs.getString(1)))
				{
					PdStatement.executeQuery("drop table " + table1);
				} else if (table2.equalsIgnoreCase(rs.getString(1)))
				{
					PdStatement.executeQuery("drop table " + table2);
				}
			}

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and verb <> 'ReportForDuty' and verb <> 'ReportForService' and teventtime >= 0 ");
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
//				System.out.println(start_time + ", " + starttime);
			} else {
//				System.out.println("Something wrong in the task table ");
				return null;
			}

			// generating time series
			if (classno == 0)
			{
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + " and verb = 'Transport'" +
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			} else if (classno >= 1 && classno < 10){
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + " and DIRECTOBJECT = " + classno  + 
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			
			} else if (classno == 10)
			{
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + 
											" and verb <> 'ReportForDuty' and verb <> 'ReportForService'" +
											" and verb <> 'GetLogSupport'" +
										" and source='" + cluster + "' and teventtype=0 group by teventtime");
			} else {
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + 
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			}

			java.sql.ResultSet rs1 = PdStatement.executeQuery("select * from " + table2);
			Vector t = new Vector();
			Vector n = new Vector();

			int x = 1;
			int ce = 0;  // cumulative number of tasks
			while(rs1.next())
			{
				if (unittime == 0)
				{
					t.add(new Float((float) (rs1.getLong(1)- starttime)));
					n.add(new Float((float) rs1.getInt(2)));
				} else {
					
					if (x*unittime >= rs1.getLong(1)- starttime)
					{
						ce = ce + rs1.getInt(2);
					} else {

						while (x*unittime < rs1.getLong(1)- starttime)
						{
							t.add(new Float((float) x*unittime));
							n.add(new Float((float) ce));
							ce = 0;
							x++;
						}

					}
				}
//				System.out.println(rs1.getLong(1));
			} 

			int s = t.size();
			
			if (s > 0)
			{
				TimeSeries = new float[2][];

				TimeSeries[0] = new float[s] ;
				TimeSeries[1] = new float[s] ;
	
				TimeSeries[0][0] = ((Float) t.get(0)).floatValue(); 
				TimeSeries[1][0] = ((Float) n.get(0)).floatValue(); 

				for (int i=1;i<s;i++ )
				{
					TimeSeries[0][i] = ((Float) t.get(i)).floatValue(); 
					TimeSeries[1][i] = TimeSeries[1][i-1] + ((Float) n.get(i)).floatValue(); 
//					TimeSeries[1][i] = ((Float) n.get(i)).floatValue(); 
//					System.out.println(TimeSeries[0][i]+", "+TimeSeries[1][i]);
				}
			}
			
			PdStatement.executeUpdate("DROP TABLE " + table2);

		} catch (SQLException E) {
//        System.out.println("SQLException: " + E.getMessage());
//        System.out.println("SQLState:     " + E.getSQLState());
//        System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }

////// Generating time series by cluster
	// June 22, 2002
	//
/* Only the following agents have the GetLogSupport task.
+------------------+---------------+
| source           | teventtime    |
+------------------+---------------+
| 1-27-FABN        | 1023977646720 |
| 1-35-ARBN        | 1023977646573 |
| 1-6-INFBN        | 1023977646570 |
| 1-AD             | 1023977646086 |
| 102-POL-SUPPLYCO | 1023977675597 |
| 110-POL-SUPPLYCO | 1023977645975 |
| 123-MSB          | 1023977646386 |
| 125-ORDBN        | 1023977647157 |
| 191-ORDBN        | 1023977645465 |
| 21-TSC-HQ        | 1023977645382 |
| 240-SSCO         | 1023977647676 |
| 3-SUPCOM-HQ      | 1023977645579 |
| 47-FSB           | 1023977646572 |
| 485-CSB          | 1023977672812 |
| 5-CORPS-ARTY     | 1023977645575 |
| 592-ORDCO        | 1023977675604 |
| 900-POL-SUPPLYCO | 1023977647599 |
| TRANSCOM         | 1023977646025 |
+------------------+---------------+
*/
	public float [][] NumTasksFromGLSAtCluster(String cluster, int classno, int unittime, int flag){

		String table1 = "task_generation";
		String table2 = "tt";
//		String specify_cluster = " and source = '"+ cluster + "' ";
		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{
			// check temporary files and delete them
			java.sql.ResultSet rs = PdStatement.executeQuery("show tables");

//			System.out.println("classno = " + classno);

			while (rs.next())
			{
//				System.out.print(rs.getString(1) + " ");
				
				if (table1.equalsIgnoreCase(rs.getString(1)))
				{
					PdStatement.executeQuery("drop table " + table1);
				} else if (table2.equalsIgnoreCase(rs.getString(1)))
				{
					PdStatement.executeQuery("drop table " + table2);
				}
			}

			// get the time of first task
//			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0");

			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and verb <> 'ReportForDuty' and verb <> 'ReportForService' and teventtime >= 0 ");			

			if (rs0.next())
			{
				starttime = rs0.getLong(1);
//				System.out.println("the first GLS in society appears at " +start_time + ", the first GLS in this agent appears at" + starttime);  // start_time : for the first GLS, starttime : for this agent's first GLS 
			} else {
//				System.out.println("Something wrong in the task table ");
				return null;
			}

			// generating time series
			if (classno == 0)
			{
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + " and verb = 'Transport'" +
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			} else if (classno >= 1 && classno < 10){
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + " and DIRECTOBJECT = " + classno  + 
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			
			} else if (classno == 10)
			{
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + 
										" and verb <> 'Finish' and verb <> 'Sample'" +
											" and verb <> 'Ready' and verb <> 'ReportForDuty'" +
											" and verb <> 'ReportForService' and verb <> 'Start'" +
											" and verb <> 'GetLogSupport'" +
										" and source='" + cluster + "' and teventtype=0 group by teventtime");
			} else {
				PdStatement2.executeUpdate("create table " + table2 + " as select teventtime, count(*) from task " +
										"where teventtime >= " + starttime + 
										" and source = '" + cluster + "' and teventtype = 0 group by teventtime");
			}

			java.sql.ResultSet rs1 = PdStatement.executeQuery("select * from " + table2 + " order by teventtime");
			Vector t = new Vector();
			Vector n = new Vector();

			int x = 1;
			int ce = 0;  // cumulative number of tasks
			while(rs1.next())
			{
				if (unittime == 0)
				{
					if (flag == 1)
					{
						t.add(new Float((float) (rs1.getLong(1)- starttime)));  // All the time stamp is subtraced by starttime of this agent(GLS Task eventtime).
					} else {
						t.add(new Float((float) (rs1.getLong(1)- start_time)));  // All the time stamp is subtraced by starttime of this society.
					}

					n.add(new Float((float) rs1.getInt(2)));
				} else {
					
					if (x*unittime >= rs1.getLong(1)- starttime)
					{
						ce = ce + rs1.getInt(2);
					} else {

						while (x*unittime < rs1.getLong(1)- starttime)
						{
							t.add(new Float((float) x*unittime));
							n.add(new Float((float) ce));
							ce = 0;
							x++;
						}

					}
				}
//				System.out.println(rs1.getLong(1));
			} 

			int s = t.size();
			
			if (s > 0)
			{
				TimeSeries = new float[2][];

				TimeSeries[0] = new float[s] ;
				TimeSeries[1] = new float[s] ;
	
				TimeSeries[0][0] = ((Float) t.get(0)).floatValue(); 
				TimeSeries[1][0] = ((Float) n.get(0)).floatValue(); 

				for (int i=1;i<s;i++ )
				{
					TimeSeries[0][i] = ((Float) t.get(i)).floatValue();						 // time 
					TimeSeries[1][i] = TimeSeries[1][i-1] + ((Float) n.get(i)).floatValue(); // num of tasks
//					TimeSeries[1][i] = ((Float) n.get(i)).floatValue(); 
//					System.out.println(TimeSeries[0][i]+", "+TimeSeries[1][i]);
				}
			}
			
			PdStatement.executeUpdate("DROP TABLE " + table2);

		} catch (SQLException E) {
//          System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }


	private void checkAndDelete(String tablename) {

		try 
		{
				// check temporary files and delete them
				java.sql.ResultSet rs = PdStatement.executeQuery("show tables");

				while (rs.next())
				{
					if (tablename.equalsIgnoreCase(rs.getString(1)))
					{
						PdStatement.executeQuery("drop table " + tablename);
					} 
				}
		} catch (SQLException E) {
//		  System.out.println("checkAndDelete");
          System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }
	}

////// Generating time series for check waiting time to allocation.
	// June 4, 2002
	// New Version for speed improvement June 7, 2002
	public float [][] generateTSofWaitingTime(String cluster, int classno, int unittime){

		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and teventtime > " + start_time);
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
//				System.out.println(start_time + ", " + starttime);
			} else {
//				System.out.println("Something wrong in the task table ");
				return null;
			}

			// check whether there are same name of tables
			checkAndDelete("tasklist");
			checkAndDelete("tt");
			checkAndDelete("finishlist");

			// prepare task list
//			System.out.println("A");
			String query = "CREATE TABLE TASKLIST ( EVENTTIME BIGINT NOT NULL, " +
												  " TASKUID BIGINT NOT NULL, " +
												  " TASKSID VARCHAR(20) NOT NULL, " +
												  " FINISHTIME BIGINT NOT NULL )";
			PdStatement.executeQuery( query ) ;

//			System.out.println("A-1");
			String s = "insert into tasklist (eventtime, taskuid, tasksid) select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' and verb <> 'Sample' and verb <> 'Finish' and verb <> 'Start' and verb <> 'Ready' order by teventtime";
//			String s = "create table tasklist as select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid, 12345678901234567 from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' order by teventtime";
			PdStatement2.executeUpdate(s);

			// prepare for finishlist from plan element
//			System.out.println("B");
			s = "create table tt as select (peventtime-"+ starttime +") as eventtime, taskuid, tasksid from processed_by where peventtype =0 and peventtime > "+ starttime +" and PROCESSED = 1 order by peventtime";
			PdStatement2.executeUpdate(s);

			// group by taskuid, tasksid.
//			System.out.println("C");
			s = "create table finishlist as select eventtime, taskuid, tasksid from tt group by taskuid, tasksid";
			PdStatement2.executeUpdate(s);

			// prepare for prepare for finishlist from task element. Remove is 1.
//			System.out.println("D");
			s = "create table finishlist2 as select (teventtime-"+ starttime +") as eventtime, taskuid, tasksid from task where teventtype = 1 and teventtime > "+ starttime +" and source = '"+ cluster + "' order by teventtime";
			PdStatement2.executeUpdate(s);

			// append finishlist2 to finishlist and delete inishlist
//			System.out.println("E");
			s = "insert into finishlist select * from finishlist2";			
			PdStatement2.executeUpdate(s);

			PdStatement2.executeUpdate("drop table finishlist2");
			PdStatement.executeUpdate("drop table tt");

			// get the finish time of each task
//		    java.sql.Statement PdStatement4 = con.createStatement();

			// Translate database into Hashtable
			Hashtable tasklist = new Hashtable();
			java.sql.ResultSet rs10 = PdStatement.executeQuery("select * from tasklist");

			while(rs10.next())
			{
				long uid = rs10.getLong(2);
				String sid = rs10.getString(3);
//				task t = new task(uid, sid, rs10.getLong(1), rs10.getLong(4));
				task t = new task(rs10.getLong(1), rs10.getLong(4));
				tasklist.put(uid + "/"+sid,t);
			}

			// Translate database into Hashtable
			Hashtable finishlist = new Hashtable();
			java.sql.ResultSet rs20 = PdStatement.executeQuery("select * from finishlist");

			while(rs20.next())
			{
				long uid = rs20.getLong(2);
				String sid = rs20.getString(3);
//				task t = new task(uid, sid, rs20.getLong(1), -1);
				task t = new task(rs20.getLong(1), -1);
				finishlist.put(uid + "/"+sid,t);
			}


////////////
//			int z = 0;
			for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
				 String k = (String) e.nextElement();
			     task tt = (task) tasklist.get(k);
				 task tt2 = null;

//  				 java.sql.ResultSet rs1 = PdStatement4.executeQuery("select * from finishlist where taskuid = " + tt.uid + " and tasksid = '" + tt.sid +"'");
				 
				 if ((tt2 = (task) finishlist.get(k))!=null )
				 {
//					z++;
//					if (z%100==0)
//					{
//						System.out.print(".");
//					}  
					tt.finishtime = tt2.eventtime;
//					s = "update tasklist set finishtime = " + rs1.getLong(1) + " where taskuid = " + uid + " and tasksid = '" + sid +"'";
//					PdStatement3.executeUpdate(s);
			 	 }else {
//				 	System.out.println(k+ " does not exist !!!");
				 }		 
			}



////////////// start to generate time series
			java.sql.ResultSet rs1 = PdStatement.executeQuery("select max(eventtime) from tasklist");
				
			int sz = 0;
			long me = 0;

			if (rs1.next())
			{
				me = rs1.getInt(1);
			}else {
//				System.out.println("Hey Hari something wrong !!!");
			}

			sz = (int) me/unittime + 1;
			
			TimeSeries = new float[2][sz];

			// Cumulative average waiting time
			int c=1;
			long ct = 0;
//			System.out.println("me = " + me + ", " + me/unittime);
			
			do {
				ct = c*unittime;

//				float wt = 0;
				long l = 0;
				int n = 0;

				for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
		 	       task tt = (task) tasklist.get(e.nextElement());
					if (tt.eventtime <= ct)
					{
						n++;
						if (tt.finishtime > ct)
						{
							l = l + ct - tt.eventtime;
						} else {
							if (tt.finishtime == 0)
							{
								l = l + ct - tt.eventtime;		
							} else {
								l = l + tt.finishtime- tt.eventtime;
							}
						}
					}
				}

				TimeSeries[0][c-1] = ct;
				TimeSeries[1][c-1] = (float) l/n;
				c++;
//				System.out.print(".");
			} while(ct < me);
			
		} catch (SQLException E) {
			System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }

////// Generating time series for check waiting time to allocation.
	// June 4, 2002
	// New Version for speed improvement June 7, 2002
	public float [][] generateNumOfTaskVsWaitingTime(String cluster, int classno, int unittime){

//		long starttime = 0;
		float [][] TimeSeries = null;
/*
		try
		{

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and teventtime > " + start_time);
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
				System.out.println(start_time + ", " + starttime);
			} else {
				System.out.println("Something wrong in the task table ");
				return null;
			}

			// check whether there are same name of tables
			checkAndDelete("tasklist");
			checkAndDelete("tt");
			checkAndDelete("finishlist");

			// prepare task list
			System.out.println("A");
			String query = "CREATE TABLE TASKLIST ( EVENTTIME BIGINT NOT NULL, " +
												  " TASKUID BIGINT NOT NULL, " +
												  " TASKSID VARCHAR(20) NOT NULL, " +
												  " FINISHTIME BIGINT NOT NULL )";
			PdStatement.executeQuery( query ) ;

//			System.out.println("A-1");
			String s = "insert into tasklist (eventtime, taskuid, tasksid) select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' and verb <> 'Sample' and verb <> 'Finish' and verb <> 'Start' and verb <> 'Ready' order by teventtime";
//			String s = "create table tasklist as select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid, 12345678901234567 from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' order by teventtime";
			PdStatement2.executeUpdate(s);

			// prepare for finishlist from plan element
//			System.out.println("B");
			s = "create table tt as select (peventtime-"+ starttime +") as eventtime, taskuid, tasksid from processed_by where peventtype =0 and peventtime > "+ starttime +" and PROCESSED = 1 order by peventtime";
			PdStatement2.executeUpdate(s);

			// group by taskuid, tasksid.
//			System.out.println("C");
			s = "create table finishlist as select eventtime, taskuid, tasksid from tt group by taskuid, tasksid";
			PdStatement2.executeUpdate(s);

			// prepare for prepare for finishlist from task element. Remove is 1.
//			System.out.println("D");
			s = "create table finishlist2 as select (teventtime-"+ starttime +") as eventtime, taskuid, tasksid from task where teventtype = 1 and teventtime > "+ starttime +" and source = '"+ cluster + "' order by teventtime";
			PdStatement2.executeUpdate(s);

			// append finishlist2 to finishlist and delete inishlist
//			System.out.println("E");
			s = "insert into finishlist select * from finishlist2";			
			PdStatement2.executeUpdate(s);

			PdStatement2.executeUpdate("drop table finishlist2");
			PdStatement.executeUpdate("drop table tt");

			// get the finish time of each task
		    java.sql.Statement PdStatement4 = con.createStatement();

			// Translate database into Hashtable
			Hashtable tasklist = new Hashtable();
			java.sql.ResultSet rs10 = PdStatement.executeQuery("select * from tasklist");

			while(rs10.next())
			{
				long uid = rs10.getLong(2);
				String sid = rs10.getString(3);
//				task t = new task(uid, sid, rs10.getLong(1), rs10.getLong(4));
				task t = new task(rs10.getLong(1), rs10.getLong(4));
				tasklist.put(uid + "/"+sid,t);
			}

			// Translate database into Hashtable
			Hashtable finishlist = new Hashtable();
			java.sql.ResultSet rs20 = PdStatement.executeQuery("select * from finishlist");

			while(rs20.next())
			{
				long uid = rs20.getLong(2);
				String sid = rs20.getString(3);
//				task t = new task(uid, sid, rs20.getLong(1), -1);
				task t = new task(rs20.getLong(1), -1);
				finishlist.put(uid + "/"+sid,t);
			}


////////////
			int z = 0;
			for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
				 String k = (String) e.nextElement();
			     task tt = (task) tasklist.get(k);
				 task tt2 = null;

//  				 java.sql.ResultSet rs1 = PdStatement4.executeQuery("select * from finishlist where taskuid = " + tt.uid + " and tasksid = '" + tt.sid +"'");
				 
				 if ((tt2 = (task) finishlist.get(k))!=null )
				 {
//					z++;
//					if (z%100==0)
//					{
//						System.out.print(".");
//					}  
					tt.finishtime = tt2.eventtime;
//					s = "update tasklist set finishtime = " + rs1.getLong(1) + " where taskuid = " + uid + " and tasksid = '" + sid +"'";
//					PdStatement3.executeUpdate(s);
			 	 }else {
				 	System.out.println(k+ " does not exist !!!");
				 }		 
			}

			System.out.println("F");

////////////// Every regular number of tasks we can check the average waiting time.
////////////// The regular numer might be 100
////////////// Counting the number of tasks

			int number_of_tasks = tasklist.size();
			int sz = 0;
			int reg = 100;

			sz = (int) number_of_tasks / reg + 1; // the specified checkpoint number is 100.

			TimeSeries = new float[2][sz];

			// Cumulative average waiting time
			int c=1;
			int nn = 0; // next check point for the number of tasks
			long ct = 0;

			do {
				nn = c*reg;

				float wt = 0;
				long l = 0;
				int n = 0;

				
				for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
	 	    
				   task tt = (task) tasklist.get(e.nextElement());
				
					if (n <= nn)
					{
						n++;
						if (tt.finishtime > ct)
						{
							l = l + ct - tt.eventtime;
						} else {
							if (tt.finishtime == 0)
							{
								l = l + ct - tt.eventtime;		
							} else {
								l = l + tt.finishtime- tt.eventtime;
							}
						}
					} else {
						break;
					}
				}

				TimeSeries[0][c-1] = ct;
				TimeSeries[1][c-1] = (float) l/n;
				c++;
//				System.out.print(".");
			} while(ct < me);
			
		} catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
        }
*/
		return TimeSeries;
  }


////// Generating time series for check waiting time to allocation.
	// June 4, 2002
	// New Version for speed improvement June 7, 2002
	public float [][] generateLoadTimeSeries(String cluster, int classno, int unittime){

		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and teventtime > " + start_time);
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
//				System.out.println(start_time + ", " + starttime);
			} else {
//				System.out.println("Something wrong in the task table ");
				return null;
			}

			// check whether there are same name of tables
			checkAndDelete("tasklist");
			checkAndDelete("tt");
			checkAndDelete("finishlist");

			// prepare task list
//			System.out.println("A");
			String query = "CREATE TABLE TASKLIST ( EVENTTIME BIGINT NOT NULL, " +
												  " TASKUID BIGINT NOT NULL, " +
												  " TASKSID VARCHAR(20) NOT NULL, " +
												  " FINISHTIME BIGINT NOT NULL )";
			PdStatement.executeQuery( query ) ;

//			System.out.println("A-1");
			String s = "insert into tasklist (eventtime, taskuid, tasksid) select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' and verb <> 'Sample' and verb <> 'Finish' and verb <> 'Start' and verb <> 'Ready' order by teventtime";
//			String s = "create table tasklist as select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid, 12345678901234567 from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' order by teventtime";
			PdStatement2.executeUpdate(s);

			// prepare for finishlist from plan element
//			System.out.println("B");
			s = "create table tt as select (peventtime-"+ starttime +") as eventtime, taskuid, tasksid from processed_by where peventtype =0 and peventtime > "+ starttime +" and PROCESSED = 1 order by peventtime";
			PdStatement2.executeUpdate(s);

			// group by taskuid, tasksid.
//			System.out.println("C");
			s = "create table finishlist as select eventtime, taskuid, tasksid from tt group by taskuid, tasksid";
			PdStatement2.executeUpdate(s);

			// prepare for prepare for finishlist from task element. Remove is 1.
//			System.out.println("D");
			s = "create table finishlist2 as select (teventtime-"+ starttime +") as eventtime, taskuid, tasksid from task where teventtype = 1 and teventtime > "+ starttime +" and source = '"+ cluster + "' order by teventtime";
			PdStatement2.executeUpdate(s);

			// append finishlist2 to finishlist and delete inishlist
//			System.out.println("E");
			s = "insert into finishlist select * from finishlist2";			
			PdStatement2.executeUpdate(s);

			PdStatement2.executeUpdate("drop table finishlist2");
			PdStatement.executeUpdate("drop table tt");

			// get the finish time of each task
//		    java.sql.Statement PdStatement4 = con.createStatement();

			// Translate database into Hashtable
			Hashtable tasklist = new Hashtable();
			java.sql.ResultSet rs10 = PdStatement.executeQuery("select * from tasklist");

			while(rs10.next())
			{
				long uid = rs10.getLong(2);
				String sid = rs10.getString(3);
//				task t = new task(uid, sid, rs10.getLong(1), rs10.getLong(4));
				task t = new task(rs10.getLong(1), rs10.getLong(4));
				tasklist.put(uid + "/"+sid,t);
			}

			// Translate database into Hashtable
			Hashtable finishlist = new Hashtable();
			java.sql.ResultSet rs20 = PdStatement.executeQuery("select * from finishlist");

			while(rs20.next())
			{
				long uid = rs20.getLong(2);
				String sid = rs20.getString(3);
//				task t = new task(uid, sid, rs20.getLong(1), -1);
				task t = new task(rs20.getLong(1), -1);
				finishlist.put(uid + "/"+sid,t);
			}


////////////
//			int z = 0;
			for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
				 String k = (String) e.nextElement();
			     task tt = (task) tasklist.get(k);
				 task tt2 = null;

//  				 java.sql.ResultSet rs1 = PdStatement4.executeQuery("select * from finishlist where taskuid = " + tt.uid + " and tasksid = '" + tt.sid +"'");
				 
				 if ((tt2 = (task) finishlist.get(k))!=null )
				 {
//					z++;
//					if (z%100==0)
//					{
//						System.out.print(".");
//					}  
					tt.finishtime = tt2.eventtime;
//					s = "update tasklist set finishtime = " + rs1.getLong(1) + " where taskuid = " + uid + " and tasksid = '" + sid +"'";
//					PdStatement3.executeUpdate(s);
			 	 }else {
//				 	System.out.println(k+ " does not exist !!!");
				 }		 
			}

//			System.out.println("F");

////////////// start to generate time series
			java.sql.ResultSet rs1 = PdStatement.executeQuery("select max(eventtime) from tasklist");
				
			int sz = 0;
			long me = 0;

			if (rs1.next())
			{
				me = rs1.getInt(1);
			}else {
//				System.out.println("Hey Hari something wrong !!!");
			}

			sz = (int) me/unittime + 1;
			
			TimeSeries = new float[2][sz];

			// Cumulative average waiting time
			int c=1;
			long ct = 0;
//			System.out.println("me = " + me + ", " + me/unittime);
			
			do {
				ct = c*unittime;

//				float wt = 0;
//				long l = 0;
				int n = 0;

				for (Enumeration e = tasklist.keys() ; e.hasMoreElements() ;) {
		 	       task tt = (task) tasklist.get(e.nextElement());
					if (tt.eventtime <= ct)
					{

						if (tt.finishtime > ct)
						{
							n++;
						} else {
							if (tt.finishtime == 0)
							{
								n++;
							} 
						}
					}
				}

				TimeSeries[0][c-1] = ct;
				TimeSeries[1][c-1] = (float) n;
				c++;
			} while(ct < me);
			
		} catch (SQLException E) {
            System.out.println("SQLException: " + E.getMessage());
//          System.out.println("SQLState:     " + E.getSQLState());
//          System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }




////// Generating time series for check waiting time to allocation.
	// June 4, 2002
	// Old version June 7, 2002
/*
	public float [][] generateTSofWaitingTime(String cluster, int classno, int unittime){

		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and teventtime > " + start_time);
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
				System.out.println(start_time + ", " + starttime);
			} else {
				System.out.println("Something wrong in the task table ");
				return null;
			}

			// check whether there are same name of tables
			checkAndDelete("tasklist");
			checkAndDelete("tt");
			checkAndDelete("finishlist");

			// prepare task list
			System.out.println("A");
			String query = "CREATE TABLE TASKLIST ( EVENTTIME BIGINT NOT NULL, " +
												  " TASKUID BIGINT NOT NULL, " +
												  " TASKSID VARCHAR(20) NOT NULL, " +
												  " FINISHTIME BIGINT NOT NULL )";
			PdStatement.executeQuery( query ) ;

			System.out.println("A-1");
			String s = "insert into tasklist (eventtime, taskuid, tasksid) select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' and verb <> 'Sample' and verb <> 'Finish' and verb <> 'Start' and verb <> 'Ready' order by teventtime";
//			String s = "create table tasklist as select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid, 12345678901234567 from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' order by teventtime";
			PdStatement2.executeUpdate(s);

			// prepare for finishlist from plan element
			System.out.println("B");
			s = "create table tt as select (peventtime-"+ starttime +") as eventtime, taskuid, tasksid from processed_by where peventtype =0 and peventtime > "+ starttime +" and PROCESSED = 1 order by peventtime";
			PdStatement2.executeUpdate(s);

			// group by taskuid, tasksid.
			System.out.println("C");
			s = "create table finishlist as select eventtime, taskuid, tasksid from tt group by taskuid, tasksid";
			PdStatement2.executeUpdate(s);

			// prepare for prepare for finishlist from task element. Remove is 1.
			System.out.println("D");
			s = "create table finishlist2 as select (teventtime-"+ starttime +") as eventtime, taskuid, tasksid from task where teventtype = 1 and teventtime > "+ starttime +" and source = '"+ cluster + "' order by teventtime";
			PdStatement2.executeUpdate(s);

			// append finishlist2 to finishlist and delete inishlist
			System.out.println("E");
			s = "insert into finishlist select * from finishlist2";			
			PdStatement2.executeUpdate(s);

			PdStatement2.executeUpdate("drop table finishlist2");
			PdStatement.executeUpdate("drop table tt");

			// get the finish time of each task
		    java.sql.Statement PdStatement4 = con.createStatement();

			java.sql.ResultSet rs10 = PdStatement.executeQuery("select * from tasklist");

			while(rs10.next())
			{
				long uid = rs10.getLong(2);
				String sid = rs10.getString(3);

				java.sql.ResultSet rs1 = PdStatement4.executeQuery("select * from finishlist where taskuid = " + uid + " and tasksid = '" + sid +"'");
				
				if (rs1.next())
				{

					System.out.print(".");
					s = "update tasklist set finishtime = " + rs1.getLong(1) + " where taskuid = " + uid + " and tasksid = '" + sid +"'";
					PdStatement3.executeUpdate(s);
				}else {
					System.out.println(uid + "/"+sid + " does not exist !!!");
				}
			} 

			System.out.println("F");
////////////// start to generate time series
			java.sql.ResultSet rs1 = PdStatement.executeQuery("select max(eventtime) from tasklist");
				
			int sz = 0;
			long me = 0;

			if (rs1.next())
			{
				me = rs1.getInt(1);
			}else {
				System.out.println("Hey Hari something wrong !!!");
			}

			sz = (int) me/unittime + 1;
			
			TimeSeries = new float[2][sz];

			// Cumulative average waiting time
			int c=1;
			long ct = 0;
			System.out.println("me = " + me + ", " + me/unittime);
			do {
				ct = c*unittime;

				s = "select * from tasklist where eventtime <= " + ct;
				java.sql.ResultSet rs3 = PdStatement.executeQuery(s);

				float wt = 0;
				long l = 0;
				int n = 0;
				while (rs3.next())
				{
					n++;
					if (rs3.getLong(4) > ct)
					{
						l = l + ct - rs3.getLong(1);
					} else {
						if (rs3.getLong(4) == 0)
						{
							l = l + ct - rs3.getLong(1);		
						} else {
							l = l + rs3.getLong(4)- rs3.getLong(1);
						}
					}
				}
				TimeSeries[0][c-1] = ct;
				TimeSeries[1][c-1] = (float) l/n;
				c++;
				System.out.print(".");
			} while(ct < me);
			
		} catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }
*/

////// Generating time series for check waiting time to allocation.
	// June 4, 2002
	// New trial but it is not finished June 7, 2002
/*
	public float [][] generateTSofWaitingTimeByEvent(String cluster, int classno, int unittime){

		long starttime = 0;
		float [][] TimeSeries = null;

		try
		{

			// get the time of first task
			java.sql.ResultSet rs0 = PdStatement.executeQuery("select min(teventtime) from task where source = '" + cluster + "' and teventtype = 0 and teventtime > " + start_time);
			
			if (rs0.next())
			{
				starttime = rs0.getLong(1);
				System.out.println(start_time + ", " + starttime);
			} else {
				System.out.println("Something wrong in the task table ");
				return null;
			}

			// check whether there are same name of tables
			checkAndDelete("tasklist");
			checkAndDelete("tt");
			checkAndDelete("finishlist");

			// prepare task list
			System.out.println("A");
			String query = "CREATE TABLE TASKLIST ( EVENTTIME BIGINT NOT NULL, " +
												  " TASKUID BIGINT NOT NULL, " +
												  " TASKSID VARCHAR(20) NOT NULL, " +
												  " FINISHTIME BIGINT NOT NULL )";
			PdStatement.executeQuery( query ) ;

			System.out.println("A-1");
			String s = "insert into tasklist (eventtime, taskuid, tasksid) select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' and verb <> 'Sample' and verb <> 'Finish' and verb <> 'Start' and verb <> 'Ready' order by teventtime";
//			String s = "create table tasklist as select (teventtime - " + starttime + ") as eventtime, taskuid, tasksid, 12345678901234567 from task where teventtype=0 and teventtime > "+ starttime +" and source = '"+ cluster +"' order by teventtime";
			PdStatement2.executeUpdate(s);

			// prepare for finishlist from plan element
			System.out.println("B");
			s = "create table tt as select (peventtime-"+ starttime +") as eventtime, taskuid, tasksid from processed_by where peventtype =0 and peventtime > "+ starttime +" and PROCESSED = 1 order by peventtime";
			PdStatement2.executeUpdate(s);

			// group by taskuid, tasksid.
			System.out.println("C");
			s = "create table finishlist as select eventtime, taskuid, tasksid from tt group by taskuid, tasksid";
			PdStatement2.executeUpdate(s);

			// prepare for prepare for finishlist from task element. Remove is 1.
			System.out.println("D");
			s = "create table finishlist2 as select (teventtime-"+ starttime +") as eventtime, taskuid, tasksid from task where teventtype = 1 and teventtime > "+ starttime +" and source = '"+ cluster + "' order by teventtime";
			PdStatement2.executeUpdate(s);

			// append finishlist2 to finishlist and delete inishlist
			System.out.println("E");
			s = "insert into finishlist select * from finishlist2";			
			PdStatement2.executeUpdate(s);

			PdStatement2.executeUpdate("drop table finishlist2");
			PdStatement.executeUpdate("drop table tt");

			// get the finish time of each task
		    java.sql.Statement PdStatement4 = con.createStatement();

			java.sql.ResultSet rs10 = PdStatement.executeQuery("select * from tasklist");
			while(rs10.next())
			{
				long uid = rs10.getLong(2);
				String sid = rs10.getString(3);

				java.sql.ResultSet rs1 = PdStatement4.executeQuery("select * from finishlist where taskuid = " + uid + " and tasksid = '" + sid +"'");
				
				if (rs1.next())
				{

					System.out.print(".");
					s = "update tasklist set finishtime = " + rs1.getLong(1) + " where taskuid = " + uid + " and tasksid = '" + sid +"'";
					PdStatement3.executeUpdate(s);
				}else {
					System.out.println(uid + "/"+sid + " does not exist !!!");
				}
			} 

			System.out.println("F");
////////////// start to generate time series
			java.sql.ResultSet rs1 = PdStatement.executeQuery("select max(eventtime) from tasklist");
				
			int sz = 0;
			long me = 0;

			if (rs1.next())
			{
				me = rs1.getInt(1);
			}else {
				System.out.println("Hey Hari something wrong !!!");
			}

			sz = (int) me/unittime + 1;
			
			TimeSeries = new float[2][sz];
			
			boolean con = true;
	
			int c=1;
			long ct = 0;
			System.out.println("me = " + me + ", " + me/unittime);
			do {
				ct = c*unittime;

				s = "select * from tasklist where eventtime <= " + ct;
				java.sql.ResultSet rs3 = PdStatement.executeQuery(s);

				long l = 0;
				int n = 0;
				while (rs3.next())
				{
					n++;
					if (rs3.getLong(4) > ct)
					{
						l = l + ct - rs3.getLong(1);
					} else {
						if (rs3.getLong(4) == 0)
						{
							l = l + ct - rs3.getLong(1);		
						} else {
							l = l + rs3.getLong(4)- rs3.getLong(1);
						}
					}
				}
				TimeSeries[0][c-1] = ct;
				TimeSeries[1][c-1] = (float) l/n;
				c++;
				System.out.print(",");
			} while(ct < me);
			
		} catch (SQLException E) {
          System.out.println("SQLException: " + E.getMessage());
          System.out.println("SQLState:     " + E.getSQLState());
          System.out.println("VendorError:  " + E.getErrorCode());
        }

		return TimeSeries;
  }
*/
  public void establishConnection(){

    // Open DB
    try {
      Class.forName("org.gjt.mm.mysql.Driver");

    } catch(java.lang.ClassNotFoundException e) {
       System.err.print("(EstablishConnetction) ClassNotFoundException:");
//      System.err.println(e.getMessage());
    }

//    String dbPath = ServerApp.instance().getDbPath() ;
	
	String dbPath = "jdbc:mysql://localhost/?user=ultralog";

    if ( dbPath == null ) {
//        System.out.println( "dbPath variable not set. Could not establish connection to database." );
        return ;
    }

//    System.out.println( "PlanDatabase:: Connecting to dababase " + dbPath ) ;
    try {
      con = DriverManager.getConnection(dbPath);

      PdStatement = con.createStatement();
      PdStatement2 = con.createStatement();
      PdStatement3 = con.createStatement();
     
    } catch(SQLException ex) {

//      System.err.println("(EstablishConnetction) -----SQLException-----");
        System.err.println("SQLState:  " + ex.getSQLState());
//      System.err.println("Message:  " + ex.getMessage());
//      System.err.println("Vendor:  " + ex.getErrorCode());
    } 

  }

  java.io.BufferedWriter trOut, tsOut;

}