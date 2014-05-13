package fact.filter;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RemoveSpikes implements Processor {
	static Logger log = LoggerFactory.getLogger(RemoveSpikes.class);

	@Parameter(required=true)
	String dataKey = null;
	@Parameter(required=true)
	String outputKey = null;
	@Parameter(required=true)
	double spikeLimit;
	@Parameter(required=true)
	double topSlope;
	@Parameter(required=true)
	String outputSpikesKey = null;
	
	String color = null;
	
	int roi;
	
	int leftBorder = 10;
	
	PixelSet singleSpikesSet;
	PixelSet doubleSpikesSet;
	IntervalMarker[] singleSpikes;
	IntervalMarker[] doubleSpikes;

	List<Integer> singleSpikePixel;
	List<Integer> singleSpikeSlice;
	List<Double> singleSpikeHeight;

	List<Integer> doubleSpikePixel;
	List<Integer> doubleSpikeSlice;
	List<Double> doubleSpikeHeight;
	List<Double> doubleSpikeTopSlope;
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(getClass(), input, dataKey);
		
		double[] data = (double[]) input.get(dataKey);
		double[] result = new double[data.length];
		
		System.arraycopy(data, 0, result, 0, data.length);
		
		roi = (Integer) input.get("NROI");
		
		singleSpikes = new IntervalMarker[Constants.NUMBEROFPIXEL];
		doubleSpikes = new IntervalMarker[Constants.NUMBEROFPIXEL];
		singleSpikesSet = new PixelSet();
		doubleSpikesSet = new PixelSet();
		
		singleSpikePixel = new ArrayList<Integer>();
		singleSpikeSlice = new ArrayList<Integer>();
		singleSpikeHeight = new ArrayList<Double>();
		
		doubleSpikePixel = new ArrayList<Integer>();
		doubleSpikeSlice = new ArrayList<Integer>();
		doubleSpikeHeight = new ArrayList<Double>();
		doubleSpikeTopSlope = new ArrayList<Double>();
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			for (int sl = leftBorder ; sl < roi-2 ; sl++)
			{
				int slice = px*roi+sl;
				// Check for a jump up:
				if (result[slice] - result[slice-1] > spikeLimit)
				{
					// Check for a jump down:
					if (result[slice+1] - result[slice] < -spikeLimit)
					{
						// Single Spike
						double spikeHeight = result[slice] - (result[slice-1] + result[slice+1] ) / 2.0; 
						result[slice] = (result[slice-1] + result[slice+1] ) / 2.0;
						singleSpikesSet.add(new Pixel(px));
						singleSpikePixel.add(px);
						singleSpikeSlice.add(sl);
						singleSpikeHeight.add(spikeHeight);
					}
					// Check for a small Step (with a maximum slope of topSlope:
					else if (Math.abs(result[slice+1] - result[slice]) < topSlope)
					{
						// Check for a jump down:
						if (result[slice+2] - result[slice+1] < -spikeLimit)
						{
							// Double Spike
							double spikeHeight = (result[slice] + result[slice+1] ) / 2.0 - (result[slice-1] + result[slice+2] ) / 2.0;
							doubleSpikesSet.add(new Pixel(px));
							doubleSpikePixel.add(px);
							doubleSpikeSlice.add(sl);
							doubleSpikeHeight.add(spikeHeight);
							doubleSpikeTopSlope.add((result[slice+1] - result[slice]));
							result[slice] = (result[slice-1] + result[slice+2] ) / 2.0;
							result[slice+1] = (result[slice-1] + result[slice+2] ) / 2.0;
						}
					}
				}
			}
		}
		
		Object[] sSpikePixel = singleSpikePixel.toArray();
		Object[] sSpikeSlice = singleSpikeSlice.toArray();
		Object[] sSpikeHeight = singleSpikeHeight.toArray();
		
		Object[] dSpikePixel = doubleSpikePixel.toArray();
		Object[] dSpikeSlice = doubleSpikeSlice.toArray();
		Object[] dSpikeHeight = doubleSpikeHeight.toArray();
		Object[] dSpikeTopSlope = doubleSpikeTopSlope.toArray();
			
		input.put(outputSpikesKey + "sPixel",sSpikePixel);
		input.put(outputSpikesKey + "sSlices",sSpikeSlice);
		input.put(outputSpikesKey + "sHeights",sSpikeHeight);
		
		input.put(outputSpikesKey + "dPixel",dSpikePixel);
		input.put(outputSpikesKey + "dSlices",dSpikeSlice);
		input.put(outputSpikesKey + "dHeights",dSpikeHeight);
		input.put(outputSpikesKey + "dTopSlope",dSpikeTopSlope);
		
		input.put(outputSpikesKey + "sSet",singleSpikesSet);
		input.put(outputSpikesKey + "dSet",doubleSpikesSet);
		
		input.put(outputKey,result);
		input.put("@"+Constants.KEY_COLOR + "_" +outputKey,color);
		
		return input;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public double getSpikeLimit() {
		return spikeLimit;
	}

	public void setSpikeLimit(double spikeLimit) {
		this.spikeLimit = spikeLimit;
	}

	public double getTopSlope() {
		return topSlope;
	}

	public void setTopSlope(double topSlope) {
		this.topSlope = topSlope;
	}

	public String getOutputSpikesKey() {
		return outputSpikesKey;
	}

	public void setOutputSpikesKey(String outputSpikesKey) {
		this.outputSpikesKey = outputSpikesKey;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getLeftBorder() {
		return leftBorder;
	}

	public void setLeftBorder(int leftBorder) {
		if (leftBorder < 2)
		{
			throw new RuntimeException("leftBorder smaller than 2: " + leftBorder + " this does not make sense!");
		}
		this.leftBorder = leftBorder;
	}

}
