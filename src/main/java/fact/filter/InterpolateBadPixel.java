package fact.filter;

import fact.Constants;
import fact.Utils;
import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import fact.utils.SimpleFactEventProcessor;
import fact.viewer.ui.DefaultPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Parameter;

/**
 * 
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class InterpolateBadPixel extends SimpleFactEventProcessor<double[], double[]> {
	static Logger log = LoggerFactory.getLogger(InterpolateBadPixel.class);
	private double[] nData;
	private int[] badChIds =  {863,868,297,927,80,873,1093,1094,527,528,721,722};
//	private int twins[] = {1093,1094,527,528,721,722};
//	private int crazy[] = {863,868,297};
//	private int bad[] = {927,80,873};

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	//returns true if the specified array is anywhere in the array
	public static boolean arrayContains(int[] ar, int value) {
		for(int i = 0; i < ar.length; i++){
			if(ar[i] == value) return true;
		}
		return false;
	}
	
	@Override
	public double[] processSeries(double[] series) {
		int roi = series.length / Constants.NUMBEROFPIXEL;
		//copy the whole data intzo a new array.
		if(outputKey != null && !outputKey.equals("")) {	
			nData = new double[series.length];
			for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
				//if were looking at a badPixel
				if(arrayContains(badChIds, pix)){
					FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);
//					log.debug("interpolating pix number: " + pix);
					//iterate over all slices
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						double avg = 0.0f; 
						int numNeighbours = 0;
						for(FactCameraPixel nPix: currentNeighbors){
							//if neighbour exists
							if (!Utils.arrayContains(badChIds, nPix.chid) ){
								avg += series[nPix.chid*roi + slice];
								numNeighbours++;
							}
						}
						//set value of current slice to average of surrounding pixels
						nData[pos] = avg/(double)numNeighbours;
					}	
				} 
				else//not a bad pixel. just copy the data
				{
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						nData[pos] = series[pos];
					}
				}
			}
			return nData;
		}
		for (int pix: badChIds) {
			int[] currentNeighbors = DefaultPixelMapping.getNeighborsFromChid(pix);
			
			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				//temp save the current value
				double avg = 0.0f; 
				int numNeighbours = 0;
				for(int nPix: currentNeighbors){
					//if neighbour exists
					if (nPix != -1 && !Utils.arrayContains(badChIds, nPix) ){
						avg += series[nPix*roi + slice];
						numNeighbours++;
					}
				}
				//set value of current slice to average of surrounding pixels
				series[pos] = avg/(double)numNeighbours;
			}
		}
		return series;
	}
	
	/*
	 * Getter and Setter
	 */
	
	public int[] getBadChidIds() {
		return badChIds;
	}
	@Parameter(required = true, description = "A List of ChIds for Pixels that are considered defect",
            defaultValue="The softIds Taken from https://www.fact-project.org/logbook/misc.php?page=known_problems")
	public void setBadChidIds(String[] badChIdStrings) {
		try{
			int i=0;
			for(String n : badChIdStrings ){
				badChIds[i] = Integer.parseInt(n);
				i++;
			}
		} catch(NumberFormatException e){
			log.error("couldnt parse badchids from xml.");			
		}
	}
}
