package fact.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import stream.Data;
import fact.FactViewer;

public class EventInfoWindow {

	JComboBox comboBox;
	short selectIndex = 0;
	protected int slice;
	private CameraPixelMap camMap;

	public CameraPixelMap getCamMap() {
		return camMap;
	}

	public void setCamMap(CameraPixelMap camMap) {
		this.camMap = camMap;
	}

	Data event;
	FactViewer mainWindow;
	private EventInfo ev;

	public EventInfoWindow(FactViewer m) {
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel mapPanel = new JPanel();
		mapPanel.setLayout(new BorderLayout(0, 0));
		ev = new EventInfo();
		ev.setEditable(false);
		ev.setEnabled(true);
		JScrollPane textAreaScrollPane = new JScrollPane(ev,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		// setEvent(event);

		// mapPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// mapPanel.setViewportView(camMap);
		textAreaScrollPane.setPreferredSize(new Dimension(300, 400));
		mapPanel.add(textAreaScrollPane, BorderLayout.CENTER);
		/**
		 * add all the stuff to frame and pack it. Also add a close listener to
		 * notify the main window that the window has been closed
		 */
		frame.getContentPane().add(mapPanel, BorderLayout.CENTER);
		// mapPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		// mapPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null,
		// null, null, null));

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainWindow.getEvWList().remove(EventInfoWindow.this);
			}
		});
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);

	}

	public void setEvent(Data event) {
		this.event = event;
		// int[] selected = itemList.getSelectedIndices();
		// comboBox.removeAll();
		// ArrayList<String> m = new ArrayList<String>();
		ev.setEvent(event);

	}

	public void setSelectedIds(Set<Integer> selectedIds) {

		ev.setSoftIds(selectedIds);
	}

}
