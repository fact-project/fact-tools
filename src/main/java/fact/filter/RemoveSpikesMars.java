/**
 * 
 */
package fact.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

/**
 * Supposedly removes all spikes in the data.
 * Original algorithm by F.Temme. 
 * Takes a float array and creates a float array as output
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class RemoveSpikesMars extends SimpleFactEventProcessor<float[], float[]> {
	static Logger log = LoggerFactory.getLogger(RemoveSpikesMars.class);
	@Override
	public float[] processSeries(float[] data) {
		int roi = data.length / Constants.NUMBEROFPIXEL;
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 1; slice < roi-3; slice++) {
				int sl = pix * roi + slice;
				// check if it is a one slice jump up
				if (data[sl] - data[sl-1] > 25)
				{
					// check if immediately a one slice jump down follows
					// ==> Single Spike
					if(data[sl+1] - data[sl] < -25)
					{
						data[sl]     = ( data[sl-1] + data[sl+1] ) / 2;
					}
				}
				// check if it is a one slice jump up
				if (data[sl] - data[sl-1] > 22)
				{
					// check if immediately a small step follows
					if (Math.abs((data[sl+1] - data[sl])) < 4 )
					{
						// check if then a one slice jump down follows
						// ==> Double Spike
						if (data[sl+2] - data[sl+1] < -22)
						{
							data[sl] = ( data[sl-1] + data[sl+2] ) / 2;
							data[sl+1] = data[sl];
						}
					}
				}

			}
		}
		return data;
	}
}
