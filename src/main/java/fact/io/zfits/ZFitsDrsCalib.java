package fact.io.zfits;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.io.FitsStream;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

public class ZFitsDrsCalib implements Processor {
	static Logger log = LoggerFactory.getLogger(ZFitsDrsCalib.class);

	private short[] calibData;

	private String outputKey = "Data";

	@Override
	public Data process(Data input) {
		if (!input.containsKey("Data") || !input.containsKey("StartCellData")) {
			log.error("Can't calibrate data missing 'Data' or 'StartCellData'");
			return null;
		}
		short[] data = ((short[])input.get("Data")).clone();
		short[] startCellData = (short[])input.get("StartCellData");
		
		int numSlices = 300;
		if (!input.containsKey("NROI"))
			log.warn("No ROI in the input data");
		else
			numSlices = (Integer)input.get("NROI");
		
		int numChannel= 1440;
		if (!input.containsKey("NPIX"))
			log.warn("No NPIX in the input data");
		else
			numChannel = (Integer)input.get("NPIX");
		
		try {
			applyDrsOffsetCalib(numSlices, numChannel, data, startCellData, this.calibData);
		} catch (IllegalArgumentException e) {
			log.error("Couldn't Calibrate Reason: "+e.getMessage());
			return null;
		}
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

	@Parameter(name = "outputKey", defaultValue = "DataCalibrated", required = false)
	public void setOutputKey(String outputKey) {
		this.outputKey  = outputKey;
	}

	public void initDrsCellOffset(SourceURL url) throws Exception {
		/*ZFitsStream drsStream = new ZFitsStream(url);
		drsStream.setTableName("ZDrsCellOffsets");
		drsStream.init();
		Data item = drsStream.read();*/
		log.info("Load DrsOffset");
		FitsStream drsStream = new FitsStream(url);
		drsStream.init();
		Data item = drsStream.read();
		if (!item.containsKey("OffsetCalibration"))
			throw new NullPointerException("Missing OffsetCalibration");
		this.calibData = (short[])item.get("OffsetCalibration");
		if (this.calibData==null)
			throw new NullPointerException("Should not happen");
		log.info("Loaded");
	}

	@Parameter(description = "A URL to the DRS calibration data (in FITS formats)")
	public void setUrl(URL url) {
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
}
