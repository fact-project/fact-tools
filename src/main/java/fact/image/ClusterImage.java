/**
 * 
 */
package fact.image;

import java.util.Collection;
import java.util.List;

import stream.Processor;
import stream.clustering.KMeans;
import stream.clustering.KMeans.Cluster;
import stream.Data;
import fact.data.EventExpander;
import fact.image.overlays.PixelSet;

/**
 * @author chris
 * 
 */
public class ClusterImage implements Processor {

	String key = "Data";
	int k = 3;
	int starts = 8;
	int rounds = 4;

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @param k
	 *            the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}

	/**
	 * @return the starts
	 */
	public int getStarts() {
		return starts;
	}

	/**
	 * @param starts
	 *            the starts to set
	 */
	public void setStarts(int starts) {
		this.starts = starts;
	}

	/**
	 * @return the rounds
	 */
	public int getRounds() {
		return rounds;
	}

	/**
	 * @param rounds
	 *            the rounds to set
	 */
	public void setRounds(int rounds) {
		this.rounds = rounds;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		float[] image = (float[]) input.get(key);
		if (image == null)
			return input;

		int numberOfPixels = 1440;
		int roi = (int) image.length / numberOfPixels;

		List<Data> items = EventExpander.expand(input, numberOfPixels, key, 0,
				roi);

		/*
		 * ZNormalization norm = new ZNormalization(); items =
		 * norm.normalize(items);
		 */

		KMeans kmeans = new KMeans();
		kmeans.setRounds(starts);
		kmeans.setOptimizations(rounds);
		Collection<Cluster> clustering = kmeans.cluster(items, k);

		for (Cluster c : clustering) {
			PixelSet pixels = new PixelSet();
			for (Data pix : c) {
				Integer pid = new Integer("" + pix.get("@pixel"));
				pixels.add(new Pixel(pid));
			}

			input.put("@cluster:" + c.getId(), pixels);
		}

		return input;
	}
}
