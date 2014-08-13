package fact.datacorrection;

import fact.Constants;
import fact.Utils;
import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * 
 * This Processor interpolates all values for a broken Pixel by the average values of its neighboring Pixels.
  * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class InterpolateBadPixel implements Processor {
	static Logger log = LoggerFactory.getLogger(InterpolateBadPixel.class);
	private double[] nData;
    @Parameter(required = false, description = "A List of ChIds for Pixels that are considered defect",
            defaultValue="The softIds Taken from https://www.fact-project.org/logbook/misc.php?page=known_problems")
	private Integer[] badChIds =  {863,868,297,927,80,873,1093,1094,527,528,721,722};



    @Parameter(required = true, description = "The data to work on")
    private String key;
    @Parameter(required = true, description = "The name of the output")
    private String outputKey;
//	private int twins[] = {1093,1094,527,528,721,722};
//	private int crazy[] = {863,868,297};
//	private int bad[] = {927,80,873};

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	

	@Override
	public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);
        if(!key.equals(outputKey)){
            double[] newdata = new double[data.length];
            System.arraycopy(data,0, newdata, 0, data.length);
            data = interpolate(newdata, badChIds);
        } else {
            data = interpolate(data, badChIds);
        }
        data = interpolate(data, badChIds);
        item.put(outputKey, data);
		return item;
	}

    public double[] interpolate(double[] data, Integer[] chids) {
        int roi = data.length / Constants.NUMBEROFPIXEL;


        for (int pix: chids) {
			FactCameraPixel[] currentNeighbors = pixelMap.getNeighboursFromID(pix);

			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				//temp save the current value
				double avg = 0.0f;
                int numNeighbours = 0;

                for(FactCameraPixel nPix: currentNeighbors){
						avg += data[nPix.id*roi + slice];
						numNeighbours++;
				}
				//set value of current slice to average of surrounding pixels
				data[pos] = avg/(double)numNeighbours;
			}
		}
        return data;
    }

    /*
     * Getter and Setter
     */
	public Integer[] getBadChIds() {
		return badChIds;
	}
	public void setBadChIds(Integer[] badChIds) {
        this.badChIds = badChIds;
	}
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
