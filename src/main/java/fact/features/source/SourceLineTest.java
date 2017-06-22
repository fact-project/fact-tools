package fact.features.source;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This function calculates the source line test (after an idea by W. Rhode).
 */
public class SourceLineTest implements Processor{

	@Parameter(required = true, defaultValue = "photoncharge", description = "Key of photoncharge array.")
	private String photonCharge;
	//consider the error of the arrival time later...
	@Parameter(required = true, defaultValue = "arrival_time", description = "Key of arrivaltime array.")
	private String arrivalTime;
	@Parameter(required = true, defaultValue = "shower_pixel", description = "Key of showerpixel array.")
	private String pixelSetKey;
	@Parameter(required = true, defaultValue = "sourceposition", description = "Key of sourceposition vector.")
	private String sourcePosition;
	@Parameter(required = true, defaultValue = "source_line_test", description = "Master outputkey, which will be written before every attribute.")
	private String outputKey;

	private double[] arrivalTimeArray = null;
	private double[] photonChargeArray = null;
	private int[] showerPixelArray = null;
	private double[] sourcePositionArray = null;

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();


	@Override
	public Data process(Data input)
	{
//	    float[] mpGeomXCoord            = DefaultPixelMapping.getGeomXArray();
//	    float[] mpGeomYCoord            = DefaultPixelMapping.getGeomYArray();
		//Test for keys.
		Utils.mapContainsKeys( input, photonCharge, arrivalTime, pixelSetKey, sourcePosition);

		photonChargeArray = (double[]) input.get(photonCharge);
		arrivalTimeArray = (double[]) input.get(arrivalTime);

		showerPixelArray = ((PixelSet) input.get(pixelSetKey)).toIntArray();

		sourcePositionArray = (double[]) input.get(sourcePosition);

		if (showerPixelArray.length < 4)
		{
			input.put(outputKey + "_source_line_test_value_projected", Double.NaN);
			input.put(outputKey + "_source_line_test_value_sorted", Double.NaN);
			input.put(outputKey + "_mean_shower_velocity_projected", Double.NaN);
			input.put(outputKey + "_mean_shower_velocity_sorted", Double.NaN);
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
			cogX += pixelMap.getPixelFromId(chid).getXPositionInMM() * photonChargeArray[chid];
			cogY += pixelMap.getPixelFromId(chid).getYPositionInMM() * photonChargeArray[chid];
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

		if (id_tl1 == -1 || id_tl2 == -1 || id_tl3 == -1 ||
				id_tf1 == -1 || id_tf2 == -1 || id_tf3 == -1)
		{
			input.put(outputKey + "_source_line_test_value_projected", Double.NaN);
			input.put(outputKey + "_source_line_test_value_sorted", Double.NaN);
			input.put(outputKey + "_mean_shower_velocity_projected", Double.NaN);
			input.put(outputKey + "_mean_shower_velocity_sorted", Double.NaN);
			return input;
		}

		double f_time = (tf1 + tf2 + tf3) / 3.0;
		double l_time = (tl1 + tl2 + tl3) / 3.0;

		double tf1_x = pixelMap.getPixelFromId(id_tf1).getXPositionInMM();
		double tf1_y = pixelMap.getPixelFromId(id_tf1).getXPositionInMM();
		double tf2_x = pixelMap.getPixelFromId(id_tf2).getXPositionInMM();
		double tf2_y = pixelMap.getPixelFromId(id_tf2).getXPositionInMM();
		double tf3_x = pixelMap.getPixelFromId(id_tf3).getXPositionInMM();
		double tf3_y = pixelMap.getPixelFromId(id_tf3).getXPositionInMM();

		double f_x = (tf1_x + tf2_x + tf3_x) / 3.0;
		double f_y = (tf1_y + tf2_y + tf3_y) / 3.0;

		double tl1_x = pixelMap.getPixelFromId(id_tl1).getXPositionInMM();
		double tl1_y = pixelMap.getPixelFromId(id_tl1).getXPositionInMM();
		double tl2_x = pixelMap.getPixelFromId(id_tl2).getXPositionInMM();
		double tl2_y = pixelMap.getPixelFromId(id_tl2).getXPositionInMM();
		double tl3_x = pixelMap.getPixelFromId(id_tl3).getXPositionInMM();
		double tl3_y = pixelMap.getPixelFromId(id_tl3).getXPositionInMM();

		double l_x = (tl1_x + tl2_x + tl3_x) / 3.0;
		double l_y = (tl1_y + tl2_y + tl3_y) / 3.0;

		double meanShowerVelocitySorted = Math.sqrt((f_x - l_x)*(f_x - l_x) + (f_y - l_y) * (f_y - l_y)) / (l_time - f_time);

		cogX /= size;
		cogY /= size;
		cogT /= size;

		cogSourceAngle = Math.atan2((cogY - sourcePositionArray[1]) , (cogX - sourcePositionArray[0]));

		// Calculate values for reconstruction
		for(int chid : showerPixelArray)
		{
			double posx = pixelMap.getPixelFromId(chid).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(chid).getYPositionInMM();

			double pixelSourceAngle = Math.atan2((posy - sourcePositionArray[1]), (posx - sourcePositionArray[0]));
			double pixelSourceDist       = (posy - sourcePositionArray[1]) / Math.sin(pixelSourceAngle);

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

			double posx = pixelMap.getPixelFromId(chid).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(chid).getYPositionInMM();

			sourceLineTestValueProjected += recoW[chid] * Math.sqrt((recoX[chid] - posx) * (recoX[chid] - posx) + (recoY[chid] - posy) * (recoY[chid] - posy));

			// Calculate with sorted velocity
			dt = arrivalTimeArray[chid] - cogT;
			dx = dt * meanShowerVelocitySorted;
			recoX[chid] = cogX + dx * Math.cos(cogSourceAngle);
			recoY[chid] = cogY + dx * Math.sin(cogSourceAngle);

			sourceLineTestValueSorted += recoW[chid] * Math.sqrt((recoX[chid] - posx) * (recoX[chid] - posx) + (recoY[chid] - posy) * (recoY[chid] - posy));


			recoWsum += recoW[chid];
		}

		sourceLineTestValueProjected /= recoWsum;
		sourceLineTestValueSorted /= recoWsum;


		input.put(outputKey + "_source_line_test_value_projected", sourceLineTestValueProjected);
		input.put(outputKey + "_source_line_test_value_sorted", sourceLineTestValueSorted);
		input.put(outputKey + "_mean_shower_velocity_projected", meanShowerVelocityProjected);
		input.put(outputKey + "_mean_shower_velocity_sorted", meanShowerVelocitySorted);


		return input;
	}


	public String getPhotonCharge() {
		return photonCharge;
	}

	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}

	public String getSourcePosition() {
		return sourcePosition;
	}

	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}
	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
}
