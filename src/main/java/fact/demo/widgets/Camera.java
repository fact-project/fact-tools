/**
 * 
 */
package fact.demo.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.util.Time;
import streams.dashboard.ColorMapping;
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
	boolean play = true;

	public Camera() {
		setContent(hexMap);
		hexMap.setSliceInterval(10, 150);
	}

	/**
	 * @see streams.dashboard.Widget#setColors(streams.dashboard.ColorMapping)
	 */
	@Override
	public void setColors(ColorMapping colorMapping) {
		super.setColors(colorMapping);

		if (hexMap != null) {
			hexMap.setBackground(getBackground());
		}
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

		if (!play) {
			int slices = data.length / 1440;
			double[] sliceSums = new double[slices];
			for (int i = 0; i < sliceSums.length; i++) {
				sliceSums[i] = 0.0;
			}

			for (int pix = 0; pix < 1440; pix++) {
				for (int s = 0; s < slices && s < 150; s++) {
					sliceSums[s] += data[pix * slices + s];
				}
			}

			double max = 0.0;
			int maxIdx = -1;
			for (int i = 0; i < sliceSums.length; i++) {
				if (maxIdx < 0) {
					maxIdx = i;
					max = sliceSums[i];
				}

				if (max < sliceSums[i]) {
					maxIdx = i;
					max = sliceSums[i];
				}
			}

			synchronized (hexMap) {
				hexMap.setData(data);
				hexMap.setCurrentSlice(maxIdx);
				hexMap.repaint();
			}
		} else {
			synchronized (hexMap) {
				// hexMap.setShowerIds(shower);
				hexMap.setData(data);

				hexMap.play(delay.asMillis());

				while (hexMap.isPlaying()) {
					try {
						hexMap.wait(100L);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return input;
	}

	/**
	 * @return the play
	 */
	public boolean isPlay() {
		return play;
	}

	/**
	 * @param play
	 *            the play to set
	 */
	public void setPlay(boolean play) {
		this.play = play;
	}

}
