package fact.extraction;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;

public class RisingEdgePolynomFit implements Processor {
	
	private String risingEdgeKey = null;
	
	private String dataKey = null;
	
	private String outputKey = null;

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, dataKey,risingEdgeKey);
		
		double[] maxDerivations = new double[Constants.NUMBEROFPIXEL];
		
		double[] data = (double[]) input.get(dataKey);
		
		double[] buffer = (double[]) input.get(risingEdgeKey);
		
		int[] risingEdges = new int[buffer.length];
		for (int i = 0 ; i < buffer.length ; i++)
		{
			risingEdges[i] = (int) buffer[i];
		}
		
		for (int pix = 0 ; pix < Constants.NUMBEROFPIXEL ; pix++)
		{
			int pos = risingEdges[pix];
			double[] times = {pos-1,pos,pos+1};
			double[] derivations = { (data[pos-1+2]-data[pos-1-2]) , (data[pos+2]-data[pos-2]) , (data[pos+1+2]-data[pos+1-2]) };
			
			double[] parabolaParameters = calculateParabola(times,derivations);
			
		}
		
		return input;
	}

	/**
	 * 
	 * 
	 * @param times
	 * @param derivations
	 * @return
	 */
	private double[] calculateParabola(double[] times, double[] derivations) {
				
		double[][] matrixData = { { times[0]*times[0] , times[0] , 1 } ,
								  { times[1]*times[1] , times[1] , 1 } ,
								  { times[2]*times[2] , times[2] , 1 } };
		
		RealVector vector = MatrixUtils.createRealVector(derivations);
		RealMatrix matrix = MatrixUtils.createRealMatrix(matrixData);
		
		RealVector param = MatrixUtils.inverse(matrix).operate(vector);
		
		double[] result = param.toArray();
		
		return result;
	}

}
