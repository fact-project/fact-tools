package fact.extraction;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class MaxAmplitudeInSearchWindow implements Processor {

	@Parameter(required=true)
	private String dataKey = null;
	@Parameter(required=true)
	private String outputKeyAmplitudes = null;
	@Parameter(required=true)
	private String outputKeyPositions = null;
	@Parameter(required=true)
	private int searchWindowLeft;
	@Parameter(required=true)
	private int searchWindowRight;
	
	private int npix;
	int roi;
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.isKeyValid(input, "NPIX", Integer.class);
		npix = (Integer) input.get("NPIX");
		
		double[] amplitudes = new double[npix];
		int[] positions = new int[npix];
		
		if (searchWindowLeft >= searchWindowRight)
		{
			throw new RuntimeException("searchWindowLeft is equal or larger than searchWindowRight: "+searchWindowLeft+" >= "+searchWindowRight);
		}
		
		Utils.mapContainsKeys(input, dataKey);
		
		double[] data = (double[]) input.get(dataKey);
		
		roi = (Integer) input.get("NROI");
		
		for (int px = 0 ; px < npix ; px++)
		{
			int tempPos = -1;
			double tempMaxValue = Double.MIN_VALUE;
			for (int sl = searchWindowLeft ; sl < searchWindowRight ; sl++)
			{
				int slice = px * roi + sl;
				if (data[slice] > tempMaxValue)
				{
					tempPos = sl;
					tempMaxValue = data[slice];
				}
			}
			positions[px] = tempPos;
			amplitudes[px] = tempMaxValue;
		}
		
		input.put(outputKeyAmplitudes, amplitudes);
		input.put(outputKeyPositions, positions);
		
		return input;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKeyAmplitudes() {
		return outputKeyAmplitudes;
	}

	public void setOutputKeyAmplitudes(String outputKeyAmplitudes) {
		this.outputKeyAmplitudes = outputKeyAmplitudes;
	}

	public String getOutputKeyPositions() {
		return outputKeyPositions;
	}

	public void setOutputKeyPositions(String outputKeyPositions) {
		this.outputKeyPositions = outputKeyPositions;
	}

	public int getSearchWindowLeft() {
		return searchWindowLeft;
	}

	public void setSearchWindowLeft(int searchWindowLeft) {
		if (searchWindowLeft < 0 || searchWindowLeft > 299)
		{
			throw new RuntimeException("searchWindowLeft is not in the ROI: "+ searchWindowLeft);
		}
		this.searchWindowLeft = searchWindowLeft;
	}

	public int getSearchWindowRight() {
		return searchWindowRight;
	}

	public void setSearchWindowRight(int searchWindowRight) {
		if (searchWindowRight < 0 || searchWindowRight > 299)
		{
			throw new RuntimeException("searchWindowRight is not in the ROI: "+ searchWindowRight);
		}
		this.searchWindowRight = searchWindowRight;
	}

}
