package org.cougaar.cpe.ui;

import org.cougaar.cpe.model.WorldState;
import org.cougaar.cpe.model.VGWorldConstants;
import org.cougaar.cpe.agents.plugin.C2AgentPlugin;
import org.cougaar.cpe.agents.plugin.WorldStateReference;
import org.cougaar.cpe.relay.SourceBufferRelay;
import org.cougaar.cpe.relay.TargetBufferRelay;
import org.cougaar.cpe.unittests.MetricsGraphPanel;
import org.cougaar.cpe.unittests.ControlMetricsGraphPanel;
import org.cougaar.tools.techspecs.qos.*;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.relay.Relay;

import org.cougaar.cpe.agents.plugin.control.*;
import org.cougaar.cpe.agents.plugin.QueueingModel;

import java.awt.*;
import java.util.*;

/**
 * User: Nathan Gnansambandam
 * Date: Sep 29, 2004
 * Time: 3:17:20 PM
 */
public class ControlDisplayPanel extends JFrame {

	private String agentName;
	private RefreshThread refreshThread;
	private JScrollPane spMeasurementPanel;
	private QPanel qPanel;

	static class QueueingParametersTableModel implements TableModel {

		final static String[] COLUMN_NAMES = { "Type", "MPF Analytical(Z1)", "MPF Analytical(Z2)", "MPF Analytical(Z3)", "Score (Z1)", "Score (Z2)", "Score (Z3)" };

		public int getRowCount() {
			return qp.size();
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}

		public Class getColumnClass(int columnIndex) {
			return String.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public void setData(Collection data) {
			synchronized (qp) {
				qp.clear();
				qp.addAll(data);
			}
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				TableModelListener l = (TableModelListener) iterator.next();
				l.tableChanged(new TableModelEvent(this));
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object qParas = null;
			synchronized (qp) {
				qParas = qp.get(rowIndex);
			}
			if (qParas instanceof QueueingParameters) {
				QueueingParameters q = (QueueingParameters) qParas;
				switch (columnIndex) {
					case 0 :
						return "Estimated";
					case 1 :
						return ((Double) q.getValue("MG1", 0)).toString();
					case 2 :
						return ((Double) q.getValue("MG1", 1)).toString();
					case 3 :
						return ((Double) q.getValue("MG1", 2)).toString();
					case 4 :
						return ((Double) q.getValue("score", 0)).toString();
					case 5 :
						return ((Double) q.getValue("score", 1)).toString();
					case 6 :
						return ((Double) q.getValue("score", 2)).toString();
					default :
						return "?";
				}
			}

			return "?";
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		ArrayList qp = new ArrayList();
		ArrayList listeners = new ArrayList();
	}

	public class QPanel extends JPanel {
		private GridBagLayout gbl;
		private QueueingParametersTableModel model;

		public QPanel() {
			setLayout(gbl = new GridBagLayout());

			// Add a JTable
			model = new QueueingParametersTableModel();

			JTable table = new JTable(model);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = gbc.weighty = 100;
			gbc.insets = new Insets(32, 32, 32, 32);

			JScrollPane sp;
			gbl.setConstraints(sp = new JScrollPane(table), gbc);
			add(sp);

			JPanel panel = new JPanel(new FlowLayout());
			// Add a button to dump results and to test liveness of the relays.
			JButton logResults = new JButton("Stop");
			panel.add(logResults);
			JButton testRelay = new JButton("Refresh");
			panel.add(testRelay);

			gbc.gridx = 0;
			gbc.gridy++;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 32, 32, 32);
			gbc.weightx = 100;
			gbc.weighty = 1;
			gbl.setConstraints(panel, gbc);
			add(panel);
		}

		public void updateData(Collection c) {
			model.setData(c);
		}
	}

	static class AverageScoresTableModel implements TableModel {

		final static String[] COLUMN_NAMES = { "Type", "Value" };

		public int getRowCount() {
			return keys.length;
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}

		public Class getColumnClass(int columnIndex) {
			return String.class;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public void setData(HashMap data) {
			synchronized (scores) {
				scores.clear();
				scores.putAll(data);
			}
			for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
				TableModelListener l = (TableModelListener) iterator.next();
				l.tableChanged(new TableModelEvent(this));
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Object o = null;
			synchronized (scores) {
				o = scores.get(keys[rowIndex]);
			}
			if (o instanceof Double) {
				String d = ((Double) o).toString();
				switch (columnIndex) {
					case 0 :
						return keys[rowIndex];
					case 1 :
						return d;
					default :
						return "?";
				}
			}

			return "?";
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}

		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		String[] keys =
			{
				"BN1.Attrition",
				"BN1.EntryRate",
				"BN2.Violations",
				"BN1.Violations",
				"BN1.Penalties",
				"BN3.Penalties",
				"BN3.Kills",
				"BN2.Attrition",
				"BN3.Violations",
				"BN1.Kills",
				"BN2.Penalties",
				"BN2.Kills",
				"BN3.EntryRate",
				"BN3.Attrition",
				"BN2.EntryRate",
				"BN1.FuelConsumption.CPY1",
				"BN1.FuelConsumption.CPY2",
				"BN1.FuelConsumption.CPY3",
				"BN2.FuelConsumption.CPY4",
				"BN2.FuelConsumption.CPY5",
				"BN2.FuelConsumption.CPY6",
				"BN3.FuelConsumption.CPY7",
				"BN3.FuelConsumption.CPY8",
				"BN3.FuelConsumption.CPY9" };
		HashMap scores = new HashMap();
		ArrayList listeners = new ArrayList();
	}

	public class ScoresPanel extends JPanel {
		private GridBagLayout gbl;
		private AverageScoresTableModel model;

		public ScoresPanel() {
			setLayout(gbl = new GridBagLayout());

			// Add a JTable
			model = new AverageScoresTableModel();

			JTable table = new JTable(model);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = gbc.weighty = 100;
			gbc.insets = new Insets(32, 32, 32, 32);

			JScrollPane sp;
			gbl.setConstraints(sp = new JScrollPane(table), gbc);
			add(sp);

			JPanel panel = new JPanel(new FlowLayout());
			// Add a button to dump results and to test liveness of the relays.
			JButton logResults = new JButton("Stop");
			panel.add(logResults);
			JButton testRelay = new JButton("Refresh");
			panel.add(testRelay);

			gbc.gridx = 0;
			gbc.gridy++;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 32, 32, 32);
			gbc.weightx = 100;
			gbc.weighty = 1;
			gbl.setConstraints(panel, gbc);
			add(panel);
		}

		public void updateData(HashMap c) {
			model.setData(c);
		}
	}

	public ControlDisplayPanel(String agentName, QueueingModel plugin) {
		this.plugin = plugin;
		this.agentName = agentName;
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(sp, BorderLayout.CENTER);
		sp.setLeftComponent(leftDisplayPanel = new WorldDisplayPanel(null));
		setSize(640, 480);
		updateTitle();

		sp.setRightComponent(rightTabbedPane = new JTabbedPane());
		rightTabbedPane.add("Metrics", spMeasurementPanel = new JScrollPane(measurementPanel = new JPanel()));
		if (spMeasurementPanel != null) {
			spMeasurementPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			spMeasurementPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		rightTabbedPane.add("Average Scores", controlPanel = new ScoresPanel());
		sp.setDividerLocation(400);

		rightTabbedPane.add("Queueing Parameters", qPanel = new QPanel());

		refreshThread = new RefreshThread();
		refreshThread.start();
	}

	protected void updateTitle() {
		StringBuffer buf = new StringBuffer();
		buf.append("Control Display Panel [Agent=");
		buf.append(agentName);
		buf.append("]");
		setTitle(buf.toString());
	}

	public void updateWorldStates(ArrayList worldStateReferences) {
	}

	protected Dimension panelSize = new Dimension(320, 320);

	public void updateMeasurements(ArrayList measurementPoints) {
		measurementPanel.removeAll();
		mpDisplayPanels.clear();

		GridBagLayout gbl = new GridBagLayout();
		measurementPanel.setLayout(gbl);
		int gridx = 0, gridy = 0;

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		gbc.anchor = GridBagConstraints.CENTER;

		for (int i = 0; i < measurementPoints.size(); i++) {
			MeasurementPoint measurementPoint = (MeasurementPoint) measurementPoints.get(i);
			//&& measurementPoint.getName().equalsIgnoreCase("ScoreAverages"
			if ((measurementPoint instanceof TimePeriodMeasurementPoint)) {
				gbc.gridx = gridx;
				gbc.gridy = gridy;
				TimePeriodMeasurementPoint tmp = (TimePeriodMeasurementPoint) measurementPoint;
				MetricsGraphPanel panel = new MetricsGraphPanel(tmp);
				panel.setMinimumSize(panelSize);
				panel.setPreferredSize(panelSize);
				gbl.setConstraints(panel, gbc);
				measurementPanel.add(panel);
				mpDisplayPanels.add(panel);
				gridy++;
			}
			if ((measurementPoint instanceof ControlMeasurementPoint)) {
				gbc.gridx = gridx;
				gbc.gridy = gridy;
				ControlMeasurementPoint tmp = (ControlMeasurementPoint) measurementPoint;
				ControlMetricsGraphPanel panel = new ControlMetricsGraphPanel(tmp);
				panel.setMinimumSize(panelSize);
				panel.setPreferredSize(panelSize);
				gbl.setConstraints(panel, gbc);
				measurementPanel.add(panel);
				mpDisplayPanels.add(panel);
				gridy++;
			}
		}

		if (spMeasurementPanel != null) {
			spMeasurementPanel.invalidate();
			spMeasurementPanel.validate();
			spMeasurementPanel.repaint();
		}
		measurementPanel.invalidate();
		measurementPanel.repaint();
	}

	public void updateQueueingParameters(Collection collection) {
		qPanel.updateData(collection);
	}

	public void updateControls(HashMap scores) {
		controlPanel.updateData(scores);
	}

	private void updateMeasurementPanels() {

		for (int i = 0; i < mpDisplayPanels.size(); i++) {
			Object o = mpDisplayPanels.get(i);
			if (o instanceof MPObserver) {
				MPObserver mp = (MPObserver) o;
				mp.updateData();
			}
		}
	}

	public void execute() {
	}

	ArrayList worldStatePanels = new ArrayList();
	ArrayList opModeControlPanels = new ArrayList();
	ArrayList mpDisplayPanels = new ArrayList();

	JTabbedPane rightTabbedPane = new JTabbedPane();

	/**
	 * If this non-null, this is the left panel.
	 */
	JTabbedPane leftTabbedPane = new JTabbedPane();

	JPanel measurementPanel;
	ScoresPanel controlPanel;
	WorldDisplayPanel leftDisplayPanel;
	QueueingModel plugin;
	JSplitPane sp;

	private class RefreshThread extends Thread {
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

				}

				//controlPanel.updateValues();
				updateMeasurementPanels();
				qPanel.repaint();
				if (leftDisplayPanel != null) {
					leftDisplayPanel.repaint();
				}

			}
		}

	}
}
