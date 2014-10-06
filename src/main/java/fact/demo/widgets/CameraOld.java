/**
 * 
 */
package fact.demo.widgets;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class CameraOld extends Widget {

	/** The unique class ID */
	private static final long serialVersionUID = -429802545013541790L;

	static Logger log = LoggerFactory.getLogger(CameraOld.class);

	final FactHexMapDisplay cameraPanel;
	int slices = 300;

	final AtomicBoolean playing = new AtomicBoolean(false);

	public CameraOld() {
		setTitle("Camera");
		cameraPanel = new FactHexMapDisplay(3.5, 600, 600, false);
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

		play(10);

		synchronized (playing) {
			while (isPlaying()) {
				try {
					playing.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return input;
	}

	public void play(long ms) {
		if (playing.get()) {
			return;
		} else {
			playing.set(true);
		}

		Thread t = new Thread() {
			public void run() {

				for (int s = 0; s < slices; s += 2) {
					cameraPanel
							.handleSliceChangeEvent(new SliceChangedEvent(s));
					// this.validate();
					doSleep(10);
				}

				playingFinished();
			}

			public void doSleep(long ms) {
				try {
					Thread.sleep(ms);
				} catch (Exception e) {
				}
			}
		};

		t.start();
	}

	private void playingFinished() {
		synchronized (playing) {
			playing.set(false);
			try {
				playing.notifyAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isPlaying() {
		return playing.get();
	}

}