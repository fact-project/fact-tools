/**
 * 
 */
package fact.image;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import stream.AbstractProcessor;
import stream.Data;
import fact.data.EventExpander;
import fact.image.overlays.PixelSet;

/**
 * @author chris
 * 
 */
public class PixelPrediction extends AbstractProcessor {

	String key = "Data";
	PixelClassifierService classifier;

	/**
	 * @param classifier
	 *            the classifier to set
	 */
	public void setClassifier(PixelClassifierService classifier) {
		this.classifier = classifier;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		float[] image = (float[]) input.get(key);
		if (image != null) {

			int pixels = 1440;
			int roi = image.length / pixels;

			List<Data> series = EventExpander
					.expand(input, pixels, key, 0, roi);

			Map<String, PixelSet> labels = new LinkedHashMap<String, PixelSet>();

			Map<Integer, Serializable> pred = classifier.predict(series);
			for (Integer i : pred.keySet()) {

				String p = pred.get(i).toString();
				PixelSet set = labels.get(p);
				if (set == null) {
					set = new PixelSet();
					labels.put(p, set);
				}

				set.add(new Pixel(i));
			}

			for (String key : labels.keySet()) {
				input.put("@pred:" + key, labels.get(key));
			}
		}

		return input;
	}
}