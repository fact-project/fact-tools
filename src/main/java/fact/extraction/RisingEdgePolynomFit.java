package fact.extraction;

import org.apache.commons.math3.linear.LUDecomposition;
import fact.Utils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
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

    @Parameter(required=false, description="degree for the polynomial to fit", defaultValue="3" )
	private int fit_degree = 3;
	
	private int npix;

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
		Utils.mapContainsKeys(input, dataKey,risingEdgeKey,"NROI");
		
		double[] arrivalTimes = new double[npix];
		double[] maxSlopes = new double[npix];
		IntervalMarker[] marker = new IntervalMarker[npix];
		
		double[] data = (double[]) input.get(dataKey);
		int roi = (Integer) input.get("NROI");
		
		double[] risingEdges = (double[]) input.get(risingEdgeKey);

		for (int pix = 0 ; pix < npix ; pix++)
		{
			int pos = (int) risingEdges[pix];
			int[] window = Utils.getValidWindow(pos-numberOfPoints/2, numberOfPoints, 0, roi);

			// We do a linear least squares fit of a0 + a1*x + a2*x^2 + a3*x^3
			// Aij = fi(xj)
			// => a = (A^T * A)^(-1) * A^T * y
			int n_points = window[1] - window[0];
			double[][] arrA = new double[n_points][fit_degree + 1];
			double[] arrY = new double[n_points];

			for (int i=0 ; i < n_points ; i++)
			{
				int x = i + window[0];
				int slice = pix * roi + x;
				for (int j=0; j <= fit_degree; j++)
				{
					arrA[i][j] = Math.pow(x, j);
					arrY[i] = data[slice];
				}
			}

			RealVector y = new ArrayRealVector(arrY);
			RealMatrix A = new Array2DRowRealMatrix(arrA);
			RealMatrix AT = A.transpose();
			RealMatrix ATA = AT.multiply(A);
			RealMatrix invATA = new LUDecomposition(ATA).getSolver().getInverse();
			RealVector a = invATA.multiply(AT).operate(y);

            double[] c = a.toArray();

            arrivalTimes[pix] = calcXPosMaxDerivation(c);
            maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);

			if (arrivalTimes[pix] < window[0])
			{
				arrivalTimes[pix] = (double) window[0];
				maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);
			}
			else if (arrivalTimes[pix] > window[1])
			{
				arrivalTimes[pix] = (double) window[1];
				maxSlopes[pix] = calcDerivationAtPoint(arrivalTimes[pix], c);
			}
			
			marker[pix] = new IntervalMarker(arrivalTimes[pix], arrivalTimes[pix] + 1);
		}
		input.put(outputKey, arrivalTimes);
		input.put(maxSlopesKey, maxSlopes);
		input.put(outputKey + "Marker", marker);
		
		return input;
	}

	/**
	 * Calculates the position of the maximal derivative of a third order polynomial with coefficient vector c
	 * f(x) = sum_{i=0}^3 c_i * x^i
	 */
	private double calcXPosMaxDerivation(double[] c)
	{
		double x1 = (-2 * c[2] + Math.sqrt(4 * Math.pow(c[2], 2) - 12 * c[1] * c[3])) / (6 * c[3]);
		double x2 = (-2 * c[2] - Math.sqrt(4 * Math.pow(c[2], 2) - 12 * c[1] * c[3])) / (6 * c[3]);
		if (calcDerivationAtPoint(x1, c) > calcDerivationAtPoint(x2, c))
		{
			return x1;
		}
		return x2;
	}

	/**
	 * Calculates the derivative of a third order polynomial  f(x) = sum_{i=0}^3 c_i * x^i
	 * @param x position
	 * @param c coefficient vector
	 * @return derivative at x for coefficient vector c
	 */
	private double calcDerivationAtPoint(double x, double[] c) {
		return 3 * c[3]*x*x + 2 * c[2]*x + c[1];
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


    public void setFit_degree(int fit_degree) {
        this.fit_degree = fit_degree;
    }


}
