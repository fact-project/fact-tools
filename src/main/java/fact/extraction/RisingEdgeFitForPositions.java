package fact.extraction;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RisingEdgeFitForPositions implements Processor {
	static Logger log = LoggerFactory.getLogger(RisingEdgeFitForPositions.class);
	@Parameter(required=true, description="key to the data array")
	private String dataKey = null;
	@Parameter(required=true, description="key to the position array")
	private String positionsKey = null;
	@Parameter(required=true, description="outputKey for the calculated arrival time")
	private String outputKey = null;
	@Parameter(required=true, description="outputKey for the calculated slope at the arrival time")
	private String maxSlopesKey=null;
	@Parameter(required=false, description="number of points in front of the position which are used to fit the rising edge",defaultValue = "11")
	private int numberOfPoints = 11;

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, dataKey,positionsKey,"NROI");
		
		double[] data = (double[]) input.get(dataKey);
		int[] positions = (int[]) input.get(positionsKey);
		
		int roi = (Integer) input.get("NROI");
		
		double[] arrivalTimes = new double[Constants.NUMBEROFPIXEL];
		double[] maxSlopes = new double[Constants.NUMBEROFPIXEL];
		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			WeightedObservedPoints observations = new WeightedObservedPoints();
			int[] window = Utils.getValidWindow(positions[px]-1-numberOfPoints, numberOfPoints, 0, roi);
			for (int slice = window[0] ; slice <= window[1] ; slice++)
			{
				int pos = px * roi + slice;
				observations.add(slice, data[pos]);
			}
			double[] coeff = fitter.fit(observations.toList());
			double[] maxDerivation = calcMaxDerivation(coeff);
			arrivalTimes[px] = maxDerivation[0];
			maxSlopes[px] = maxDerivation[1];
			if (maxDerivation[0] < window[0])
			{
				arrivalTimes[px] = (double) window[0];
				maxSlopes[px] = calcDerivationAtPoint(arrivalTimes[px],coeff);
			}
			else if (maxDerivation[0] > window[1])
			{
				arrivalTimes[px] = (double) window[1];
				maxSlopes[px] = calcDerivationAtPoint(arrivalTimes[px],coeff);
			}
			m[px] = new IntervalMarker(arrivalTimes[px],arrivalTimes[px] + 1);
		}
		input.put(outputKey, arrivalTimes);
		input.put(maxSlopesKey, maxSlopes);
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
	
	// ax^3 + bx^2 + cx + d
	// d: c[0] ; c:c[1] ; b:c[2] ; a:c[3]
	private double calcDerivationAtPoint(double x, double[] c) {
		double result = 0;
		result = c[3]*x*x + c[2]*x + c[1];
		return result;
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getPositionsKey() {
		return positionsKey;
	}

	public void setPositionsKey(String positionsKey) {
		this.positionsKey = positionsKey;
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

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public void setNumberOfPoints(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

}
