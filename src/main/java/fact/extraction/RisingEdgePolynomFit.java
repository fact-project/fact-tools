package fact.extraction;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jfree.chart.plot.IntervalMarker;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;

public class RisingEdgePolynomFit implements Processor {
	
	private String risingEdgeKey = null;
	
	private String dataKey = null;
	
	private int range = 5;
	
	private String outputKey = null;
	
	private String maxSlopesKey = null;

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, dataKey,risingEdgeKey,"NROI");
		
		double[] maxDerivations = new double[Constants.NUMBEROFPIXEL];
		double[] maxDerivationsPositions = new double[Constants.NUMBEROFPIXEL];
		
		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		
		double[] data = (double[]) input.get(dataKey);
		int roi = (Integer) input.get("NROI");
		
		double[] buffer = (double[]) input.get(risingEdgeKey);
		int[] risingEdges = new int[buffer.length];
		for (int i = 0 ; i < buffer.length ; i++)
		{
			risingEdges[i] = (int) buffer[i];
		}
		
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
		
		for (int pix = 0 ; pix < Constants.NUMBEROFPIXEL ; pix++)
		{
			int pos = risingEdges[pix];
			WeightedObservedPoints observations = new WeightedObservedPoints();
			for (int sl=pos-range ; sl < pos+range+1 ; sl++)
			{
				int slice = pix*roi + sl;
				observations.add(sl,data[slice]);
			}
			double[] coeff = fitter.fit(observations.toList());
			double[] maxDerivation = calcMaxDerivation(coeff);
			maxDerivationsPositions[pix] = maxDerivation[0];
			m[pix] = new IntervalMarker(maxDerivationsPositions[pix],maxDerivationsPositions[pix] + 1);
			maxDerivations[pix] = maxDerivation[1];
		}
		input.put(outputKey, maxDerivationsPositions);
		input.put(maxSlopesKey, maxDerivations);
		input.put(outputKey + "Marker", m);
		
		return input;
	}

	// ax^3 + bx^2 + cx + d
	// d: c[0] ; c:c[1] ; b:c[2] ; a:c[3]
	private double[] calcMaxDerivation(double[] c) {
		double[] result = new double[2];
		result[0] = -c[2]/(3*c[3]);
		result[1] = -c[2]*c[2]/(3*c[3])+c[1];
		return result;
	}

	public String getRisingEdgeKey() {
		return risingEdgeKey;
	}

	public void setRisingEdgeKey(String risingEdgeKey) {
		this.risingEdgeKey = risingEdgeKey;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getMaxSlopesKey() {
		return maxSlopesKey;
	}

	public void setMaxSlopesKey(String maxSlopesKey) {
		this.maxSlopesKey = maxSlopesKey;
	}



}
