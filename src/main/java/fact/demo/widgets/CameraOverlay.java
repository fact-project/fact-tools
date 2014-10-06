/**
 * 
 */
package fact.demo.widgets;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.util.Time;
import streams.dashboard.Widget;
import fact.Utils;
import fact.demo.ui.HexMap;

/**
 * @author chris
 * 
 */
public class CameraOverlay extends Widget {

	/** The unique class ID */
	private static final long serialVersionUID = 1262313429564896384L;

	static Logger log = LoggerFactory.getLogger(CameraOverlay.class);

	final HexMap hexMap = new HexMap();
	Double radius = 3.5d;
	Time delay = new Time(10L);

	public CameraOverlay() {
		setContent(hexMap);

		double[] data = new double[1440 * 300];
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.0;
		}

		hexMap.setBackground(new Color(21, 30, 3));

		hexMap.setTimeScale(false);
		hexMap.setData(data);
		hexMap.setShowerIds(new int[0]);
		hexMap.repaint();
	}

	/**
	 * @param radius
	 *            the radius to set
	 */
	public void setRadius(Double radius) {
		if (radius > 1.0) {
			log.debug("Setting radius to {}", radius);
			this.radius = radius;
			this.hexMap.setRadius(radius);
		}
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(Time delay) {
		if (delay.asMillis() > 0) {
			this.delay = delay;
		}
	}

	/**
	 * @see streams.dashboard.Widget#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		String num = input.get("EventNum") + "";
		setTitle("Camera - Event " + num);

		double[] max = new double[1440];

		double[] data = Utils.toDoubleArray(input.get("DataCalibrated"));
		data = new double[1440 * 300];
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.0;

			int pix = i / 300;
			max[pix] = Math.max(max[pix], data[i]);
		}

		int[] shower = null;
		if (input.get("shower") != null) {
			setTitle("Event " + num + " - Shower Detected");
			shower = (int[]) input.get("shower");
			hexMap.setShowerIds(shower);
			hexMap.setData(data);

			hexMap.setCurrentSlice(60);
			hexMap.repaint();
			return input;
		} else {
			setTitle("Event " + num + " - No Shower Identified");
			hexMap.setShowerIds(new int[0]);

			hexMap.setData(data);
			hexMap.repaint();
			return input;
		}

		// synchronized (hexMap) {
		// hexMap.setShowerIds(shower);
		// hexMap.setData(data);
		//
		// hexMap.play(delay.asMillis());
		//
		// while (hexMap.isPlaying()) {
		// try {
		// hexMap.wait(250L);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }

		// return input;
	}
}
