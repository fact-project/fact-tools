package fact.features;

import stream.Data;

import stream.Processor;
public class ArrivalTimeDistribution implements Processor {

	private String showerPixelKey;
	private String outputKey;
	private String arrivalTimeKey
	;
	
	@Override
	public Data process(Data input) {
		
		double[] arrivalTimes = (double[]) input.get(arrivalTimeKey);
		int[] showerPixel = (int[]) input.get(showerPixelKey);
		
		double[] showerArrivalTime = new double[showerPixel.length];
		
		double meanArrivalTime = 0;
		double rmsArrivalTime = 0;
		double stdDevArrivalTime = 0;
		for(int i=0; i<showerPixel.length; i++){
			showerArrivalTime[i] = arrivalTimes[showerPixel[i]];
			meanArrivalTime += showerArrivalTime[i];
			rmsArrivalTime += Math.pow(showerArrivalTime[i], 2);
			
		}
		
		meanArrivalTime /= showerArrivalTime.length;
		rmsArrivalTime = Math.sqrt(rmsArrivalTime/showerArrivalTime.length);
		
		for(int i=0; i<showerPixel.length; i++){
			showerArrivalTime[i] -= meanArrivalTime;
			stdDevArrivalTime += Math.pow(showerArrivalTime[i] - meanArrivalTime, 2);
		}
		
		stdDevArrivalTime = Math.sqrt(1/(showerArrivalTime.length -1) * stdDevArrivalTime);
		
		input.put(outputKey+"Mean", meanArrivalTime);
		input.put(outputKey+"Rms", meanArrivalTime);
		input.put(outputKey+"StdDev", meanArrivalTime);
		
		return input;
	}

	public String getShowerPixelKey() {
		return showerPixelKey;
	}

	public void setShowerPixelKey(String showerPixelKey) {
		this.showerPixelKey = showerPixelKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputkey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getArrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

}
