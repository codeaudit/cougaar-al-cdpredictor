package org.cougaar.tools.database;

import org.cougaar.cpe.agents.plugin.control.*;
import org.cougaar.tools.techspecs.qos.*;
import org.cougaar.cpe.agents.messages.ControlMessage;
import org.cougaar.core.mts.MessageAddress;

import java.util.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataCollector {
	public DataCollector() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		java.util.Date date = new java.util.Date();
		expDate = fmt.format(date);
		version = "0.1"; //control version
		comment = "Initial";
		lotID = "1";
		runID = System.currentTimeMillis();
	}

	public String generateSQLString() {
		String sql = "Insert into CPE values('" + expDate + "','" + version + "','" + comment + "','" + lotID;
		sql += "'," + runID + "," + BN1depth + "," + BN1breadth + "," + BN1replanTimer + "," + BN2depth + "," + BN2breadth + "," + BN2replanTimer + "," + BN3depth + "," + BN3breadth + "," + BN3replanTimer + "," + CPYupdatetimeZ1 + "," + CPYupdatetimeZ2 + "," + CPYupdatetimeZ3;
		sql += "," + Score_Actual[0] + "," + Score_Actual[1] + "," + Score_Actual[2] + "," + MAU[0] + "," + MAU[1] + "," + MAU[2] + "," + MAU[3] + "," + MAU[4] + "," + BN1EntryRateAvg + "," + BN2EntryRateAvg + "," + BN3EntryRateAvg;
		sql += "," + MG1_MPF[0] + "," + MG1_MPF[1] + "," + MG1_MPF[2] + "," + Whitt_MPF[0] + "," + Whitt_MPF[1] + "," + Whitt_MPF[2] + "," + Arena_MPF[0] + "," + Arena_MPF[1] + "," + Arena_MPF[2] + "," + Score_Estimated[0] + "," + Score_Estimated[1] + "," + Score_Estimated[2];
		sql += "," + cntlBN1depth + "," + cntlBN1breadth + "," + cntlBN1replanTimer + "," + cntlBN2depth + "," + cntlBN2breadth + "," + cntlBN2replanTimer + "," + cntlBN3depth + "," + cntlBN3breadth + "," + cntlBN3replanTimer + "," + cntlCPYupdatetimeZ1 + "," + cntlCPYupdatetimeZ2 + "," + cntlCPYupdatetimeZ3 + ")";
		System.out.println(sql);
		return sql;
	}

	public void insertDB() {
		try {
			/* Test loading driver */
			String driver = "com.mysql.jdbc.Driver";
			//System.out.println("\n=> loading driver:");
			Class.forName(driver).newInstance();
			//System.out.println("OK");

			/* Test the connection */
			String url = "jdbc:mysql://lb234f.ie.psu.edu/darpa";
			//System.out.println("\n=> connecting:");
			Connection con = DriverManager.getConnection(url, "darpa-chaos", "darpa-chaos(2004)");
			//System.out.println("OK");

			Statement stmt = con.createStatement();

			stmt.execute(generateSQLString());

			con.close();

		} catch (Exception x) {
			//System.err.println(x);
		}
	}

	public void setValues(HashMap scores, ControlMeasurement c, ControlMessage cm, double[] MAU, double[] mg1, double[] whitt, double[] arena, double[] score) {
		try {
			BN1depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN1"), "PlanningDepth")).intValue();
			BN1breadth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN1"), "PlanningBreadth")).intValue();
			BN1replanTimer = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN1"), "ReplanPeriod")).intValue();
			CPYupdatetimeZ1 = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("CPY1"), "WorldStateUpdatePeriod")).intValue();
		} catch (Exception e) {

		}
		try {
			BN2depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN2"), "PlanningDepth")).intValue();
			BN2breadth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN2"), "PlanningBreadth")).intValue();
			BN2replanTimer = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN2"), "ReplanPeriod")).intValue();
			CPYupdatetimeZ2 = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("CPY4"), "WorldStateUpdatePeriod")).intValue();
		} catch (Exception e) {
		}

		try {
			BN3depth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN3"), "PlanningDepth")).intValue();
			BN3breadth = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN3"), "PlanningBreadth")).intValue();
			BN3replanTimer = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("BN3"), "ReplanPeriod")).intValue();
			CPYupdatetimeZ3 = ((Integer) c.getOpModeValue(MessageAddress.getMessageAddress("CPY7"), "WorldStateUpdatePeriod")).intValue();
		} catch (Exception e) {
		}

		//theoretically the queueingParmeters could be from different QueueingParameters
		//we dont have ranked set now
		try {
			MG1_MPF = mg1;
			Whitt_MPF = whitt;
			Arena_MPF = arena;
			Score_Estimated = score;
			this.MAU = MAU;
		} catch (Exception e) {
		}

		if (cm != null) {
			try {
				cntlBN1depth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN1"), "PlanningDepth")).intValue();
				cntlBN1breadth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN1"), "PlanningBreadth")).intValue();
				cntlBN1replanTimer = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN1"), "ReplanPeriod")).intValue();
				cntlCPYupdatetimeZ1 = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("CPY1"), "WorldStateUpdatePeriod")).intValue();
			} catch (Exception e) {
				//System.out.println("DC ControlMessage zone 1");
				//e.printStackTrace();
				cntlBN1depth = 0;
				cntlBN1breadth = 0;
				cntlBN1replanTimer = 0;
				cntlCPYupdatetimeZ1 = 0;
			}
			try {
				cntlBN2depth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN2"), "PlanningDepth")).intValue();
				cntlBN2breadth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN2"), "PlanningBreadth")).intValue();
				cntlBN2replanTimer = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN2"), "ReplanPeriod")).intValue();
				cntlCPYupdatetimeZ2 = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("CPY4"), "WorldStateUpdatePeriod")).intValue();
			} catch (Exception e) {
				//System.out.println("DC ControlMessage zone 2");
				//e.printStackTrace();
				cntlBN2depth = 0;
				cntlBN2breadth = 0;
				cntlBN2replanTimer = 0;
				cntlCPYupdatetimeZ2 = 0;

			}

			try {
				cntlBN3depth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN3"), "PlanningDepth")).intValue();
				cntlBN3breadth = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN3"), "PlanningBreadth")).intValue();
				cntlBN3replanTimer = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("BN3"), "ReplanPeriod")).intValue();
				cntlCPYupdatetimeZ3 = ((Integer) cm.getControlParameter(MessageAddress.getMessageAddress("CPY7"), "WorldStateUpdatePeriod")).intValue();
			} catch (Exception e) {
				//System.out.println("DC ControlMessage zone 3");
				//e.printStackTrace();
				cntlBN3depth = 0;
				cntlBN3breadth = 0;
				cntlBN3replanTimer = 0;
				cntlCPYupdatetimeZ3 = 0;
			}
		} else {
			cntlBN1depth = 0;
			cntlBN1breadth = 0;
			cntlBN1replanTimer = 0;
			cntlBN2depth = 0;
			cntlBN2breadth = 0;
			cntlBN2replanTimer = 0;
			cntlBN3depth = 0;
			cntlBN3breadth = 0;
			cntlBN3replanTimer = 0;
			cntlCPYupdatetimeZ1 = 0;
			cntlCPYupdatetimeZ2 = 0;
			cntlCPYupdatetimeZ3 = 0;
		}

		try {
			String[] BN1_keys = { "BN1.Kills", "BN1.Attrition", "BN1.Violations", "BN1.Penalties", "BN1.FuelConsumption.CPY1", "BN1.FuelConsumption.CPY2", "BN1.FuelConsumption.CPY3" };
			String[] BN2_keys = { "BN2.Kills", "BN2.Attrition", "BN2.Violations", "BN2.Penalties", "BN2.FuelConsumption.CPY4", "BN2.FuelConsumption.CPY5", "BN2.FuelConsumption.CPY6" };
			String[] BN3_keys = { "BN3.Kills", "BN3.Attrition", "BN3.Violations", "BN3.Penalties", "BN3.FuelConsumption.CPY7", "BN3.FuelConsumption.CPY8", "BN3.FuelConsumption.CPY9" };
			String[] entryRateAvg_keys = { "BN1.EntryRate", "BN2.EntryRate", "BN3.EntryRate" };

			double g1 = 0;
			for (int i = 0; i < 5; i++) {
				g1 += (((Double) scores.get(BN1_keys[i])).doubleValue()) * MAU[i];
			}
			g1 += ((((Double) scores.get(BN1_keys[5])).doubleValue()) * MAU[4] + (((Double) scores.get(BN1_keys[5])).doubleValue()) * MAU[4]);

			double g2 = 0;
			for (int i = 0; i < 5; i++) {
				g2 += (((Double) scores.get(BN2_keys[i])).doubleValue()) * MAU[i];
			}
			g2 += ((((Double) scores.get(BN2_keys[5])).doubleValue()) * MAU[4] + (((Double) scores.get(BN2_keys[5])).doubleValue()) * MAU[4]);

			double g3 = 0;
			for (int i = 0; i < 5; i++) {
				g3 += (((Double) scores.get(BN3_keys[i])).doubleValue()) * MAU[i];
			}
			g3 += ((((Double) scores.get(BN3_keys[5])).doubleValue()) * MAU[4] + (((Double) scores.get(BN3_keys[5])).doubleValue()) * MAU[4]);

			Score_Actual[0] = g1;
			Score_Actual[1] = g2;
			Score_Actual[2] = g3;

			BN1EntryRateAvg = (((Double) scores.get(entryRateAvg_keys[0])).doubleValue());
			BN2EntryRateAvg = (((Double) scores.get(entryRateAvg_keys[1])).doubleValue());
			BN3EntryRateAvg = (((Double) scores.get(entryRateAvg_keys[2])).doubleValue());
		} catch (Exception e) {
		}

		//INSERT
		insertDB();

	}

	String expDate;
	String version = "", comment = "", lotID = "";
	long runID;
	//CPE data : current config
	int BN1depth, BN1breadth, BN1replanTimer;
	int BN2depth, BN2breadth, BN2replanTimer;
	int BN3depth, BN3breadth, BN3replanTimer;
	int CPYupdatetimeZ1, CPYupdatetimeZ2, CPYupdatetimeZ3;

	double[] Score_Actual = new double[3]; //
	double[] MAU = new double[5];
	double BN1EntryRateAvg, BN2EntryRateAvg, BN3EntryRateAvg; //

	double[] MG1_MPF = new double[3];
	double[] Whitt_MPF = new double[3];
	double[] Arena_MPF = new double[3];
	double[] Score_Estimated = new double[3];

	int cntlBN1depth = 0, cntlBN1breadth = 0, cntlBN1replanTimer = 0;
	int cntlBN2depth = 0, cntlBN2breadth = 0, cntlBN2replanTimer = 0;
	int cntlBN3depth = 0, cntlBN3breadth = 0, cntlBN3replanTimer = 0;
	int cntlCPYupdatetimeZ1 = 0, cntlCPYupdatetimeZ2 = 0, cntlCPYupdatetimeZ3 = 0;

}
