/**
 * 
 */
package fact.demo.widgets;

import java.awt.Color;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import streams.dashboard.Widget;
import fact.hexmap.ui.colormapping.NeutralColorMapping;
import fact.hexmap.ui.colormapping.RainbowColorMapping;
import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;
import fact.hexmap.ui.events.SliceChangedEvent;

/**
 * @author chris
 * 
 */
public class Camera extends Widget {

	/** The unique class ID */
	private static final long serialVersionUID = -429802545013541790L;

	static Logger log = LoggerFactory.getLogger(Camera.class);

	final FactHexMapDisplay cameraPanel;

	public Camera() {
		setTitle("Camera");
		cameraPanel = new FactHexMapDisplay(3.5, 320, 480, false);
		cameraPanel.setOffsetY(-120);
		cameraPanel.setOffsetX(-30);
		cameraPanel.setBackground(new Color(0xff, 0x0, 0x0, 0x0));
		cameraPanel.setIncludeScale(false);
		cameraPanel.setColorMap(new RainbowColorMapping());
		cameraPanel.setColorMap(new NeutralColorMapping());
		this.setContent(cameraPanel);
		this.setSize(480, 480);
	}

	/**
	 * @see streams.dashboard.Widget#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		log.info("Would need to display event from {}", input);

		if (input.containsKey("EventNum")) {
			setTitle("Camera - Event " + input.get("EventNum"));
		}

		short[] data = (short[]) input.get("Data");
		double[] dd = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			dd[i] = data[i];
		}
		input.put("DataDouble", dd);
		cameraPanel.handleEventChange(new Pair<Data, String>(input,
				"DataDouble"));

		int slices = dd.length / 1440;
		for (int s = 0; s < slices; s += 2) {
			cameraPanel.handleSliceChangeEvent(new SliceChangedEvent(s));
			this.validate();
			sleep(10);
		}

		return input;
	}

	public void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
		}
	}
}