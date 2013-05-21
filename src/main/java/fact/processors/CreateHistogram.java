package fact.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.ProcessContext;


/**
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class CreateHistogram extends SimpleFactEventProcessor<float[],int[]> {
	static Logger log = LoggerFactory.getLogger(CreateHistogram.class);
	private int numberOfBins = 12;
	private float maxBin = 500;
	private float minbin = 0;
	int[] bin;
	
	@Override
	public void init(ProcessContext context){
		//number of buckets and over- and underflow bins
		bin = new int[numberOfBins + 2];
		
	}
	@Override
	public int[] processSeries(float[] data) {
		for(float f: data){
			int index = 0;
			if(f < minbin){
				//thius goes into the underflow bin
				index = 0;
			} else if( f > maxBin){
				//this goes into the overflow bin
				index = numberOfBins + 1;
			} else {
				index= (int)( (f/maxBin)*numberOfBins + 1);
			}
			bin[index]++;
		}
		return bin;
	}
	public int getNumberOfBins() {
		return numberOfBins;
	}
	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}
	public float getMaxBin() {
		return maxBin;
	}
	public void setMaxBin(float maxBin) {
		this.maxBin = maxBin;
	}
	public float getMinbin() {
		return minbin;
	}
	public void setMinbin(float minbin) {
		this.minbin = minbin;
	}

	
	/*
	 * Getter and Setter
	 */

}
