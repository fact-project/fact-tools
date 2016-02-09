package fact.datacorrection;

import fact.Utils;
import fact.container.SpikeInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Tries to remove the artefact called 'Spike' from the data for different spike lengths. We know that there exist
 *  spikes with length 4.
 *
 *  @author Fabian Temme
 */
public class RemoveSpikes implements Processor {
	static Logger log = LoggerFactory.getLogger(RemoveSpikes.class);

	@Parameter(required=true, defaultValue = "raw:dataCalibrated", description = "calibrated Data array")
	String dataKey 		= "raw:dataCalibrated";
	@Parameter(required=true, defaultValue = "meta:startCellData", description = "key for start cell information")
	String startCellKey = "meta:startCellData";
	@Parameter(required=true, defaultValue = "raw:dataCalibrated", description = "Ouputkey for corrected Data array")
	String outputKey 	= "raw:dataCalibrated";
	@Parameter(required=false, defaultValue = "20")
	double spikeLimit 	= 20;
    @Parameter(required=false, defaultValue = "4")
	int maxSpikeLength 	= 4;
    @Parameter(required=false, defaultValue = "6")
    int leftBorder 		= 6;
    @Parameter(required=false, defaultValue = "16")
    double topSlopeLimit 	= 16;
    @Parameter(required=false, defaultValue = "false")
	boolean addSpikeInfo    = false;
    @Parameter(required=false,description = "useful for spike studies")
    String outputSpikesKey 	= null;

	int roi;
	int npix;

	@Override
	public Data process(Data item) {
		Utils.isKeyValid(item, dataKey, double[].class);
		Utils.isKeyValid(item, startCellKey, short[].class);
		Utils.isKeyValid(item, "NROI", Integer.class);
		Utils.isKeyValid(item, "NPIX", Integer.class);
		
		double[] data = (double[]) item.get(dataKey);
		double[] result = new double[data.length];
		System.arraycopy(data, 0, result, 0, data.length);
		roi = (Integer) item.get("NROI");
		npix = (Integer) item.get("NPIX");
		short[] startCells = (short[]) item.get(startCellKey);
		
		for (int spikeLength = 1 ; spikeLength <= maxSpikeLength ; spikeLength++)
		{
			SpikeInfos spikeInfos = null;
			if (addSpikeInfo)
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
						for (int topSlice = 1 ; topSlice < spikeLength && isSpike ; topSlice++)
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
						if (isSpike)
						{
							if (result[slice+spikeLength] - result[slice+spikeLength-1] < -spikeLimit)
							{
								if (spikeLength > 1)
								{
									averTopSlope /= (spikeLength-1);
									averTopValues /= spikeLength;
								}
								double spikeHeight = CorrectSpike(slice, spikeLength, averTopValues,result);
								if (addSpikeInfo)
								{
									spikeInfos.addSpike(px,sl,startCells[px],spikeHeight,averTopSlope);
								}
							}
						}
					}
				}
			}
			
			if (addSpikeInfo)
			{
				spikeInfos.addInfosToDataItem(item,spikeLength,outputSpikesKey);
			}
				
		}
		
		item.put(outputKey,result);

		return item;
	}

	private double CorrectSpike(int pos, int spikeLength,double averTopValues, double[] result)
	{
		double spikeHeight;
		
		double averBaseValues = (result[pos-1] + result[pos+spikeLength])/2.0;
		
		spikeHeight = averTopValues - averBaseValues;
		
		for (int sl = 0 ; sl < spikeLength ; sl++)
		{
			result[pos+sl] -= spikeHeight;
		}
		
		return spikeHeight;
	}
}
