package fact.datacorrection;

import java.util.ArrayList;
import java.util.List;

import fact.mapping.ui.overlays.PixelSetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RemoveSpikes implements Processor {
	static Logger log = LoggerFactory.getLogger(RemoveSpikes.class);

	@Parameter(required=true)
	String dataKey = null;
	@Parameter(required=true)
	String startCellKey = null;
	@Parameter(required=true)
	String outputKey = null;
	@Parameter(required=true)
	double spikeLimit;
	@Parameter(required=true)
	double topSlopeLimit;
	@Parameter(required=true)
	String outputSpikesKey = null;
	@Parameter(required=true)
	int maxSpikeLength = 2;
	@Parameter(required=true)
	String color = null;
	@Parameter(required=false)
	boolean showSpikes = false;
	
	int roi;
	
	int leftBorder = 10;
	
	double[] result = null;
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(getClass(), input, dataKey, startCellKey);
		
		double[] data = (double[]) input.get(dataKey);
		result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		
		roi = (Integer) input.get("NROI");
		
		short[] startCells = (short[]) input.get(startCellKey);
		
		for (int spikeLength = 1 ; spikeLength <= maxSpikeLength ; spikeLength++)
		{
			
			List<Integer> spPixel = new ArrayList<Integer>();
			List<Integer> spLogSlice = new ArrayList<Integer>();
			List<Integer> spPhysSpike = new ArrayList<Integer>();
			List<Double> spHeight = new ArrayList<Double>();
			List<Double> spTopSlope = new ArrayList<Double>();
			
			PixelSetOverlay spikesSet = new PixelSetOverlay();
			
			for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
			{
				int rightBorder = roi - spikeLength;
				// we want to skip the timemarker signal in the spike removal
				if (px%9 == 8)
				{
					rightBorder = 260;	
				}
				for (int sl = leftBorder ; sl < rightBorder ; sl++)
				{
					int slice = px*roi+sl;
					boolean isSpike = true;
					double averTopSlope = 0;
					double averTopValues = 0;
					// Check for a jump up:
					if (result[slice] - result[slice-1] > spikeLimit)
					{
						averTopValues += result[slice];
						for (int topSlice = 1 ; topSlice < spikeLength && isSpike == true ; topSlice++)
						{
							// Check for small steps (with a maximum slope of topSlope):
							if (Math.abs(result[slice+topSlice] - result[slice+topSlice-1]) >= topSlopeLimit)
							{
								isSpike = false;
							}
							else
							{
								averTopSlope += result[slice+topSlice] - result[slice+topSlice-1];
								averTopValues += result[slice+topSlice];
							}
						}
						if (isSpike == true)
						{
							if (result[slice+spikeLength] - result[slice+spikeLength-1] < -spikeLimit)
							{
								if (spikeLength > 1)
								{
									averTopSlope /= (spikeLength-1);
									averTopValues /= spikeLength;
								}
								double spikeHeight = CorrectSpike(slice, spikeLength, averTopValues);
								spikesSet.addById(px);
								spPixel.add(px);
								spLogSlice.add(sl);
								spPhysSpike.add((sl+startCells[px])%1024);
								spHeight.add(spikeHeight);
								spTopSlope.add(averTopSlope);
							}
						}
					}
				}
			}
						
			
			int[] spPixelArr = new int[spPixel.size()];
			int[] spLogSliceArr = new int[spLogSlice.size()];
			int[] spPhysSliceArr = new int[spPhysSpike.size()];
			double[] spHeightArr = new double[spHeight.size()];
			double[] spTopSlopeArr = new double[spTopSlope.size()];
			for (int i = 0 ; i < spPixel.size() ; i++)
			{
				spPixelArr[i] = spPixel.get(i);
				spLogSliceArr[i] = spLogSlice.get(i);
				spPhysSliceArr[i] = spPhysSpike.get(i);
				spHeightArr[i] = spHeight.get(i);
				spTopSlopeArr[i] = spTopSlope.get(i);
			}
			

			input.put(outputSpikesKey + "N"+spikeLength,spPixelArr.length);
			input.put(outputSpikesKey + "Pixel"+spikeLength,spPixelArr);
			input.put(outputSpikesKey + "LogSlices"+spikeLength,spLogSliceArr);
			input.put(outputSpikesKey + "PhysSlices"+spikeLength,spPhysSliceArr);
			input.put(outputSpikesKey + "Heights"+spikeLength,spHeightArr);
			input.put(outputSpikesKey + "TopSlope"+spikeLength,spTopSlopeArr);

			if (showSpikes == true)
			{			
				input.put(outputSpikesKey + "Set"+spikeLength,spikesSet);
			}
		
		}
		
		input.put(outputKey,result);
		input.put("@"+Constants.KEY_COLOR + "_" +outputKey,color);
		
		return input;
	}

	private double CorrectSpike(int pos, int spikeLength,double averTopValues)
	{
		double spikeHeight = 0;
		
		double averBaseValues = (result[pos-1] + result[pos+spikeLength])/2.0;
		
		spikeHeight = averTopValues - averBaseValues;
		
		for (int sl = 0 ; sl < spikeLength ; sl++)
		{
			result[pos+sl] -= spikeHeight;
		}
		
		return spikeHeight;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getStartCellKey() {
		return startCellKey;
	}

	public void setStartCellKey(String startCellKey) {
		this.startCellKey = startCellKey;
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

	public double getTopSlopeLimit() {
		return topSlopeLimit;
	}

	public void setTopSlopeLimit(double topSlopeLimit) {
		this.topSlopeLimit = topSlopeLimit;
	}

	public String getOutputSpikesKey() {
		return outputSpikesKey;
	}

	public void setOutputSpikesKey(String outputSpikesKey) {
		this.outputSpikesKey = outputSpikesKey;
	}

	public int getMaxSpikeLength() {
		return maxSpikeLength;
	}

	public void setMaxSpikeLength(int maxSpikeLength) {
		this.maxSpikeLength = maxSpikeLength;
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
		this.leftBorder = leftBorder;
	}

	public boolean isShowSpikes() {
		return showSpikes;
	}

	public void setShowSpikes(boolean showSpikes) {
		this.showSpikes = showSpikes;
	}
	
}
