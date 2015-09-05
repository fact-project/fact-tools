package fact.extraction;

import fact.Utils;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class RisingEdgePolynomFit implements Processor {
	
	@Parameter(required=true, description="Key to the position of the rising edges")
	private String risingEdgeKey = null;
	@Parameter(required=true, description="Key to the data array")	
	private String dataKey = null;
	@Parameter(required=true, description="outputKey for the calculated arrival time")
	private String outputKey = null;
	@Parameter(required=true, description="outputKey for the calculated slope at the arrival time")
	private String maxSlopesKey = null;
	
	@Parameter(required=false, description="number of points used for the fit", defaultValue="11")
	private int numberOfPoints = 11;
	
	private int npix;

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
		Utils.mapContainsKeys(input, dataKey,risingEdgeKey,"NROI");
		
		double[] arrivalTimes = new double[npix];
		double[] maxSlopes = new double[npix];
		IntervalMarker[] m = new IntervalMarker[npix];
		
		double[] data = (double[]) input.get(dataKey);
		int roi = (Integer) input.get("NROI");
		
		double[] buffer = (double[]) input.get(risingEdgeKey);
		int[] risingEdges = new int[buffer.length];
		for (int i = 0 ; i < buffer.length ; i++)
		{
			risingEdges[i] = (int) buffer[i];
		}
		
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
				
		for (int pix = 0 ; pix < npix ; pix++)
		{
			int pos = risingEdges[pix];
			int[] window = Utils.getValidWindow(pos-numberOfPoints/2, numberOfPoints, 0, roi);
			WeightedObservedPoints observations = new WeightedObservedPoints();
			for (int sl = window[0] ; sl < window[1] ; sl++)
			{
				int slice = pix*roi + sl;
				observations.add(sl,data[slice]);
			}
			double[] coeff = fitter.fit(observations.toList());
			double[] maxDerivation = calcMaxDerivation(coeff);
			arrivalTimes[pix] = maxDerivation[0];
			maxSlopes[pix] = maxDerivation[1];
			if (maxDerivation[0] < window[0])
			{
				arrivalTimes[pix] = (double) window[0];
				maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix],coeff);
			}
			else if (maxDerivation[0] > window[1])
			{
				arrivalTimes[pix] = (double) window[1];
				maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix],coeff);
			}
			
			m[pix] = new IntervalMarker(arrivalTimes[pix],arrivalTimes[pix] + 1);
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

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public void setNumberOfPoints(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
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
