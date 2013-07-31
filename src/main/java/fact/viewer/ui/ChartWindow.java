package fact.viewer.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JFrame;

import stream.Data;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import fact.Constants;
import fact.FactViewer;
import fact.utils.FactEvent;

public class ChartWindow {

	short selectIndex = 0;
	private int slice;
	// private CameraPixelMap camMap;
	Data event;
	FactViewer mainWindow;
	private SimplePlotPanel plotPanel;
	private SourceSelector sL;
	private Set<Integer> set;

	public Set<Integer> getSet() {
		return set;
	}

	public void setSet(Set<Integer> set) {
		this.set = set;
	}

	public SimplePlotPanel getPlotPanel() {
		return plotPanel;
	}

	public void setPlotPanel(SimplePlotPanel plotPanel) {
		this.plotPanel = plotPanel;
	}

	/**
	 * @param m
	 */
	public ChartWindow(FactViewer m) {
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("pref:grow"),
				ColumnSpec.decode("pref:grow"),},
			new RowSpec[] {
				RowSpec.decode("max(445px;min):grow"),}));
	
		// frame.setResizable(false);

		plotPanel = new SimplePlotPanel();
		plotPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		plotPanel.setBorder(null);
		frame.getContentPane().add(plotPanel, "1, 1, fill, fill");
		sL = new SourceSelector(event, this);
		sL.setAlignmentX(Component.CENTER_ALIGNMENT);
		sL.setAlignmentY(Component.TOP_ALIGNMENT);
		sL.setPreferredSize(new Dimension(100,
				plotPanel.getPreferredSize().height));
		frame.getContentPane().add(sL, "2, 1, fill, fill");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainWindow.getChartWindowList().remove(this);
			}
		});
		frame.pack();
		frame.setVisible(true);

	}

	public void setEvent(Data event, Set<Integer> set) {
		this.event = event;
		sL.setEvent(event);
		updateGraph(event, set);
	}

	// just redraw some shiiiit
	public void updateGraph() {
		updateGraph(event, set);
	}

	/**
	 * @param event
	 * @param set
	 */
	public void updateGraph(Data event, Set<Integer> setP) {
		set = setP;
		if (event != null) {
			plotPanel.clearSeries();
				for (String key : sL.getSelectedKeys()) {
					addSeriesToPlot(event, set, key);
				}
		}
	}

	/**
	 * @param event
	 * @param set
	 */
	private void addSeriesToPlot(Data event, Set<Integer> set, String key) {
		if (!set.isEmpty()) {
			for (int id : set) {
				/**
				 * TODO: why get hId??
				 */
				id = FactEvent.PIXEL_MAPPING.getChidID(id);
				float[] data = (float[]) event.get(key);
				int roi = data.length / Constants.NUMBEROFPIXEL;
				plotPanel.addSeries(key + "-" + id, data, roi * id, (roi * id)
						+ roi);

			}
		} else {
			int id = 0;
			float[] data = (float[]) event.get(key);
			int roi = data.length / Constants.NUMBEROFPIXEL;
			plotPanel.addSeries(key + "-" + id, data, roi * id, (roi * id)
					+ roi);
		}
	}

	public void setSlice(int i) {
		slice = i;
		plotPanel.setSlice(slice);
	}

}
