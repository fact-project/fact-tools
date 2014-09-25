package fact.datacorrection;

import fact.Utils;
import fact.container.SpikeInfos;
import fact.hexmap.ui.overlays.PixelSetOverlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tries to remove the artefact called 'Spike' from the data for different spike lengths. We know that there exist
 *  spikes with length 4.
 *
 *  @author Fabian Temme
 */
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
	@Parameter(required=false,description = "useful for spike studies")
	String outputSpikesKey = null;
	@Parameter(required=true, defaultValue = "2")
	int maxSpikeLength = 2;
	@Parameter(required=false)
	boolean addSpikeInfo = false;
	
	int roi;
	int npix;
	
	int leftBorder = 10;
	
	
	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, dataKey, double[].class);
		Utils.isKeyValid(input, startCellKey, short[].class);
		Utils.isKeyValid(input, "NROI", Integer.class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
		
		double[] data = (double[]) input.get(dataKey);
		double[] result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		roi = (Integer) input.get("NROI");
		npix = (Integer) input.get("NPIX");
		short[] startCells = (short[]) input.get(startCellKey);
		
		for (int spikeLength = 1 ; spikeLength <= maxSpikeLength ; spikeLength++)
		{
			SpikeInfos spikeInfos = null;
			if (addSpikeInfo == true)
			{
				spikeInfos = new SpikeInfos();
			}
			
			for (int px = 0 ; px < npix ; px++)
			{
				int rightBorder = roi - spikeLength;
				// we want to skip the timemarker signal (which only occur in files with roi == 300) in the spike removal
				if (px%9 == 8 && roi == 300)
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
								double spikeHeight = CorrectSpike(slice, spikeLength, averTopValues,result);
								if (addSpikeInfo == true)
								{
									spikeInfos.addSpike(px,sl,startCells[px],spikeHeight,averTopSlope);
								}
							}
						}
					}
				}
			}
			
			if (addSpikeInfo == true)
			{
				spikeInfos.addInfosToDataItem(input,spikeLength,outputSpikesKey);
			}
				
		}
		
		input.put(outputKey,result);

		return input;
	}

	private double CorrectSpike(int pos, int spikeLength,double averTopValues, double[] result)
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

	public int getLeftBorder() {
		return leftBorder;
	}

	public void setLeftBorder(int leftBorder) {
		this.leftBorder = leftBorder;
	}

	public boolean isAddSpikeInfo() {
		return addSpikeInfo;
	}

	public void setAddSpikeInfo(boolean addSpikeInfo) {
		this.addSpikeInfo = addSpikeInfo;
	}

	
}
