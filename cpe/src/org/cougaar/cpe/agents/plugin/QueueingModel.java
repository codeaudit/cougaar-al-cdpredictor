package org.cougaar.cpe.agents.plugin;

/**
 * @author Nathan Gnanasambandam
 * This plugin will get the measurements (arrival and service time measurments from the 
 * concerned plugins and using the queueing models, compute MPF of the system.
 * This along with other stats will eventually be used for control 
 */
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.service.LoggingService;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.tools.techspecs.qos.*;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.cpe.relay.ControlSourceBufferRelay;
import org.cougaar.cpe.relay.ControlTargetBufferRelay;
import org.cougaar.core.service.UIDService;
import org.cougaar.core.relay.Relay;
import org.cougaar.core.agent.service.alarm.Alarm;
import com.axiom.lib.mat.*;
import com.axiom.lib.util.*;
import java.io.Serializable;
import org.cougaar.cpe.agents.messages.ControlMessage;
import org.cougaar.cpe.agents.messages.OpmodeNotificationMessage;
import org.cougaar.core.adaptivity.OperatingModeCondition;
import org.cougaar.cpe.agents.plugin.control.*;
import org.cougaar.cpe.ui.ControlDisplayPanel;
import org.cougaar.tools.database.*;

import java.io.File;
//import java.io.FileInputStream;
import java.util.*;
import java.util.Comparator;
import java.io.*;

public class QueueingModel extends ComponentPlugin {
	private LoggingService log;

	public void processMessage(Object[] o) {
		// all types of message received are known
		// so the object array Object[] o is traversed and the latest this is set to the local copy
		if (o.length > 0) {
			onm = null;
			for (int i = 0; i < o.length; i++) {
				if (o[i] instanceof OpmodeNotificationMessage) {
					onm = (OpmodeNotificationMessage) o[i];
				} else {
				}
			}
		}
	}

	//get subscriptions of the measurements points - from BDEAgentPlugin, C2AgentPlugin, UnitAgentPlugin
	protected void setupSubscriptions() {
		cdp = new ControlDisplayPanel(getAgentIdentifier().getAddress(), this);
		cdp.setSize(800, 600);
		cdp.setVisible(true);

		logger = (LoggingService) getServiceBroker().getService(this, LoggingService.class, null);

		baseTime = System.currentTimeMillis();

		measurementPointSubscription = (IncrementalSubscription) getBlackboardService().subscribe(new UnaryPredicate() {
			public boolean execute(Object o) {
				return o instanceof MeasurementPoint;
			}
		});

		setupRelaySubscriptions();

		opModeCondSubscription = (IncrementalSubscription) getBlackboardService().subscribe(new UnaryPredicate() {
			public boolean execute(Object o) {
				return o instanceof OperatingModeCondition;
			}
		});

		makeMeasurementPoints();
	}

	/**
	 * Sets up control source buffer relays to every subordinate.
	 * The list of subordinates is currently hard-coded.
	 *
	 */
	private void setupRelaySubscriptions() {

		csbr = new ControlSourceBufferRelay[SubordinateList.length];
		UIDService service = (UIDService) getServiceBroker().getService(this, UIDService.class, null);
		for (int i = 0; i < SubordinateList.length; i++) {
			csbr[i] = new ControlSourceBufferRelay(service.nextUID(), MessageAddress.getMessageAddress(SubordinateList[i]), getAgentIdentifier());
			getBlackboardService().publishAdd(csbr[i]);
		}
	}

	/**
	 * averaging every score attribute for the last five minutes
	 * @param currentTime
	 * @param period
	 * @return
	 */
	public HashMap getMeanScore(long currentTime, int period) {
		HashMap scores = new HashMap();
		Iterator iter = null;
		if (period == 0)
			return null;
		try {
			for (int i = 0; i < measurementPoints.size(); i++) {
				MeasurementPoint measurementPoint = (MeasurementPoint) measurementPoints.get(i);
				//System.out.println("Measurement points name " + measurementPoint.getName()+","+measurementPoint.getHistorySize());
				if (measurementPoint instanceof TimePeriodMeasurementPoint) {
					TimePeriodMeasurementPoint dmp = (TimePeriodMeasurementPoint) measurementPoint;
					//System.out.println("Measurement points name " + dmp.getName()+","+dmp.getHistorySize());
					iter = dmp.getMeasurements(period);
					double total = 0;
					double count = 0;
					while ((iter != null) && (iter.hasNext())) {
						TimePeriodMeasurement t = (TimePeriodMeasurement) iter.next();
						Object o = t.getValue();
						if (o instanceof Double)
							total += ((Double) (o)).doubleValue();
						else if (o instanceof Integer)
							total += ((Integer) (o)).intValue();
						count++;
					}
					if (count != 0) {
						//System.out.println(dmp.getName()+":  " +total+"/"+(count*5/60));
						scores.put(dmp.getName(), new Double(total / (count * 5 / 60)));
					}
				}
			}
			//System.out.println("AVG SCORES " + scores);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return scores;
	}

	/**
	 * method for calculating average delay
	 * @param currentTime
	 * @param period
	 * @return moving average for the last n points specified by period
	 */
	public double[][] getMeanDelay(long currentTime, int period) {
		double[][] avg_delays = new double[7][2];
		double[][] total_delay = new double[7][3];

		Iterator iter = null;
		int count = 0;

		if (period == 0)
			return null;

		try {
			//System.out.println(
			//"Measurement points size " + measurementPoints.size());
			for (int i = 0; i < measurementPoints.size(); i++) {
				MeasurementPoint measurementPoint = (MeasurementPoint) measurementPoints.get(i);
				//System.out.println(
				//"Measurement points name " + measurementPoint.getName());
				long[] delays = new long[period];
				if (measurementPoint instanceof EventDurationMeasurementPoint) {
					EventDurationMeasurementPoint dmp = (EventDurationMeasurementPoint) measurementPoint;
					iter = dmp.getMeasurements(period);
				}
				int typeOfDelayMeasurement = -1;
				//System.out.println("iter " + iter.hasNext());
				if ((iter != null) && (iter.hasNext())) {
					//first column is total delay, second column is number of delay points
					count = 0;
					while (iter.hasNext()) {
						DelayMeasurement delayMeasurement = (DelayMeasurement) iter.next();
						long delay = 0;

						//	System.out.println(
						//	"DelayMeasurment " + delayMeasurement);
						if (delayMeasurement.getAction() != null) {
							if ((delayMeasurement.getAction().equals("ZonePlan")) && (total_delay[0][1] < period)) {
								delay = delayMeasurement.getLocalTime() - delayMeasurement.getTimestamp();
								total_delay[0][0] += delay;
								total_delay[0][1]++;
								typeOfDelayMeasurement = 0;
								//System.out.println(
								//	"DELAY   ZonePlan: " + delay);
							} else if ((delayMeasurement.getAction().equals("ProcessUpdateBDE")) && (total_delay[1][1] < period)) {
								delay = delayMeasurement.getLocalTime() - delayMeasurement.getTimestamp();
								total_delay[1][0] += delay;
								total_delay[1][1]++;
								//System.out.println(
								//	"DELAY   ProcessUpdateBDE: " + delay);
								typeOfDelayMeasurement = 1;
							}
							//if (delay > 0) {
							delays[count] = delay;
							count++;
							//}
						}
					}

					for (int ii = 0; ii < 7; ii++) {
						if (total_delay[ii][1] > 0) {
							avg_delays[ii][0] = (double) (total_delay[ii][0] / total_delay[ii][1]);
						} else {
							avg_delays[ii][0] = -1;
						}
					}
					//broken code: doesnt work as it should
					if (count > 0) {
						double sum = 0;
						for (int p = 0; p < count; p++) {
							double diffSq = (double) Math.pow(delays[p] - avg_delays[typeOfDelayMeasurement][0], 2);
							//System.out.println("diffSq: " + diffSq);					
							sum += diffSq;
							//System.out.println("sum: " + sum);
						}
						double variance = (double) (sum / count);
						//System.out.println("count: " + count);
						avg_delays[typeOfDelayMeasurement][1] = variance;
					}
				}
			}
			return avg_delays;
		} catch (Exception e) {
			//throw new RuntimeException("Unexpected condition reached");
			e.printStackTrace();
			return null;
		}
	}

	//Here the measurements will be used in the queueing models
	public void execute() {
		//logger.shout("\n *------------------Execute Called---------------* ");
		if (!started) {
			//setting the base times
			lastTime = System.currentTimeMillis();
			startTime = lastTime;

			started = true;
			measurement();
			readMAU();
		}

		if (!measurementPointSubscription.getAddedCollection().isEmpty() || !measurementPointSubscription.getRemovedCollection().isEmpty()) {
			measurementPoints.clear();
			measurementPoints.addAll(measurementPointSubscription.getCollection());
			Collections.sort(measurementPoints, new Comparator() {
				public int compare(Object o1, Object o2) {
					MeasurementPoint w1 = (MeasurementPoint) o1, w2 = (MeasurementPoint) o2;
					return w1.getName().compareTo(w2.getName());
				}
			});
			cdp.updateMeasurements(measurementPoints);
		}

	}

	public void measurement() {

		try {
			//last (SCORE * 5) seconds of history
			HashMap scores = getMeanScore(System.currentTimeMillis(), SCORE);

			measurementPoints.clear();
			measurementPoints.addAll(measurementPointSubscription.getCollection());


			logger.shout("\n *MEASUREMENT DATA* @" + this.getAgentIdentifier());
			ControlMeasurement c = consolidateDelays(false);

			//store the system snapshot in a measurement point
			boolean wasOpen = true;
			if (!getBlackboardService().isTransactionOpen()) {
				wasOpen = false;
				getBlackboardService().openTransaction();
			}
			controlMP.addMeasurement(c);
			if (getBlackboardService().isTransactionOpen() && !wasOpen) {
				getBlackboardService().closeTransaction();
			}

			//if system has been purturbed control, predicted entry rate is different from last time or stress is present
			//set timerCount as a multiple of nextTime for Control Measurement
			if ((timerCount++ == CONTROL) && (hasSystemBeenPurturbed())) {
				timerCount = 0;

				//check if c is ok, if not get a good candidate
				ControlMeasurement candidate_CM = null;
				long ts = 0;
				if (ifControlMeasurementOk(c) == false) {
					Iterator itr = controlMP.getMeasurements(4);
					while (itr.hasNext()) {
						ControlMeasurement temp_CM = (ControlMeasurement) itr.next();
						if ((ifControlMeasurementOk(temp_CM) == true) && (temp_CM.getTimeStamp() > ts)) {
							candidate_CM = temp_CM;
							ts = temp_CM.getTimeStamp();
						}
					}
					c = candidate_CM; //c becomes the new c
				} else {
					candidate_CM = c; //c is ok 
				}

				if (candidate_CM != null) {

					SearchAndRank s = new SearchAndRank(c, scores);
					ArrayList estimated_qp = s.generateQueueingParameters(c, scores);
					//ships the ith ranking control set from the class s
					// this control set is a hashmap of hashmaps
					ControlMessage cm = s.getTop(1);
					//System.out.println("Control String "+cm.toString());

					if (cm != null)
						shipControlSet(cm);

					dc.setValues(scores, c, cm, MAU, s.getMG1(), s.getWhitt(), s.getArena(), s.getScore());

					//GUI stuff	---------------------------
					if (estimated_qp.size() > 0)
						cdp.updateQueueingParameters(estimated_qp);
					//cdp.updateQueueingParameters(estimated_qp.subList(0, 10));

					if (scores.size() > 0)
						cdp.updateControls(scores);

					wasOpen = true;
					if (!getBlackboardService().isTransactionOpen()) {
						wasOpen = false;
						getBlackboardService().openTransaction();
					}
					if (scores != null) {
						long currTime = System.currentTimeMillis();
						avgScoresMP.addMeasurement(new TimePeriodMeasurement(lastTime, currTime, new Double(getScore(scores))));
						lastTime = currTime;
					}
					if (getBlackboardService().isTransactionOpen() && !wasOpen) {
						getBlackboardService().closeTransaction();
					}

					//end GUI Stuff----------------------------				
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Reset the alarm service.
			if (started) {
				long nextTime = SAMPLE; //Measurement every nextTime seconds
				getAlarmService().addRealTimeAlarm(new MeasurementAlarm(nextTime));
			}

		}
	}

	public void readMAU() {

		int i, j, indexs, indexn;
		String str = null;
		String fname = "MAU.txt";
		BufferedReader is = null;

		try {

			is = new BufferedReader(new FileReader(fname));

			str = is.readLine();
			indexs = 0;
			indexn = str.indexOf('\t', 0);
			MAU[0] = Double.parseDouble(str.substring(indexs, indexn));
			indexs = indexn + 1;
			indexn = str.indexOf('\t', indexn + 1);
			MAU[1] = Double.parseDouble(str.substring(indexs, indexn));
			indexs = indexn + 1;
			indexn = str.indexOf('\t', indexn + 1);
			MAU[2] = Double.parseDouble(str.substring(indexs, indexn));
			indexs = indexn + 1;
			indexn = str.indexOf('\t', indexn + 1);
			MAU[3] = Double.parseDouble(str.substring(indexs, indexn));
			indexs = indexn + 1;
			indexn = str.indexOf('\t', indexn + 1);
			MAU[4] = Double.parseDouble(str.substring(indexs, indexn));

		} catch (IOException e) {
		}

	}

	private boolean ifControlMeasurementOk(ControlMeasurement c) {
		HashMap h = c.getOpmodes();
		boolean check = true;
		for (int i = 0; i < SubordinateList.length; i++) {
			if (h.containsKey(MessageAddress.getMessageAddress(SubordinateList[i])) == false) {
				check = false;
				break;
			}
		}
		return check;
	}

	private double getScore(HashMap scores) {
		double score_avg = 0;
		//double[] mau = { 15, 1, -50, -5, -0.2 }; //KAVPF

		String[] Kills = { "BN3.Kills", "BN1.Kills", "BN2.Kills" };
		String[] Attritions = { "BN1.Attrition", "BN2.Attrition", "BN3.Attrition" };
		String[] Violations = { "BN1.Violations", "BN3.Violations", "BN2.Violations" };
		String[] Penalities = { "BN1.Penalties", "BN3.Penalties", "BN2.Penalties" };
		String[] FuelConsumption = { "BN1.FuelConsumption.CPY1", "BN1.FuelConsumption.CPY2", "BN1.FuelConsumption.CPY3", "BN2.FuelConsumption.CPY4", "BN2.FuelConsumption.CPY5", "BN2.FuelConsumption.CPY6", "BN3.FuelConsumption.CPY7", "BN3.FuelConsumption.CPY8", "BN3.FuelConsumption.CPY9" };

		for (int i = 0; i < Kills.length; i++) {
			Object o = scores.get(Kills[i]);
			if (o != null)
				score_avg += (((Double) o).doubleValue()) * MAU[0];
		}
		for (int i = 0; i < Attritions.length; i++) {
			Object o = scores.get(Attritions[i]);
			if (o != null)
				score_avg += (((Double) o).doubleValue()) * MAU[1];
		}

		for (int i = 0; i < Violations.length; i++) {
			Object o = scores.get(Violations[i]);
			if (o != null)
				score_avg += (((Double) o).doubleValue()) * MAU[2];
		}
		for (int i = 0; i < Penalities.length; i++) {
			Object o = scores.get(Penalities[i]);
			if (o != null)
				score_avg += (((Double) o).doubleValue()) * MAU[3];
		}
		for (int i = 0; i < FuelConsumption.length; i++) {
			Object o = scores.get(FuelConsumption[i]);
			if (o != null)
				score_avg += (((Double) o).doubleValue()) * MAU[4];
		}

		return score_avg;
	}

	private boolean hasSystemBeenPurturbed() {
		return true;
	}

	private void shipControlSet(ControlMessage c) {
		for (int l = 0; l < SubordinateList.length; l++)
			sendMessage((Serializable) c, csbr[l]);
	}

	private ControlMeasurement consolidateDelays(boolean print) {
		double[][] averageProcessingDelays = new double[29][2];
		double[][] Dlay = getMeanDelay(System.currentTimeMillis(), MEASUREMENT);
		HashMap tempHash = new HashMap();

		//getting BDE avg processing delays
		if (Dlay != null) {
			for (int i = 0; i < 2; i++) {
				averageProcessingDelays[i][0] = Dlay[i][0];
				averageProcessingDelays[i][1] = Dlay[i][1];
			}
		} else
			for (int i = 0; i < 2; i++) {
				averageProcessingDelays[i][0] = -2;
				//		-2 indicates nothing arrived
			}
		//puts all control paramters into hashMap
		tempHash.put((Object) this.getAgentIdentifier(), (Object) putControlParameters());

		//getting BN avg processing delays
		int currPos = 1;
		for (int l = 0; l < SubordinateList.length; l++) {
			if (csbr[l] != null) {
				Object[] o = (csbr[l].clearReponses());
				if (o.length > 0) {
					//TODO scan the complete object array and check for message type
					onm = null; // <-
					processMessage(o); // <-
					//OpmodeNotificationMessage msg = (OpmodeNotificationMessage) o[0];
					OpmodeNotificationMessage msg = (OpmodeNotificationMessage) onm; // <-
					tempHash.put((Object) msg.getEntityName(), (Object) msg.getAllOpmodes());

					double[][] DlayFromSubs = msg.getTimeForModes();
					if (DlayFromSubs != null) {
						for (int i = 2; i < 5; i++) {
							//							System.out.println(
							//								"Mean Delays from "
							//									+ SubordinateList[l]
							//									+ "  "
							//									+ DlayFromSubs[i]
							//									+ " millisecs.");
							averageProcessingDelays[(currPos + 1) + (i - 2)][0] = DlayFromSubs[i][0];
							averageProcessingDelays[(currPos + 1) + (i - 2)][1] = DlayFromSubs[i][1];
						}
					}
				}
			} else {
				//System.out.println("No Delays from " + SubordinateList[l]);
				for (int i = 2; i < 5; i++) {
					averageProcessingDelays[(currPos + 1) + (i - 2)][0] = -2;
					averageProcessingDelays[(currPos + 1) + (i - 2)][1] = -2;
				}
			}
			currPos += 3;
			if (currPos == 10)
				break;
		}

		//getting CPY avg processing delays
		if (currPos == 10) {

			for (int l = 3; l < SubordinateList.length; l++) {
				if (csbr[l] != null) {
					Object[] o = (csbr[l].clearReponses());
					if (o.length > 0) {
						onm = null; // <-
						processMessage(o); // <-
						//OpmodeNotificationMessage msg = (OpmodeNotificationMessage) o[0];
						OpmodeNotificationMessage msg = (OpmodeNotificationMessage) onm; // <-
						tempHash.put((Object) msg.getEntityName(), (Object) msg.getAllOpmodes());
						double[][] DlayFromSubs = msg.getTimeForModes();
						if (DlayFromSubs != null) {
							for (int i = 5; i < 7; i++) {
								//								System.out.println(
								//									"Mean Delays from "
								//										+ SubordinateList[l]
								//										+ "  "
								//										+ DlayFromSubs[i]
								//										+ " millisecs.");
								averageProcessingDelays[(currPos + 1) + (i - 5)][0] = DlayFromSubs[i][0];
								averageProcessingDelays[(currPos + 1) + (i - 5)][1] = DlayFromSubs[i][1];
							}
						}
					}
				} else {
					//System.out.println("No Delays from " + SubordinateList[l]);
					for (int i = 5; i < 7; i++) {
						averageProcessingDelays[(currPos + 1) + (i - 5)][0] = -2;
						averageProcessingDelays[(currPos + 1) + (i - 5)][1] = -2;
					}
				}
				currPos += 2;
			}
		}

		if (print) {
			for (int i = 0; i < 29; i++)
				System.out.println("Consolidated Mean and Variances of Delays from all agents(" + i + "): " + averageProcessingDelays[i][0] + "   " + averageProcessingDelays[i][1] + " millisecs.");
			//			System.out.println(
			//				"DELAY VARIANCE  " + averageProcessingDelays[0][1] + " " + averageProcessingDelays[1][1]);
		}

		ControlMeasurement cm = new ControlMeasurement("MeasurementTimer", "Measurement", this.getAgentIdentifier(), System.currentTimeMillis(), tempHash, averageProcessingDelays);
		return cm;
	}

	private void sendMessage(Object msg, ControlSourceBufferRelay c) {
		boolean wasOpen = true;
		if (!getBlackboardService().isTransactionOpen()) {
			wasOpen = false;
			getBlackboardService().openTransaction();
		}

		if (c != null) {
			c.addOutgoing((Serializable) msg);
			this.getBlackboardService().publishChange((ControlSourceBufferRelay) c);
		}

		if (getBlackboardService().isTransactionOpen() && !wasOpen) {
			getBlackboardService().closeTransaction();
		}
	}

	private void makeMeasurementPoints() {
		// Make some measurement points.
		controlMP = new ControlMeasurementPoint("ControlMeasurementBDE");
		getBlackboardService().publishAdd(controlMP);

		avgScoresMP = new TimePeriodMeasurementPoint("ScoreAverages", Double.class);
		getBlackboardService().publishAdd(avgScoresMP);

	}

	private HashMap putControlParameters() {
		boolean wasOpen = true;
		HashMap temp = new HashMap();
		if (!getBlackboardService().isTransactionOpen()) {
			wasOpen = false;
			getBlackboardService().openTransaction();
		}

		//put all the current opmodes in the opmode notification message
		Collection opModeCollection = opModeCondSubscription.getCollection();
		Iterator iter = opModeCollection.iterator();
		while (iter.hasNext()) {
			OperatingModeCondition omc = (OperatingModeCondition) iter.next();
			temp.put((Object) omc.getName(), (Object) omc.getValue());
		}

		if (getBlackboardService().isTransactionOpen() && !wasOpen) {
			getBlackboardService().closeTransaction();
		}
		return temp;

	}

	public class MeasurementAlarm implements Alarm {
		private boolean expired;

		public MeasurementAlarm(long period) {
			this.period = period;
			this.expirationTime = period + System.currentTimeMillis();
		}

		public boolean cancel() {
			return false;
		}

		public void reset(long currentTime) {
			expired = false;
			expirationTime = currentTime + period;
		}

		public void expire() {
			expired = true;
			measurement();
		}

		public long getExpirationTime() {
			return expirationTime;
		}
		public boolean hasExpired() {
			return expired;
		}

		public long getPeriod() {
			return period;
		}

		public void setPeriod(long period) {
			this.period = period;
		}

		protected long expirationTime;
		protected long period;
	}

	//messages this node gets
	private DataCollector dc = new DataCollector();
	private int timerCount = 0;
	private OpmodeNotificationMessage onm;
	private ControlMeasurementPoint controlMP;
	private TimePeriodMeasurementPoint avgScoresMP;
	LoggingService logger;
	//QMHelper qmh = new QMHelper();
	private ArrayList measurementPoints = new ArrayList();
	long baseTime = 0;
	IncrementalSubscription measurementPointSubscription, opModeCondSubscription;
	private boolean started = false;
	private ControlSourceBufferRelay[] csbr;
	private String[] SubordinateList = { "BN1", "BN2", "BN3", "CPY1", "CPY2", "CPY3", "CPY4", "CPY5", "CPY6", "CPY7", "CPY8", "CPY9" };
	private ControlDisplayPanel cdp;
	private long lastTime = 0;
	private long startTime = 0;
	double[] MAU = new double[5];
	 
	private static final int CONTROL = 4; // 12 * 10sec = 120 sec = 2 min {or 4 if the sample is 30 seonds}
	private static final int MEASUREMENT = 2; //last six for averaging = 6*10 = 60 sec = 1 min {or 2 if the sample is 30 seconds}
	private static final int SAMPLE = 30000; //every 10 or 30 seconds 
	private static final int SCORE = 24; // 1 every 5 seconds=> 24 for a 2 minutes
	

}

//-------------------old style -----------------------------------------
//TODO remove later
//qmh.invokeMatlab(c.getTaskTimes());

//if inside qmh something is in process, the function will return a null
//HashMap h = qmh.getControlSet(processingDelayStats);

//a null HashMap ensures no opmodes are shipped
//if (h != null) {
//	shipControlSet(h);
//}
//TODO REMOVE: TEST FOR SENDING MESSAGES BACK TO SUBORDINATES:
//ControlMessage controlMsg = new ControlMessage("a", "b");
//controlMsg.putControlParameter((Object) "BN1Replan", new Integer(10));
//for (int l = 0; l < SubordinateList.length; l++)
//sendMessage((Serializable) controlMsg, csbr[l]);
//------------------end old style---------------------------------------
