package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * SimpleThreshold. Identifies showerPixel in the image array.
 * 	Identify all pixel with a charge above a given threshold (Photoncharge higher Threshold)
 *  @author Jens Buss &lt;jens.buss@tu-dortmund.de&gt
 *
 */

public class SimpleThreshold extends BasicCleaning implements Processor{
	static Logger log = LoggerFactory.getLogger(SimpleThreshold.class);

    @Parameter(required = true)
	private String photonChargeKey;

    @Parameter(required = true)
	private String outputKey;

    @Parameter(required = true, description = "The smallest PhotonCharge a Pixel must have to be " +
            "identified as a CorePixel")
	private  double threshold;

    @Parameter(required = false)
    private String[] starPositionKeys = null;
    @Parameter(required = false, defaultValue="Constants.PIXEL_SIZE")
	private double starRadiusInCamera = Constants.PIXEL_SIZE;

    private PixelSet cleanedPixelSet;
	
    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		DateTime timeStamp = null;
		if (input.containsKey("UnixTimeUTC") == true){
    		Utils.isKeyValid(input, "UnixTimeUTC", int[].class);
    		int[] eventTime = (int[]) input.get("UnixTimeUTC");
        	timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
    	}
    	else {
    		// MC Files don't have a UnixTimeUTC in the data item. Here the timestamp is hardcoded to 1.1.2000
    		// => The 12 bad pixels we have from the beginning on are used.
    		timeStamp = new DateTime(2000, 1, 1, 0, 0);
    	}

		double[] photonCharge = Utils.toDoubleArray(input.get(photonChargeKey));

		ArrayList<Integer> showerPixel= new ArrayList<>();
		
		showerPixel = addCorePixel(showerPixel, photonCharge, threshold, timeStamp);


        if (starPositionKeys != null)
        {
            PixelSet starSet = new PixelSet();
            for (String starPositionKey : starPositionKeys)
            {
                Utils.isKeyValid(input, starPositionKey, double[].class);
                double[] starPosition = (double[]) input.get(starPositionKey);

                showerPixel = removeStarIslands(showerPixel,starPosition,starSet,starRadiusInCamera, log);
				input.put("Starset", starSet);
			}
        }

        if(showerPixel.size() > 0){

            cleanedPixelSet = new PixelSet();
            for (int i = 0; i < showerPixel.size(); i++) {
                cleanedPixelSet.addById(showerPixel.get(i));
            }
            input.put(outputKey, cleanedPixelSet);
        }

		return input;
	}

	/*
	 * Getter and Setter
	 */

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public void setStarPositionKeys(String[] starPositionKeys) {
		this.starPositionKeys = starPositionKeys;
	}

	public void setStarRadiusInCamera(double starRadiusInCamera) {
		this.starRadiusInCamera = starRadiusInCamera;
	}

	public void setCleanedPixelSet(PixelSet cleanedPixelSet) {
		this.cleanedPixelSet = cleanedPixelSet;
	}
}
