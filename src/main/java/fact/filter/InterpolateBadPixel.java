package fact.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Parameter;
import fact.Constants;
import fact.data.EventUtils;
import fact.utils.SimpleFactEventProcessor;
import fact.viewer.ui.DefaultPixelMapping;

/**
 * 
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class InterpolateBadPixel extends SimpleFactEventProcessor<float[], float[]> {
	static Logger log = LoggerFactory.getLogger(InterpolateBadPixel.class);
	private float[] nData;
	private int[] badChIds =  {863,868,297,927,80,873,1093,1094,527,528,721,722};
//	private int twins[] = {1093,1094,527,528,721,722};
//	private int crazy[] = {863,868,297};
//	private int bad[] = {927,80,873};
	
	@Override
	public float[] processSeries(float[] series) {
		int roi = series.length / Constants.NUMBEROFPIXEL;
		//copy the whole data intzo a new array. 
		if(outputKey != null && !outputKey.equals("")) {	
			nData = new float[series.length];
			for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
				//if were looking at a badPixel
				if(EventUtils.arrayContains(badChIds, pix)){
					int[] currentNeighbors = DefaultPixelMapping.getNeighborsFromChid(pix);
//					log.debug("interpolating pix number: " + pix);
					//iterate over all slices
					for (int slice = 0; slice < roi; slice++) {
						int pos = pix * roi + slice;
						float avg = 0.0f; 
						int numNeighbours = 0;
						for(int nPix: currentNeighbors){
							//if neighbour exists
							if (nPix != -1 && !EventUtils.arrayContains(badChIds,  nPix) ){
								avg += series[nPix*roi + slice];
								numNeighbours++;
							}
						}
						//set value of current slice to average of surrounding pixels
						nData[pos] = avg/(float)numNeighbours;
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
				float avg = 0.0f; 
				int numNeighbours = 0;
				for(int nPix: currentNeighbors){
					//if neighbour exists
					if (nPix != -1 && !EventUtils.arrayContains(badChIds,  nPix) ){
						avg += series[nPix*roi + slice];
						numNeighbours++;
					}
				}
				//set value of current slice to average of surrounding pixels
				series[pos] = avg/(float)numNeighbours;
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
	@Parameter(required = true, description = "A List of ChIds for Pixels that are considered defect", defaultValue="The softIds Taken from https://www.fact-project.org/logbook/misc.php?page=known_problems")
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
