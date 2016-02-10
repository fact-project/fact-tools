package fact.features.source;

import org.apache.commons.lang3.ArrayUtils;

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

	@Parameter(required = false, defaultValue = "pixels:estNumPhotons", description = "Key of photoncharge array.")
	private String estNumPhotonsKey = "pixels:estNumPhotons";
	//consider the error of the arrival time later...
	@Parameter(required = false, defaultValue = "pixels:arrivalTimes", description = "Key of arrivaltime array.")
	private String arrivalTimesKey = "pixels:arrivalTimes";
	@Parameter(required = false, defaultValue = "shower", description = "Key of showerpixel array.")
	private String pixelSetKey = "shower";
	@Parameter(required = false, defaultValue="sourcePosition:x")
	private String sourcePositionXKey = "sourcePosition:x";
	@Parameter(required = false, defaultValue="sourcePosition:y")
	private String sourcePositionYKey = "sourcePosition:y";
	@Parameter(required = false, defaultValue = "shower:sourceLineTest", description = "Master outputkey, which will be written before every attribute.")
	private String outputKey = "shower:sourceLineTest";

	FactPixelMapping pixelMap = FactPixelMapping.getInstance();


	@Override
	public Data process(Data item)
	{
		Utils.mapContainsKeys( item, estNumPhotonsKey, arrivalTimesKey, pixelSetKey, sourcePositionXKey, sourcePositionYKey);
		
		double[] estNumPhotons = (double[]) item.get(estNumPhotonsKey);
		double[] arrivalTimes = (double[]) item.get(arrivalTimesKey);

		int[] shower = ((PixelSet) item.get(pixelSetKey)).toIntArray();

		double sourcex = (Double) item.get(sourcePositionXKey);
		double sourcey = (Double) item.get(sourcePositionYKey);
		
		// output variables set to NaN, so if the values cannot be calculated, NaN will be put in the data item
		double sourceLineTestValueProjected = Double.NaN;
		double sourceLineTestValueSorted = Double.NaN;
		double meanShowerVelocityProjected = Double.NaN;
		double meanShowerVelocitySorted = Double.NaN;
		
		// Calculation only possible if there are at least 4 shower pixel
		if (shower.length >= 4)
		{	
			double[] result = calculateCogSpaceAndTime(shower, arrivalTimes, estNumPhotons);
			double cogt = result[0];
			double cogx = result[1];
			double cogy = result[2];
			
			double cogSourceAngle = Math.atan2((cogy - sourcey) , (cogx - sourcex));
			
			double f_time = 0;
			double l_time = 0;
			int[] foundIds = findFirstAndLastThreePixelInShower(shower, arrivalTimes, f_time, l_time);

			// If one of the ids is -1 the calculation is not possible
			if (!ArrayUtils.contains(foundIds, -1))
			{
				result = calculateMeanShowerVelocity(shower, arrivalTimes, foundIds, l_time, f_time, cogx, cogy, cogt, cogSourceAngle, sourcex, sourcey);
				meanShowerVelocityProjected = result[0];
				meanShowerVelocitySorted = result[1];
				
				result = calculateTestValues(shower, estNumPhotons, arrivalTimes, cogt, cogx, cogy, cogSourceAngle, meanShowerVelocityProjected, meanShowerVelocitySorted);
				sourceLineTestValueProjected = result[0];
				sourceLineTestValueSorted = result[1];
				
			}			
		}
		item.put(outputKey + ":value:projected", sourceLineTestValueProjected);
		item.put(outputKey + ":value:sourted", sourceLineTestValueSorted);
		item.put(outputKey + ":meanShowerVelocity:projected", meanShowerVelocityProjected);
		item.put(outputKey + ":meanShowerVelocity:sorted", meanShowerVelocitySorted);
		return item;
	}
	
	private double[] calculateCogSpaceAndTime(int[] shower, double[] arrivalTimes, double[] estNumPhotons){
		double cogt = 0;
		double size = 0;
		double cogx = 0;
		double cogy = 0;
		for(int chid : shower)
		{
			cogt += arrivalTimes[chid] * estNumPhotons[chid];
			cogx += pixelMap.getPixelFromId(chid).getXPositionInMM() * estNumPhotons[chid];
			cogy += pixelMap.getPixelFromId(chid).getYPositionInMM() * estNumPhotons[chid];
			size += estNumPhotons[chid];
		}
		cogt /= size;
		cogx /= size;
		cogy /= size;
		double[] result = {cogt,cogx,cogy};
		return result;
	}
		
	private int[] findFirstAndLastThreePixelInShower(
			int[] shower,
			double[] arrivalTimes,
			double f_time, 
			double l_time
			){
		
		double tf1 = Double.MAX_VALUE, tf2 = Double.MAX_VALUE, tf3 = Double.MAX_VALUE; // First three times
		double tl1 = Double.MIN_VALUE, tl2 = Double.MIN_VALUE, tl3 = Double.MIN_VALUE; // Last three times
		// id_t<f,l><1,2,3> stands for chid of the pixel with the <f: first, l: last> arrival times (<1,2,3> is the number)
		int id_tf1 = -1, id_tf2 = -1, id_tf3 = -1;
		int id_tl1 = -1, id_tl2 = -1, id_tl3 = -1;
		
		for(int chid : shower)
		{
			double t = arrivalTimes[chid]; // tf1 < tf2 < tf3 // tl1 > tl2 > tl3
			
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
		
		f_time = (tf1 + tf2 + tf3) / 3.0;
		l_time = (tl1 + tl2 + tl3) / 3.0;
		
		int[] foundIds = {id_tf1,id_tf2,id_tf3,id_tl1,id_tl2,id_tl3};
		return foundIds;
	}
	
	private double[] calculateMeanShowerVelocity(
			int[] shower,
			double[] arrivalTimes,
			int[] foundIds, 
			double l_time,
			double f_time,
			double cogx, 
			double cogy, 
			double cogt, 
			double cogSourceAngle,
			double sourcex, 
			double sourcey
			){
		// Variables for speed calculation
		double projPrimary = 0; // Projected ordinate on primary axis towards source position
		//double projSecondary = 0; // secondary axis concerning cog---source
		
		double projPrimaryMin = Double.MAX_VALUE;
		double projPrimaryMax = Double.MIN_VALUE;
		double timeMin = Double.MAX_VALUE;
		double timeMax = Double.MIN_VALUE;
		
		double tf1_x = pixelMap.getPixelFromId(foundIds[0]).getXPositionInMM();
		double tf1_y = pixelMap.getPixelFromId(foundIds[0]).getXPositionInMM();
		double tf2_x = pixelMap.getPixelFromId(foundIds[1]).getXPositionInMM();
		double tf2_y = pixelMap.getPixelFromId(foundIds[1]).getXPositionInMM();
		double tf3_x = pixelMap.getPixelFromId(foundIds[2]).getXPositionInMM();
		double tf3_y = pixelMap.getPixelFromId(foundIds[2]).getXPositionInMM();
		
		double f_x = (tf1_x + tf2_x + tf3_x) / 3.0;
		double f_y = (tf1_y + tf2_y + tf3_y) / 3.0;
		
		double tl1_x = pixelMap.getPixelFromId(foundIds[3]).getXPositionInMM();
		double tl1_y = pixelMap.getPixelFromId(foundIds[3]).getXPositionInMM();
		double tl2_x = pixelMap.getPixelFromId(foundIds[4]).getXPositionInMM();
		double tl2_y = pixelMap.getPixelFromId(foundIds[4]).getXPositionInMM();
		double tl3_x = pixelMap.getPixelFromId(foundIds[5]).getXPositionInMM();
		double tl3_y = pixelMap.getPixelFromId(foundIds[5]).getXPositionInMM();
		
		double l_x = (tl1_x + tl2_x + tl3_x) / 3.0;
		double l_y = (tl1_y + tl2_y + tl3_y) / 3.0;
		
		double meanShowerVelocitySorted = Math.sqrt((f_x - l_x)*(f_x - l_x) + (f_y - l_y) * (f_y - l_y)) / (l_time - f_time);
		
		// Calculate values for reconstruction
		for(int chid : shower)
		{
			double posx = pixelMap.getPixelFromId(chid).getXPositionInMM();
			double posy = pixelMap.getPixelFromId(chid).getYPositionInMM();
			
			double pixelSourceAngle = Math.atan2((posy - sourcey), (posx - sourcex));
			double pixelSourceDist       = (posy - sourcey) / Math.sin(pixelSourceAngle);
			
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
			if(arrivalTimes[chid] < timeMin)
			{
				timeMin = arrivalTimes[chid];
			}
			if(arrivalTimes[chid] > timeMax)
			{
				timeMax = arrivalTimes[chid];
			}
		}
		double meanShowerVelocityProjected = (projPrimaryMax - projPrimaryMin) / (timeMax - timeMin);
		double[] result = {meanShowerVelocityProjected,meanShowerVelocitySorted};
		return result;
		
	}

	private double[] calculateTestValues(
			int[] shower,
			double[] estNumPhotons,
			double[] arrivalTimes,
			double cogt,
			double cogx,
			double cogy,
			double cogSourceAngle,
			double meanShowerVelocityProjected,
			double meanShowerVelocitySorted
			){
		// variables for reconstruction
		double [] recoX = new double[estNumPhotons.length];
		double [] recoY = new double[estNumPhotons.length];
		double [] recoW = new double[estNumPhotons.length]; // weights are 1.0 in this version
		double recoWsum = estNumPhotons.length;
		
		recoWsum = 0;
		double sourceLineTestValueProjected = 0;
		double sourceLineTestValueSorted = 0;
		for(int chid : shower)
		{
			 recoW[chid] = 1.0; // consider arrival time error later
		     
			 // Calculate with projected velocity
			 double dt = arrivalTimes[chid] - cogt;
		     double dx = dt * meanShowerVelocityProjected;
		     recoX[chid] = cogx + dx * Math.cos(cogSourceAngle);
		     recoY[chid] = cogy + dx * Math.sin(cogSourceAngle);
		     
		     double posx = pixelMap.getPixelFromId(chid).getXPositionInMM();
			 double posy = pixelMap.getPixelFromId(chid).getYPositionInMM();
		     
		     sourceLineTestValueProjected += recoW[chid] * Math.sqrt((recoX[chid] - posx) * (recoX[chid] - posx) + (recoY[chid] - posy) * (recoY[chid] - posy));
		     
		     // Calculate with sorted velocity
			 dt = arrivalTimes[chid] - cogt;
		     dx = dt * meanShowerVelocitySorted;
		     recoX[chid] = cogx + dx * Math.cos(cogSourceAngle);
		     recoY[chid] = cogy + dx * Math.sin(cogSourceAngle);
		     
		     sourceLineTestValueSorted += recoW[chid] * Math.sqrt((recoX[chid] - posx) * (recoX[chid] - posx) + (recoY[chid] - posy) * (recoY[chid] - posy));
		     
		     
		     recoWsum += recoW[chid];
		}
		
		sourceLineTestValueProjected /= recoWsum;
		sourceLineTestValueSorted /= recoWsum;
		double[] result = {sourceLineTestValueProjected,sourceLineTestValueSorted};
		return result;
		
	}

}
