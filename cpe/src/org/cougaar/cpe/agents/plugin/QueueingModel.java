/*
 * Created on May 20, 2004
 *
 */
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

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class QueueingModel extends ComponentPlugin {
	private LoggingService log;

	private double BCMPQueueingModel() {
		double MPF = 1000.00;
		return MPF;
	}

	private double WWhittQueueingModel() {
		double MPF = 1000.00;
		return MPF;
	}

	private void searchBestSet() {
		return;
	}

	public void processMessage(Object o) {
	}

	//get subscriptions of the measurements points - from BDEAgentPlugin, C2AgentPlugin, UnitAgentPlugin
	protected void setupSubscriptions() {
		logger =
			(LoggingService) getServiceBroker().getService(
				this,
				LoggingService.class,
				null);

		baseTime = System.currentTimeMillis();

		measurementPointSubscription =
			(IncrementalSubscription) getBlackboardService()
				.subscribe(new UnaryPredicate() {
			public boolean execute(Object o) {
				return o instanceof MeasurementPoint;
			}
		});

		setupRelaySubscriptions();

	}

	/**
	 * Sets up control source buffer relays to every subordinate.
	 * The list of subordinates is currently hard-coded.
	 *
	 */
	private void setupRelaySubscriptions() {

		csbr = new ControlSourceBufferRelay[SubordinateList.length];
		UIDService service =
			(UIDService) getServiceBroker().getService(
				this,
				UIDService.class,
				null);
		for (int i = 0; i < SubordinateList.length; i++) {
			csbr[i] =
				new ControlSourceBufferRelay(
					service.nextUID(),
					MessageAddress.getMessageAddress(SubordinateList[i]),
					getAgentIdentifier());
			getBlackboardService().publishAdd(csbr[i]);
		}
	}

	/**
	 * method for calculating average delay
	 * @param currentTime
	 * @param period
	 * @return moving average for the last n points specified by period
	 */
	public double[][] getMeanDelay(long currentTime, int period) {
		double[][] avg_delays = new double[7][2];
		long[][] total_delay = new long[7][3];

		Iterator iter = null;
		int count = 0;

		if (period == 0)
			return null;

		try {
			//System.out.println(
			//"Measurement points size " + measurementPoints.size());
			for (int i = 0; i < measurementPoints.size(); i++) {
				MeasurementPoint measurementPoint =
					(MeasurementPoint) measurementPoints.get(i);
				//System.out.println(
				//	"Measurement points name " + measurementPoint.getName());
				long[] delays = new long[period];
				if (measurementPoint
					instanceof EventDurationMeasurementPoint) {
					EventDurationMeasurementPoint dmp =
						(EventDurationMeasurementPoint) measurementPoint;
					iter = dmp.getMeasurements(period);
				}
				int typeOfDelayMeasurement = -1;
				//System.out.println("iter " + iter.hasNext());
				if ((iter != null) && (iter.hasNext())) {
					//first column is total delay, second column is number of delay points
					count = 0;
					while (iter.hasNext()) {
						DelayMeasurement delayMeasurement =
							(DelayMeasurement) iter.next();
						long delay = 0;

						//						System.out.println(
						//							"DelayMeasurment " + delayMeasurement);
						if (delayMeasurement.getAction() != null) {
							if ((delayMeasurement
								.getAction()
								.equals("ZonePlan"))
								&& (total_delay[0][1] < period)) {
								delay =
									delayMeasurement.getLocalTime()
										- delayMeasurement.getTimestamp();
								total_delay[0][0] += delay;
								total_delay[0][1]++;
								typeOfDelayMeasurement = 0;
								//System.out.println(
								//	"DELAY   ZonePlan: " + delay);
							} else if (
								(delayMeasurement
									.getAction()
									.equals("ProcessUpdateBDE"))
									&& (total_delay[1][1] < period)) {
								delay =
									delayMeasurement.getLocalTime()
										- delayMeasurement.getTimestamp();
								total_delay[1][0] += delay;
								total_delay[1][1]++;
								//System.out.println(
								//	"DELAY   ProcessUpdateBDE: " + delay);
								typeOfDelayMeasurement = 1;
							}
						}
						if (delay > 0) {
							delays[count] = delay;
							count++;
						}
					}

					for (int ii = 0; ii < 7; ii++) {
						if (total_delay[ii][1] > 0) {

							avg_delays[ii][0] =
								total_delay[ii][0] / total_delay[ii][1];
						} else {

							avg_delays[ii][0] = -1;
						}
					}
					if (count > 0) {
						double sum = 0;
						for (int p = 0; p < count; p++) {
							double diffSq =
								(double) Math.pow(
									delays[p]
										- avg_delays[typeOfDelayMeasurement][0],
									2);

							//System.out.println("diffSq: " + diffSq);					
							sum += diffSq;
							//System.out.println("sum: " + sum);

						}
						double variance = (double) (sum / count);
						//System.out.println("count: " + count);
						avg_delays[typeOfDelayMeasurement][1] = variance;
					}

					//TODO pass this variance as the second argument of avg_delays
					//TODO rename avg_delays to stats
					//should add a method in Measurement.java to return the last n by number or time.

					//logger.shout(
					//	"\n *--------------avg delays computed----------------* ");
				}
			}
			return avg_delays;
		} catch (Exception e) {
			//throw new RuntimeException("Unexpected condition reached");
			e.printStackTrace();
			return null;
		}
	}

	public void matlab(double[][] stats) {
		MatEng eng = null;
		try {
			eng = new MatEng();
			eng.open(null);
			eng.setBufSize(1024);

			//forming the mean and variance strings
			String means = "";
			String variances = "";
			for (int i = 0; i < stats.length; i++) {

				if (i == 0) {
					means += "SM = [" + stats[i][0] + " ";
					variances += "SV = [" + stats[i][1] + " ";
				} else if (i == (stats.length - 1)) {
					means += stats[i][0] + " " + "]";
					variances += stats[i][1] + " " + "]";
				} else {
					means +=  stats[i][0] + " ";
					variances +=  stats[i][1] + " ";
				}

			}
			System.out.println(means);
			System.out.println(variances);
			eng.evalString(means + ";" + variances);
			eng.evalString("whitt(SM,SV)");
			
			FloatMatrix fm = eng.getArray("means");
			
			System.out.println("MATLAB OUTPUT: "+fm.toString());
			
			 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			if (eng!=null)
//				eng.close();
			logger.shout(
				"\n *------------------EXITING MATLAB-------------* @"
					+ this.getAgentIdentifier());
		}
	}

	//Here the measurements will be used in the queueing models
	public void execute() {
		//logger.shout("\n *------------------Execute Called---------------* ");
		if (!started) {
			started = true;
			measurement();
		}

	}

	public void measurement() {
		try {

			measurementPoints.clear();
			measurementPoints.addAll(
				measurementPointSubscription.getCollection());

			//			find the mean and variance
			logger.shout(
				"\n *------------------CALCULATING DELAY-------------* @"
					+ this.getAgentIdentifier());
			double[][] stats = consolidateDelays(true);
			matlab(stats);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Now, reset the alarm service.
			if (started) {
				long nextTime = 10000;
				//				System.out.println(
				//					getAgentIdentifier()
				//						+ ":  SCHEDULING NEXT MEASUREMENT in "
				//						+ nextTime / 1000
				//						+ " secs.");
				getAlarmService().addRealTimeAlarm(
					new MeasurementAlarm(nextTime));
			}

		}
	}

	private double[][] consolidateDelays(boolean print) {
		double[][] averageProcessingDelays = new double[29][2];
		double[][] Dlay = getMeanDelay(System.currentTimeMillis(), 20);

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

		//getting BN avg processing delays
		int currPos = 1;
		for (int l = 0; l < SubordinateList.length; l++) {
			if (csbr[l] != null) {
				Object[] o = (csbr[l].clearReponses());
				if (o.length > 0) {
					double[][] DlayFromSubs = (double[][]) o[0];
					if (DlayFromSubs != null) {
						for (int i = 2; i < 5; i++) {
							//							System.out.println(
							//								"Mean Delays from "
							//									+ SubordinateList[l]
							//									+ "  "
							//									+ DlayFromSubs[i]
							//									+ " millisecs.");
							averageProcessingDelays[(currPos + 1) + (i - 2)][0] =
								DlayFromSubs[i][0];
							averageProcessingDelays[(currPos + 1) + (i - 2)][1] =
								DlayFromSubs[i][1];
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
						double[][] DlayFromSubs = (double[][]) o[0];
						if (DlayFromSubs != null) {
							for (int i = 5; i < 7; i++) {
								//								System.out.println(
								//									"Mean Delays from "
								//										+ SubordinateList[l]
								//										+ "  "
								//										+ DlayFromSubs[i]
								//										+ " millisecs.");
								averageProcessingDelays[(currPos + 1)
									+ (i - 5)][0] =
									DlayFromSubs[i][0];
								averageProcessingDelays[(currPos + 1)
									+ (i - 5)][1] =
									DlayFromSubs[i][1];
							}
						}
					}
				} else {
					//System.out.println("No Delays from " + SubordinateList[l]);
					for (int i = 5; i < 7; i++) {
						averageProcessingDelays[(currPos + 1) + (i - 5)][0] =
							-2;
						averageProcessingDelays[(currPos + 1) + (i - 5)][1] =
							-2;
					}
				}
				currPos += 2;
			}
		}

		if (print) {
			for (int i = 0; i < 29; i++)
				System.out.println(
					"Consolidated Mean and Variances of Delays from all agents("
						+ i
						+ "): "
						+ averageProcessingDelays[i][0]
						+ "   "
						+ averageProcessingDelays[i][1]
						+ " millisecs.");
			//			System.out.println(
			//				"DELAY VARIANCE  " + averageProcessingDelays[0][1] + " " + averageProcessingDelays[1][1]);
		}

		return averageProcessingDelays;
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

	LoggingService logger;
	private ArrayList measurementPoints = new ArrayList();
	long baseTime = 0;
	IncrementalSubscription measurementPointSubscription;
	private boolean started = false;
	private ControlSourceBufferRelay[] csbr;
	private String[] SubordinateList =
		{
			"BN1",
			"BN2",
			"BN3",
			"CPY1",
			"CPY2",
			"CPY3",
			"CPY4",
			"CPY5",
			"CPY6",
			"CPY7",
			"CPY8",
			"CPY9" };

}
