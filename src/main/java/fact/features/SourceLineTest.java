package fact.features;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;

public class SourceLineTest implements Processor{

	/**
	 * This function calculates the "RhodeParameter" described in Parfact with adjustments(later) and proper(hopefully) weighting 
	 */
	@Override
	public Data process(Data input)
	{
	    float[] mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
	    float[] mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
		//Test for keys.
		EventUtils.mapContainsKeys(getClass(), input, photonCharge, arrivalTime, showerPixel, sourcePosition, hillasWidth, hillasLength);
		
		photonChargeArray = (double[]) input.get(photonCharge);
		arrivalTimeArray = new double[photonChargeArray.length];
		int[] arrivalPos = (int[]) input.get(arrivalTime);
		for(int i = 0; i < arrivalPos.length; i++)
		{
			arrivalTimeArray[i] = (double) arrivalPos[i];
		}
		showerPixelArray = (int[]) input.get(showerPixel);
		sourcePositionArray = (double[]) input.get(sourcePosition);
		
		hillasLengthValue = (Double) input.get(hillasLength);
		hillasWidthValue = (Double) input.get(hillasWidth);
		
		
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
		double sourceLineTestValue = 0;
		double meanShowerVelocity = 0;

		
		for(int chid : showerPixelArray)
		{
			cogT += arrivalTimeArray[chid] * photonChargeArray[chid];
			cogX += mpGeomXCoord[chid] * photonChargeArray[chid];
			cogY += mpGeomYCoord[chid] * photonChargeArray[chid];
			size += photonChargeArray[chid];
		}
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
		meanShowerVelocity = (projPrimaryMax - projPrimaryMin) / (timeMax - timeMin);
		
		recoWsum = 0;
		for(int chid : showerPixelArray)
		{
			 double dt = arrivalTimeArray[chid] - cogT;
		     double dx = dt * meanShowerVelocity;
		     recoX[chid] = cogX + dx * Math.cos(cogSourceAngle);
		     recoY[chid] = cogY + dx * Math.sin(cogSourceAngle);
		     recoW[chid] = 1.0; // consider arrival time error later
		     recoWsum += recoW[chid];
		     sourceLineTestValue += recoW[chid] * Math.sqrt((recoX[chid] - mpGeomXCoord[chid]) * (recoX[chid] - mpGeomXCoord[chid]) + (recoY[chid] - mpGeomYCoord[chid]) * (recoY[chid] - mpGeomYCoord[chid]));
		}
		
		sourceLineTestValue /= recoWsum;
		
		//@Todo add alpha*alpha here
		double weightedSourceLineTestValue = sourceLineTestValue * ( hillasWidthValue / hillasLengthValue ); 
		
		input.put(outputKey + "_sourceLineTestValue", sourceLineTestValue);
		input.put(outputKey + "_WeightedSourceLineTestValue", weightedSourceLineTestValue);
		input.put(outputKey + "_meanShowerVelocity", meanShowerVelocity);
		
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


	public String getHillasWidth() {
		return hillasWidth;
	}

	@Parameter(required = true, defaultValue = "HillasWidth", description = "Key to Hillas Width.")
	public void setHillasWidth(String hillasWidth) {
		this.hillasWidth = hillasWidth;
	}


	public String getHillasLength() {
		return hillasLength;
	}


	@Parameter(required = true, defaultValue = "HillasLength", description = "Key to Hillas Length.")
	public void setHillasLength(String hillasLength) {
		this.hillasLength = hillasLength;
	}


	private double[] arrivalTimeArray = null;
	private double[] photonChargeArray = null;
	private int[] showerPixelArray = null;
	private double[] sourcePositionArray = null;
	private Double hillasLengthValue = null;
	private Double hillasWidthValue = null;
	
	private String photonCharge;
	//consider the error of the arrival time later...
	private String arrivalTime;
	private String showerPixel;
	private String sourcePosition;
	private String outputKey;
	private String hillasWidth;
	private String hillasLength;
	
	
}
