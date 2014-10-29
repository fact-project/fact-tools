package fact.statistics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.StatUtils;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;



/**
 * This processor calculates several features of the timerow in a given timewindow:
 * - mean, std, skewness, kurtosis
 * - median, mode, min, max, quantil 25%, quantil 75%
 * - histogram (over all events) from the difference between the data voltages and the voltages after a moving average filter
 * 
 * @author F. Temme
 *
 */
public class TimerowFeatures implements Processor {
	
	@Parameter(required=true,description="key to the data array")
	private String dataKey = null;
	@Parameter(required=true,description="key to a data array, which was previously filtered using a moving average filter")
	private String movingAverageKey = null;
	@Parameter(required=false,description="left side of the search window for which the features are calculated. 0 < searchWindowLeft < roi", defaultValue="10")
	private int searchWindowLeft = 10;
	@Parameter(required=false,description="right side of the search window for which the features are calculated. 0 < searchWindowLeft < searchWindowRight < roi", defaultValue="250")
	private int searchWindowRight = 250;
	@Parameter(required=true,description="name of the key of the calculated features")
	private String outputKey = null;
	
	
	private int numberOfBins = 200;
	private double histogramMinBin = -10.0;
	private double histogramMaxBin = 10.0;
	private double binWidth = (histogramMaxBin - histogramMinBin)/numberOfBins;
	

	private int[] histogram = new int[numberOfBins+2];
	
	@Override
	public Data process(Data input) {
		
		int roi = (Integer) input.get("NROI");
		
		if ( searchWindowLeft < 0 || searchWindowLeft > searchWindowRight || searchWindowRight > roi)
		{
			throw new RuntimeException("Search window not in the correct region:\n search window: ("
					+ searchWindowLeft + "," + searchWindowRight + ")\t roi: " + roi);
		}
		
		Utils.mapContainsKeys(input, dataKey, movingAverageKey);
		
		double[] data = (double[]) input.get(dataKey);
		double[] movingAverage = (double[]) input.get(movingAverageKey);
		
		
		double[] mean = new double[Constants.NUMBEROFPIXEL];
		double[] median = new double[Constants.NUMBEROFPIXEL];
		double[] mode = new double[Constants.NUMBEROFPIXEL];
		double[] std = new double[Constants.NUMBEROFPIXEL];
		double[] kurtosis = new double[Constants.NUMBEROFPIXEL];
		double[] skewness = new double[Constants.NUMBEROFPIXEL];
		double[] min = new double[Constants.NUMBEROFPIXEL];
		double[] max = new double[Constants.NUMBEROFPIXEL];
		double[] quantil25 = new double[Constants.NUMBEROFPIXEL];
		double[] quantil75 = new double[Constants.NUMBEROFPIXEL];
		
		for (int pix = 0 ; pix < Constants.NUMBEROFPIXEL ; pix++)
		{
			double[] values = new double[searchWindowRight - searchWindowLeft]; 
			for (int sl = searchWindowLeft ; sl < searchWindowRight ; sl++)
			{
				int slice = pix*roi + sl;
				values[sl-searchWindowLeft] = data[slice];
				int binNumber = findBinNumber((data[slice]-movingAverage[slice]));
				histogram[binNumber] += 1;
			}
			DescriptiveStatistics stats = new DescriptiveStatistics(values);
			mean[pix] = stats.getMean();
			min[pix] = stats.getMin();
			max[pix] = stats.getMax();
			std[pix] = stats.getStandardDeviation();
			skewness[pix] = stats.getSkewness();
			kurtosis[pix] = stats.getKurtosis();
			
			Percentile percentile = new Percentile();
			median[pix] = percentile.evaluate(values,0.5);
			quantil25[pix] = percentile.evaluate(values, 0.25);
			quantil75[pix] = percentile.evaluate(values, 0.75);
			
			double[] modeArray = StatUtils.mode(values);
			mode[pix] = modeArray[0];
		}
		
		input.put(outputKey+ "_mean", mean);
		input.put(outputKey+ "_median", median);
		input.put(outputKey+ "_mode", mode);
		input.put(outputKey+ "_std", std);
		input.put(outputKey+ "_kurtosis", kurtosis);
		input.put(outputKey+ "_skewness", skewness);
		input.put(outputKey+ "_min", min);
		input.put(outputKey+ "_max", max);
		input.put(outputKey+ "_quantil25", quantil25);
		input.put(outputKey+ "_quantil75", quantil75);
		input.put(outputKey+ "_histogram",histogram);
		
		
		return input;
	}
	
	private int findBinNumber(double value){
		if (value < histogramMinBin)
		{
			return 0;
		}
		if (value >= histogramMaxBin)
		{
			return numberOfBins+1;
		}
		int binNumber = (int) ( (value - histogramMinBin) / binWidth);
		return binNumber;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getMovingAverageKey() {
		return movingAverageKey;
	}

	public void setMovingAverageKey(String movingAverageKey) {
		this.movingAverageKey = movingAverageKey;
	}

	public int getSearchWindowLeft() {
		return searchWindowLeft;
	}

	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}

	public int getSearchWindowRight() {
		return searchWindowRight;
	}

	public void setSearchWindowRight(int searchWindowRight) {
		this.searchWindowRight = searchWindowRight;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getNumberOfBins() {
		return numberOfBins;
	}

	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}

	public double getHistogramMinBin() {
		return histogramMinBin;
	}

	public void setHistogramMinBin(double histogramMinBin) {
		this.histogramMinBin = histogramMinBin;
	}

	public double getHistogramMaxBin() {
		return histogramMaxBin;
	}

	public void setHistogramMaxBin(double histogramMaxBin) {
		this.histogramMaxBin = histogramMaxBin;
	}

}