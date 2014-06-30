package fact.extraction;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class HandleSaturation implements Processor {

	@Parameter(required=true)
	private String dataKey = null;
	@Parameter(required=true)
	private String maxAmplitudesKey = null;
	@Parameter(required=true)
	private String photonChargeKey = null;
	@Parameter(required=true)
	private String timeOverThresholdChargeKey = null;
	@Parameter(required=true)
	private String arrivalTimeKey = null;
	@Parameter(required=true)
	private int limitForSaturatedPixel;
	@Parameter(required=true)
	private int leftBorder;
	@Parameter(required=true)
	private String outputKeyPhotonCharge = null;
	@Parameter(required=true)
	private String outputKeyArrivalTime = null;
	
	private int roi;
	private double[] data;
			
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(getClass(), input ,dataKey,maxAmplitudesKey,photonChargeKey,timeOverThresholdChargeKey,arrivalTimeKey);
		
		double[] maxAmplitudes = (double[]) input.get(maxAmplitudesKey);
		double[] photonCharge = (double[]) input.get(photonChargeKey);
		double[] timeOverThresholdCharge = (double[]) input.get(timeOverThresholdChargeKey);
		double[] arrivalTimes = (double[]) input.get(arrivalTimeKey);
		
		roi = (Integer) input.get("NROI");
		data = (double[]) input.get(dataKey);
		
		double[] resultPhotonCharge = new double[photonCharge.length];
		System.arraycopy(photonCharge, 0, resultPhotonCharge, 0, photonCharge.length);
		double[] resultArrivalTimes = new double[arrivalTimes.length];
		System.arraycopy(arrivalTimes, 0, resultArrivalTimes, 0, arrivalTimes.length);
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			if (maxAmplitudes[px] > limitForSaturatedPixel)
			{
				resultArrivalTimes[px] = searchForRisingEdge(px, arrivalTimes[px]);
				resultPhotonCharge[px] = timeOverThresholdCharge[px];
			}
		}
		
		input.put(outputKeyArrivalTime, resultArrivalTimes);
		input.put(outputKeyPhotonCharge, resultPhotonCharge);
		
		// TODO Auto-generated method stub
		return input;
	}

	private int searchForRisingEdge(int pixel,double oldMaxPos)
	{		
		double tempMaxDeriv = Double.MIN_VALUE;
		int tempPos = -1;
		for (int sl = leftBorder ; sl < oldMaxPos ; sl++)
		{
			int slice = pixel*roi+sl;
			if ((data[slice+1] - data[slice]) > tempMaxDeriv)
			{
				tempPos = sl;
				tempMaxDeriv = data[slice+1] - data[slice];
			}
		}
		
		return tempPos;
	}
	
}
