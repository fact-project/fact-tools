/**
 * 
 */
package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;

/**
 * 
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class InterpolateBadPixel implements Processor {
	static Logger log = LoggerFactory.getLogger(InterpolateBadPixel.class);
	private float[] nData;
	private int[] badChIds =  {863,868,297,927,80,873,1093,1094,527,528,721,722};
//	private int twins[] = {1093,1094,527,528,721,722};
//	private int crazy[] = {863,868,297};
//	private int bad[] = {927,80,873};
//	private Integer badCrazy[] =  {863,868,297,927,80,873,1093,1094,527,528,721,722};
	private String key, output;

	public InterpolateBadPixel() {
		
	}
	public InterpolateBadPixel(String key) {
		this.key=key;
	}


	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if(output == null || output ==""){
			input.put(key, processEvent(input, key));
		} else {
			input.put(output, processEvent(input, key));
		}
		
		return input;
	}

	public float[] processEvent(Data input, String key) {
		
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
			
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
	}

	public float[] processSeries(float[] series) {
		int roi = series.length / Constants.NUMBEROFPIXEL;
		//copy the whole data intzo a new array. 
		if(output != null || output !="") {	
			nData = new float[series.length];
			for(int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
				//if were looking at a badPixel
				if(EventUtils.arrayContains(badChIds, pix)){
					int[] currentNeighbors = FactEvent.PIXEL_MAPPING.getNeighborsFromChid(pix);
					
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
		}
		else //overwrite
		{
			for (int pix: badChIds) {
				int[] currentNeighbors = FactEvent.PIXEL_MAPPING.getNeighborsFromChid(pix);
				
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
	public void setBadChidIds(int[] badChIds) {
		this.badChIds = badChIds;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	
}
