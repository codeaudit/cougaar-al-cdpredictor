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
import java.io.Serializable;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ControlC2AgentPlugin extends ComponentPlugin {

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

		controlTargetRelaySubscription =
			(IncrementalSubscription) getBlackboardService()
				.subscribe(new UnaryPredicate() {
			public boolean execute(Object o) {
				if (o instanceof ControlTargetBufferRelay) {
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * method for calculating average delay
	 * @param currentTime
	 * @param period
	 * @return moving average for the last n points specified by period
	 */
	public double[][] getMeanDelay(long currentTime, int period) {
		double[][] avg_delays = new double[7][2];
		Iterator iter = null;
		double[][] total_delay = new double[7][3];
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
						//TODO getEvent or getAction() resolve									
						//TODO some duplicate names in call to delaymeasurement
						if (delayMeasurement.getAction() != null) {
							if ((delayMeasurement
								.getAction()
								.equals("ReplanTime"))
								&& (total_delay[2][1] < period)) {
								delay =
									delayMeasurement.getLocalTime()
										- delayMeasurement.getTimestamp();
								total_delay[2][0] += delay;
								total_delay[2][1]++;
								typeOfDelayMeasurement = 2;
								//System.out.println("DELAY " + delay);

							} else if (
								(delayMeasurement
									.getAction()
									.equals("ProcessUpdateBN"))
									&& (total_delay[3][1] < period)) {
								delay =
									delayMeasurement.getLocalTime()
										- delayMeasurement.getTimestamp();
								total_delay[3][0] += delay;
								total_delay[3][1]++;
								typeOfDelayMeasurement = 3;
								//System.out.println("DELAY " + delay);
							} else if (
								(delayMeasurement
									.getAction()
									.equals("ProcessZonePlanBN"))
									&& (total_delay[4][1] < period)) {
								delay =
									delayMeasurement.getLocalTime()
										- delayMeasurement.getTimestamp();
								total_delay[4][0] += delay;
								total_delay[4][1]++;
								System.out.println("DELAY " + delay);
								typeOfDelayMeasurement = 4;
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
								(double) (total_delay[ii][0] / total_delay[ii][1]);
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
				}
			}
			//logger.shout(
			//	"\n *-----------------avg-delays-computed---------------* ");
			return avg_delays;
		} catch (Exception e) {
			//throw new RuntimeException("Unexpected condition reached while calculating mean delays");
			e.printStackTrace();
			return null;
		}
	}

	//Here the measurements will be used in the queueing models
	public void execute() {
		//logger.shout("\n *------------------Execute Called---------------* ");
		if (!started) {
			started = true;
			measurement();
		}
		findRelay();
	}

	public void measurement() {
		try {
			measurementPoints.clear();
			measurementPoints.addAll(
				measurementPointSubscription.getCollection());

			//			find the mean and variance
			//			logger.shout(
			//				"\n *------------------CALCULATING DELAY-------------* @"
			//					+ this.getAgentIdentifier());

			double[][] Dlay = getMeanDelay(System.currentTimeMillis(), 20);

			if (Dlay != null) {
				//				for (int i = 0; i < 7; i++)
				//					System.out.println(
				//						"Mean Delays " + i + "  " + Dlay[i] + " secs.");

				//send the delays through TargetControlBufferRelay
				sendMessage(Dlay);
			}
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
	private void findRelay() {
		//	Find any new relays from one's superior.
		Collection relayCollection =
			controlTargetRelaySubscription.getAddedCollection();
		Iterator iter = relayCollection.iterator();
		//System.out.println("relay iterator in ControlC2AgentPlugin: " + iter);

		while (iter.hasNext()) {
			ControlTargetBufferRelay relay =
				(ControlTargetBufferRelay) iter.next();
			if (relayFromSuperior == null) {
				System.out.println(
					getAgentIdentifier()
						+ " -------------found RELAY from------------- "
						+ relay.getSource());
				relayFromSuperior = relay;
				//				relayFromSuperior.addResponse((Serializable) "GOD");
				//				this.getBlackboardService().publishChange(
				//					(ControlTargetBufferRelay) relayFromSuperior);
			}
		}
	}

	private void sendMessage(Object msg) {
		boolean wasOpen = true;
		if (!getBlackboardService().isTransactionOpen()) {
			wasOpen = false;
			getBlackboardService().openTransaction();
		}

		if (relayFromSuperior != null) {
			relayFromSuperior.addResponse((Serializable) msg);
			this.getBlackboardService().publishChange(
				(ControlTargetBufferRelay) relayFromSuperior);
		}

		if (getBlackboardService().isTransactionOpen() && !wasOpen) {
			getBlackboardService().closeTransaction();
		}
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
	ArrayList measurementPoints = new ArrayList();
	long baseTime = 0;
	IncrementalSubscription measurementPointSubscription,
		controlTargetRelaySubscription;
	private boolean started = false;
	ControlTargetBufferRelay relayFromSuperior;
}
