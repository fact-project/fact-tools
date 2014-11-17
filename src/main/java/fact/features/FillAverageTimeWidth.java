package fact.features;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

public class FillAverageTimeWidth implements Processor {
	static Logger log = LoggerFactory.getLogger(FillAverageTimeWidth.class);
	
	private int numberTimeMarker = 160;
	private int numberOfSlices = 1024;
	
	private String key = null;
	private String outputKeyOffset = "timeOffset";
	private String outputKeyAverageWidth = "timeAverageWidth";
	private double[] averageTimeWidth = new double[numberTimeMarker*numberOfSlices];
	private double[] weights = new double[numberTimeMarker*numberOfSlices];
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys( input, key, "StartCellData");
		double[] data;
		try{
			data = (double[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		if (data==null){
			log.error("Couldn't get key: " + key);
		}
		short[] startCell = (short[]) input.get("StartCellData");
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		for(int timemarker=0 ; timemarker < numberTimeMarker ; timemarker++){
			int pos = (9*timemarker + 8) * roi;
			
			short current_start_cell = startCell[9*timemarker + 8];
			
			double last_zero_crossing = -1;
			double last_weight = 0;
			
			for(int sl=0 ; sl < roi - 1 ; sl++){
				// Search for zero crossing on rising edges:
				// To do, make sure, that this is really a zero crossing of our signal, not a small fluctuation,
				// maybe we check the calculated length, or use a filter 
				if (data[pos+sl] < 0 && data[pos+sl+1] > 0){
					// calculate zero crossing in the interval [pos+sl,pos+sl+1] relativ to pos+sl, by linear interpolation
					// this is also the weight which we use for the bin in which the zero crossing is happening:
					double weight = data[pos+sl] / (data[pos+sl] - data[pos+sl+1]);
					if (last_zero_crossing >= 0){
						double length = (double) sl + weight - last_zero_crossing;
//						System.out.println(length);
						// now update the averageTimeWidthArray for the bins between the last zero crossing
						// and the current zero crossing.
						// The first and the last bin of this interval are only filled according to the
						// calculated weights of the last zero crossing and the current zero crossing.
						int j = (int)Math.floor(last_zero_crossing);
						int cell = timemarker*roi+(j+current_start_cell)%1024;
						averageTimeWidth[cell] *= weights[cell];
						averageTimeWidth[cell] += (1-last_weight) * length;
						weights[cell] += (1-last_weight);
						averageTimeWidth[cell] /= weights[cell];
						j++;
						for (; j < sl ; j++){
							cell = timemarker*roi+(j+current_start_cell)%1024;
							averageTimeWidth[cell] *= weights[cell];
							averageTimeWidth[cell] += length;
							weights[cell] += 1;
							averageTimeWidth[cell] /= weights[cell];
						}
						cell = timemarker*roi+(sl+current_start_cell)%1024;
						averageTimeWidth[cell] *= weights[cell];
						averageTimeWidth[cell] += weight * length;
						weights[cell] += weight;
						averageTimeWidth[cell] /= weights[cell];
					}
					last_zero_crossing = (double) sl + weight;
					last_weight = weight;
				}
			}
		}
		double[] wholeAverageTimeWidth = new double[numberTimeMarker];
		for(int timemarker=0 ; timemarker < numberTimeMarker ; timemarker++){
			for (int sl = 0 ; sl < numberOfSlices ; sl++){
				wholeAverageTimeWidth[timemarker] += averageTimeWidth[timemarker*roi+sl];
			}
			wholeAverageTimeWidth[timemarker] /= numberOfSlices;
		}
		double[] timeOffsets = new double[numberTimeMarker*numberOfSlices];
		for(int timemarker=0 ; timemarker < numberTimeMarker ; timemarker++){
			timeOffsets[timemarker*roi] = averageTimeWidth[timemarker*roi] / wholeAverageTimeWidth[timemarker] - 1;
			for (int sl = 1 ; sl < numberOfSlices ; sl++){
				timeOffsets[timemarker*roi+sl] = timeOffsets[timemarker*roi+sl-1] 
						+ averageTimeWidth[timemarker*roi+sl] / wholeAverageTimeWidth[timemarker] - 1;
			}
		}
//		for (int sl = 0 ; sl < numberOfSlices ; sl++){
//			System.out.println(timeOffsets[sl]);
//		}
		input.put(outputKeyOffset, timeOffsets);
		input.put(outputKeyAverageWidth, wholeAverageTimeWidth);
		
		return input;
	}
	
	public int getNumberTimeMarker() {
		return numberTimeMarker;
	}
	public void setNumberTimeMarker(int numberTimeMarker) {
		this.numberTimeMarker = numberTimeMarker;
	}
	public int getNumberOfSlices() {
		return numberOfSlices;
	}
	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getOutputKeyOffset() {
		return outputKeyOffset;
	}
	public void setOutputKeyOffset(String outputKeyOffset) {
		this.outputKeyOffset = outputKeyOffset;
	}
	public String getOutputKeyAverageWidth() {
		return outputKeyAverageWidth;
	}

	public void setOutputKeyAverageWidth(String outputKeyAverageWidth) {
		this.outputKeyAverageWidth = outputKeyAverageWidth;
	}
	

}
