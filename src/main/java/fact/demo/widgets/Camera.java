/**
 * 
 */
package fact.demo.widgets;

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
public class Camera extends Widget {

	/** The unique class ID */
	private static final long serialVersionUID = 1262313429564896384L;

	static Logger log = LoggerFactory.getLogger(Camera.class);

	final HexMap hexMap = new HexMap();
	Double radius = 3.5d;
	Time delay = new Time(10L);

	public Camera() {
		setContent(hexMap);
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

		setTitle("Camera - Event " + input.get("EventNum"));

		double[] data = Utils.toDoubleArray(input.get("DataCalibrated"));
		int[] shower = null;
		if (input.get("shower") != null) {
			shower = (int[]) input.get("shower");
		}
		synchronized (hexMap) {
			hexMap.setShowerIds(shower);
			hexMap.setData(data);

			hexMap.play(delay.asMillis());

			while (hexMap.isPlaying()) {
				try {
					hexMap.wait(250L);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return input;
	}
}
