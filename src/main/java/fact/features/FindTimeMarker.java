package fact.features;

import fact.Constants;
import fact.EventUtils;
import stream.Data;
import stream.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindTimeMarker implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	
	private String key = null;
	private String outputkey = null;
	private String timeOffsetKey = null;
	double[] posRisingEdges = null;
	double[] posFallingEdges = null;
	double[] durations = null;
	double[] maxHeights = null;
	double[] integrals = null;
	double[] averageHeights = null;

	@Override
	public Data process(Data input) {
		double[] data;
		try{
			data = (double[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		double[] timeOffsets;
		try{
			timeOffsets = (double[]) input.get(timeOffsetKey);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		int numberTimeMarker = 160;
		
		posRisingEdges = new double[numberTimeMarker];
		posFallingEdges = new double[numberTimeMarker];
		durations = new double[numberTimeMarker];
		maxHeights = new double[numberTimeMarker];
		integrals = new double[numberTimeMarker];
		averageHeights = new double[numberTimeMarker];
		double[] offsetsRis = new double[numberTimeMarker];
		double[] offsetsFal = new double[numberTimeMarker];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		for(int timemarker = 0 ; timemarker < numberTimeMarker; timemarker++){
			int pos = (9*timemarker + 8) * roi;
			
			int posRisingEdge = 0;
			int posFallingEdge = 0;
			double maxHeight = 0;
			double slope = 0;
			double integral = 0;
			int sl = 1;
			
			sl = roi - 51;
			
			for(; sl < roi && posRisingEdge == 0 ; sl++){
				slope = data[pos+sl] - data[pos+sl-1];
				if (slope > 50){
					posRisingEdge = sl;
				}
			}
			if (posRisingEdge == 0)
			{
				log.warn("Rising Edge not found");
			}
			for(; sl < roi && posFallingEdge == 0 ; sl++){
				slope = data[pos+sl] - data[pos+sl-1];
				integral = integral + data[pos+sl];
				if (maxHeight < data[pos+sl]){
					maxHeight = data[pos+sl];
				}
				if (slope < -50 && data[pos+sl]<150){
					posFallingEdge = sl;
				}
			}
			if (posFallingEdge == 0)
			{
				log.warn("Falling Edge not found");
			}
			posRisingEdges[timemarker] = (double)posRisingEdge;
			posFallingEdges[timemarker] = (double)posFallingEdge;
			if (timeOffsets != null){
				posRisingEdges[timemarker] += timeOffsets[timemarker*roi + posRisingEdge];
				posFallingEdges[timemarker] += timeOffsets[timemarker*roi + posFallingEdge];
				offsetsRis[timemarker] = timeOffsets[timemarker*roi + posRisingEdge];
				offsetsFal[timemarker] = timeOffsets[timemarker*roi + posFallingEdge];
//				log.info(""+offsetsFal[timemarker]);
			}
			durations[timemarker] = posFallingEdge - posRisingEdge;
			maxHeights[timemarker] = maxHeight;
			integrals[timemarker] = integral;
			averageHeights[timemarker] = integral / durations[timemarker];
			
			
		}
		
		input.put(outputkey + "_risingEdges", posRisingEdges);
		input.put(outputkey + "_fallingEdges", posFallingEdges);
		input.put(outputkey + "_durations", durations);
		input.put(outputkey + "_maxHeights", maxHeights);
		input.put(outputkey + "_integrals", integrals);
		input.put(outputkey + "_averageHeights", averageHeights);
		input.put(outputkey + "_offsetRis", offsetsRis);
		input.put(outputkey + "_offsetFal", offsetsFal);
		
		return input;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputkey() {
		return outputkey;
	}

	public void setOutputkey(String outputkey) {
		this.outputkey = outputkey;
	}

	public double[] getPosRisingEdges() {
		return posRisingEdges;
	}

	public void setPosRisingEdges(double[] posRisingEdges) {
		this.posRisingEdges = posRisingEdges;
	}

	public double[] getPosFallingEdges() {
		return posFallingEdges;
	}

	public void setPosFallingEdges(double[] posFallingEdges) {
		this.posFallingEdges = posFallingEdges;
	}

	public double[] getDurations() {
		return durations;
	}

	public void setDurations(double[] durations) {
		this.durations = durations;
	}

	public double[] getMaxHeights() {
		return maxHeights;
	}

	public void setMaxHeights(double[] maxHeights) {
		this.maxHeights = maxHeights;
	}

	public double[] getIntegrals() {
		return integrals;
	}

	public void setIntegrals(double[] integrals) {
		this.integrals = integrals;
	}

	public double[] getAverageHeights() {
		return averageHeights;
	}

	public void setAverageHeights(double[] averageHeights) {
		this.averageHeights = averageHeights;
	}

	public String getTimeOffsetKey() {
		return timeOffsetKey;
	}

	public void setTimeOffsetKey(String timeOffsetKey) {
		this.timeOffsetKey = timeOffsetKey;
	}

}
