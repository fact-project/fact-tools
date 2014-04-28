package fact.io.zfits;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.MissingArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.EventUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

public class ZFitsDrsCalib implements Processor {
	static Logger log = LoggerFactory.getLogger(ZFitsDrsCalib.class);

	private short[] calibData;

	@Parameter(name = "outputKey", defaultValue = "DataCalibrated", required=true)
	private String outputKey;

	@Parameter(name = "optional", defaultValue = "false", required=false,
			description="If the file is a normal Fits File ignore the drs calibration of the zfits format.")
	private boolean optional = false;

	@Override
	public Data process(Data input) {
		if (optional)
			return input;
		EventUtils.mapContainsKeys(getClass(), input, "Data", "StartCellData");
		short[] data = ((short[])input.get("Data")).clone();
		short[] startCellData = (short[])input.get("StartCellData");
		
		int numSlices = 300;
		if (!input.containsKey("NROI"))
			log.warn("No ROI in the input data");
		else
			numSlices = ((Integer)input.get("NROI")).intValue();
		
		int numChannel= 1440;
		if (!input.containsKey("NPIX"))
			log.warn("No NPIX in the input data");
		else
			numChannel = ((Integer)input.get("NPIX")).intValue();
		
		//System.out.println("0: "+data[0]);
		try {
			applyDrsOffsetCalib(numSlices, numChannel, data, startCellData, this.calibData);
		} catch (IllegalArgumentException e) {
			log.error("Couldn't Calibrate Reason: "+e.getMessage());
			return null;
		}
		//System.out.println("0: "+data[0]);
		/*double[] floatData = new double[data.length];
		for (int i=0; i<floatData.length; i++) {
			floatData[i] = (float)data[i];
		}*/
		input.put(this.outputKey, data);
		return input;
	}

	public void applyDrsOffsetCalib(int numSlices, int numChannel, short[] data, short[] startCellData, short[] calibData) throws IllegalArgumentException {
		if (data==null || data.length != numSlices*numChannel)
			throw new IllegalArgumentException("The length of the data array is wrong.");
		if (startCellData==null || startCellData.length != numChannel)
			throw new IllegalArgumentException("The length of the startCellData should be the same as the number of Channel");
		if (calibData==null || calibData.length != 1024*numChannel)
			throw new IllegalArgumentException("The length of the calibData is not the same as 1024*numChannel");
		
		for (int ch=0; ch<numChannel; ch++) {
			// if the startCellData[ch] is negativ ignore the calibration step for the channel
			if (startCellData[ch]<0) {
				log.warn("Start Cell for channel : "+ch+"is negativ");
				continue;
			}
			//get the startCell
			int startCell = startCellData[ch];
			for (int sliceNum=0; sliceNum<numSlices; sliceNum++) {
				// the cells we look at are going roundabout
				int curCell = (startCell+sliceNum)%1024;
				
				data[ch*numSlices+sliceNum] += calibData[ ch*1024 + curCell ];
			}
		}
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey  = outputKey;
	}

	public void initDrsCellOffset(SourceURL url) throws Exception {
		log.info("Load DrsOffset");
		ZFitsStream drsStream = new ZFitsStream(url);
		drsStream.setTableName("ZDrsCellOffsets");
		try{
			drsStream.init();
		} catch (MissingArgumentException e) {
			if (this.optional)
				return;
			throw e;
		}
		Data item = drsStream.read();
		if (!item.containsKey("OffsetCalibration"))
			throw new NullPointerException("Missing OffsetCalibration");
		this.calibData = (short[])item.get("OffsetCalibration");
		//System.out.println("0: "+this.calibData[0]);
		//System.out.println("1: "+this.calibData[1]);
		//System.out.println("2: "+this.calibData[2]);
		if (this.calibData==null)
			throw new NullPointerException("Should not happen");
		log.info("Loaded");
	}

	@Parameter(description = "A URL to the DRS calibration data (in FITS formats)")
	public void setUrl(URL url) {
		log.info("Init DrsCellOffset Calibration");
		try {
			initDrsCellOffset(new SourceURL(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Parameter(description = "A String with a valid URL to the DRS calibration data (in FITS formats)")
	public void setUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			initDrsCellOffset(new SourceURL(url));
		} catch (MalformedURLException e){
			log.error("Malformed URL. The URL parameter of this processor has to a be a valid url");
			throw new RuntimeException("Cant open drsFile");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
}
