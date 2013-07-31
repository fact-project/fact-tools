package fact.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.utils.SimpleFactEventProcessor;

import stream.ProcessContext;


/**
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class CreateHistogram extends SimpleFactEventProcessor<float[],int[]> {
	static Logger log = LoggerFactory.getLogger(CreateHistogram.class);
	private int numberOfBins = 12;
	private float max = 500;
	private float min = 0;
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
			if(f < min){
				//thius goes into the underflow bin
				index = 0;
			} else if( f > max){
				//this goes into the overflow bin
				index = numberOfBins + 1;
			} else {
				index= (int)( (f/max)*numberOfBins + 1);
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
	public float getMax() {
		return max;
	}
	public void setMax(float maxBin) {
		this.max = maxBin;
	}
	public float getMin() {
		return min;
	}
	public void setMin(float minbin) {
		this.min = minbin;
	}

	
	/*
	 * Getter and Setter
	 */

}
