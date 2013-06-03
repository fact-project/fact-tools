package fact.viewer.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import fact.FactViewer;

public class CamWindow {

	short selectIndex = 0;
	Data event;
	final FactViewer mainWindow;
	static Logger log = LoggerFactory.getLogger(CamWindow.class);

	private MapView mapPanel;

	public CamWindow(FactViewer m) {
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		JFrame frame = new JFrame();
		mapPanel = new MapView(m, false, true);
		mapPanel.getPreferredSize();

		/**
		 * add all the stuff to frame and pack it. Also add a close listener to
		 * notify the main window that the window has been closed
		 */
		frame.getContentPane().add(mapPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				log.debug("CamWindow is being closed. camwindowlist size is currently: " + mainWindow.getCamWindowList().size());
				mainWindow.getCamWindowList().remove(CamWindow.this);
				log.debug("CamWindow has been closed. camwindowlist size is currently: " + mainWindow.getCamWindowList().size());
			}
		});
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);

	}

	public void setEvent(Data event) {
		if (event != null) {
			this.event = event;
			mapPanel.setEvent(event);
		}

	}

	public void setSlice(int i) {
		mapPanel.setSlice(i);
	}

	public void setActiveOverlayKeys(Set<String> selectedKeys) {
		mapPanel.setActiveOverlayKeys(selectedKeys);
	}

}
