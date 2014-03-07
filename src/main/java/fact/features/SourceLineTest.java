package fact.features;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;

public class SourceLineTest implements Processor{

	/**
	 * This function calculates the source line test (after an idea by W. Rhode).
	 */
	@Override
	public Data process(Data input)
	{
	    float[] mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
	    float[] mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
		//Test for keys.
		EventUtils.mapContainsKeys(getClass(), input, photonCharge, arrivalTime, showerPixel, sourcePosition);
		
		photonChargeArray = (double[]) input.get(photonCharge);
		
		
		arrivalTimeArray = new double[photonChargeArray.length];
		int[] arrivalPos = (int[]) input.get(arrivalTime);
		// Convert to double

		for(int i = 0; i < arrivalPos.length; i++)
		{
			
			arrivalTimeArray[i] = (double) arrivalPos[i];
			
		}
		
		
		showerPixelArray = (int[]) input.get(showerPixel);
		sourcePositionArray = (double[]) input.get(sourcePosition);		
		
		if (showerPixelArray.length < 6)
		{
			input.put(outputKey + "_sourceLineTestValueProjected", Double.NaN);
			input.put(outputKey + "_sourceLineTestValueSorted", Double.NaN);
			input.put(outputKey + "_meanShowerVelocityProjected", Double.NaN);
			input.put(outputKey + "_meanShowerVelocitySorted", Double.NaN);
			return input;
		}
			
		
		double cogT = 0;
		double size = 0;
		double cogX = 0,cogY = 0;
		
		// Variables for speed calculation
		double projPrimary = 0; // Projected ordinate on primary axis towards source position
		//double projSecondary = 0; // secondary axis concerning cog---source
		
		double projPrimaryMin = Double.MAX_VALUE;
		double projPrimaryMax = Double.MIN_VALUE;
		double timeMin = Double.MAX_VALUE;
		double timeMax = Double.MIN_VALUE;
		
		double cogSourceAngle = 0;
		
		// variables for reconstruction
		
		double [] recoX = new double[photonChargeArray.length];
		double [] recoY = new double[photonChargeArray.length];
		double [] recoW = new double[photonChargeArray.length]; // weights are 1.0 in this version
		double recoWsum = photonChargeArray.length;
		
		// output variables
		double sourceLineTestValueProjected = 0;
		double sourceLineTestValueSorted = 0;
		
		double meanShowerVelocityProjected = 0;

		double tf1 = Double.MAX_VALUE, tf2 = Double.MAX_VALUE, tf3 = Double.MAX_VALUE; // First three times
		double tl1 = Double.MIN_VALUE, tl2 = Double.MIN_VALUE, tl3 = Double.MIN_VALUE; // Last three times
		int id_tf1 = -1, id_tf2 = -1, id_tf3 = -1; // Corresponding ids
		int id_tl1 = -1, id_tl2 = -1, id_tl3 = -1;
		
		
		
		for(int chid : showerPixelArray)
		{
			cogT += arrivalTimeArray[chid] * photonChargeArray[chid];
			cogX += mpGeomXCoord[chid] * photonChargeArray[chid];
			cogY += mpGeomYCoord[chid] * photonChargeArray[chid];
			size += photonChargeArray[chid];
			
			double t = arrivalTimeArray[chid]; // tf1 < tf2 < tf3 // tl1 > tl2 > tl3

			
			if (t < tf1)
			{
				tf3 = tf2;
				tf2 = tf1;
				tf1 = t;
				id_tf3 = id_tf2;
				id_tf2 = id_tf1;
				id_tf1 = chid;
			}
			else if (t < tf2)
			{
				tf3 = tf2;
				tf2 = t;
				id_tf3 = id_tf2;
				id_tf2 = chid;
			}
			else if (t < tf3)
			{
				tf3 = t;
				id_tf3 = chid;
			}
			
			if (t > tl1)
			{
				tl3 = tl2;
				tl2 = tl1;
				tl1 = t;
				id_tl3 = id_tl2;
				id_tl2 = id_tl1;
				id_tl1 = chid;
			}
			else if (t > tl2)
			{
				tl3 = tl2;
				tl2 = t;
				id_tl3 = id_tl2;
				id_tl2 = chid;
			}
			else if (t > tl3)
			{
				tl3 = t;
				id_tl3 = chid;
			}
			
		}
		
		double f_time = (tf1 + tf2 + tf3) / 3.0;
		double l_time = (tl1 + tl2 + tl3) / 3.0;
		
		double f_x = (mpGeomXCoord[id_tf1] + mpGeomXCoord[id_tf2] + mpGeomXCoord[id_tf3]) / 3.0;
		double f_y = (mpGeomYCoord[id_tf1] + mpGeomYCoord[id_tf2] + mpGeomYCoord[id_tf3]) / 3.0;
		
		double l_x = (mpGeomXCoord[id_tl1] + mpGeomXCoord[id_tl2] + mpGeomXCoord[id_tl3]) / 3.0;
		double l_y = (mpGeomYCoord[id_tl1] + mpGeomYCoord[id_tl2] + mpGeomYCoord[id_tl3]) / 3.0;
		
		double meanShowerVelocitySorted = Math.sqrt((f_x - l_x)*(f_x - l_x) + (f_y - l_y) * (f_y - l_y)) / (l_time - f_time);
		
		cogX /= size;
		cogY /= size;
		cogT /= size;
		
		cogSourceAngle = Math.atan2((cogY - sourcePositionArray[1]) , (cogX - sourcePositionArray[0]));
		
		// Calculate values for reconstruction
		for(int chid : showerPixelArray)
		{
			double pixelSourceAngle = Math.atan2((mpGeomYCoord[chid] - sourcePositionArray[1]), (mpGeomXCoord[chid] - sourcePositionArray[0]));
			double pixelSourceDist       = (mpGeomYCoord[chid] - sourcePositionArray[1]) / Math.sin(pixelSourceAngle);
			
			projPrimary = Math.cos(pixelSourceAngle - cogSourceAngle) * pixelSourceDist;
			
			//Use for max dist from Line test later...
			//projSecondary = Math.sin(pixelSourceAngle - cogSourceAngle) * pixelSourceDist;
			
			if(projPrimary < projPrimaryMin)
			{
				projPrimaryMin = projPrimary;
			}
			if(projPrimary > projPrimaryMax)
			{
				projPrimaryMax = projPrimary;
			}
			if(arrivalTimeArray[chid] < timeMin)
			{
				timeMin = arrivalTimeArray[chid];
			}
			if(arrivalTimeArray[chid] > timeMax)
			{
				timeMax = arrivalTimeArray[chid];
			}
		}
		meanShowerVelocityProjected = (projPrimaryMax - projPrimaryMin) / (timeMax - timeMin);
		
		recoWsum = 0;
		for(int chid : showerPixelArray)
		{
			 recoW[chid] = 1.0; // consider arrival time error later
		     
			 // Calculate with projected velocity
			 double dt = arrivalTimeArray[chid] - cogT;
		     double dx = dt * meanShowerVelocityProjected;
		     recoX[chid] = cogX + dx * Math.cos(cogSourceAngle);
		     recoY[chid] = cogY + dx * Math.sin(cogSourceAngle);
		     
		     sourceLineTestValueProjected += recoW[chid] * Math.sqrt((recoX[chid] - mpGeomXCoord[chid]) * (recoX[chid] - mpGeomXCoord[chid]) + (recoY[chid] - mpGeomYCoord[chid]) * (recoY[chid] - mpGeomYCoord[chid]));
		     
		     // Calculate with sorted velocity
			 dt = arrivalTimeArray[chid] - cogT;
		     dx = dt * meanShowerVelocitySorted;
		     recoX[chid] = cogX + dx * Math.cos(cogSourceAngle);
		     recoY[chid] = cogY + dx * Math.sin(cogSourceAngle);
		     
		     sourceLineTestValueSorted += recoW[chid] * Math.sqrt((recoX[chid] - mpGeomXCoord[chid]) * (recoX[chid] - mpGeomXCoord[chid]) + (recoY[chid] - mpGeomYCoord[chid]) * (recoY[chid] - mpGeomYCoord[chid]));
		     
		     
		     recoWsum += recoW[chid];
		}
		
		sourceLineTestValueProjected /= recoWsum;
		sourceLineTestValueSorted /= recoWsum;
		
		
		input.put(outputKey + "_sourceLineTestValueProjected", sourceLineTestValueProjected);
		input.put(outputKey + "_sourceLineTestValueSorted", sourceLineTestValueSorted);
		input.put(outputKey + "_meanShowerVelocityProjected", meanShowerVelocityProjected);
		input.put(outputKey + "_meanShowerVelocitySorted", meanShowerVelocitySorted);
		
		
		return input;
	}
	
	
	public String getPhotonCharge() {
		return photonCharge;
	}
	@Parameter(required = true, defaultValue = "photonCharge", description = "Key of photoncharge array.")
	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}
	@Parameter(required = true, defaultValue = "arrivalTime", description = "Key of arrivaltime array.")
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getShowerPixel() {
		return showerPixel;
	}
	@Parameter(required = true, defaultValue = "showerPixel", description = "Key of showerpixel array.")
	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}
	public String getSourcePosition() {
		return sourcePosition;
	}
	@Parameter(required = true, defaultValue = "sourceposition", description = "Key of sourceposition vector.")
	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(required = true, defaultValue = "SourceLineTest", description = "Master outputkey, which will be written before every attribute.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}



	private double[] arrivalTimeArray = null;
	private double[] photonChargeArray = null;
	private int[] showerPixelArray = null;
	private double[] sourcePositionArray = null;
	
	private String photonCharge;
	//consider the error of the arrival time later...
	private String arrivalTime;
	private String showerPixel;
	private String sourcePosition;
	private String outputKey;
	
	
}
