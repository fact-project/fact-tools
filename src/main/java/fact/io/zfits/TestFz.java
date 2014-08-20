package fact.io.zfits;

import fact.io.FitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;

public class TestFz implements Processor {
	static Logger log = LoggerFactory.getLogger(TestFz.class);

	private FitsStream compareStream = null;
	private int currentEvent = 0;

	@Override
	public Data process(Data input) {
		Data inputCompare;
		try {
			inputCompare = this.compareStream.readNext();
		} catch (Exception e) {
			log.error("Compare stream fail: {}", e.getMessage());
			e.printStackTrace();
			throw new RuntimeException();
		}

		short[] data = ((short[])input.get("DataZCal")).clone();
		short[] dataCompare = ((short[])inputCompare.get("Data")).clone();
		short[] startCellData = (short[])input.get("StartCellData");
		short[] startCellDataCompare = (short[])inputCompare.get("StartCellData");
		
		int numSlices = ((Integer)input.get("NROI")).intValue();
		int numSlicesCompare = ((Integer)inputCompare.get("NROI")).intValue();
		if (numSlices!=numSlicesCompare) {
			throw new RuntimeException("Slice number are diffrent. Event: "+this.currentEvent);
		}
		
		int numChannel = ((Integer)input.get("NPIX")).intValue();
		int numChannelCompare = ((Integer)inputCompare.get("NPIX")).intValue();
		if (numChannel!=numChannelCompare) {
			throw new RuntimeException("Channel number are diffrent. Event: "+this.currentEvent);
		}

		//check startData
		for (int i=0; i<numChannel; i++) {
			if (startCellData[i]!=startCellDataCompare[i]) {
				log.error("Event: {}, StartCellData for channel: {} is wrong, got {} expected {}", this.currentEvent, i, startCellData[i], startCellDataCompare[i]);
				throw new RuntimeException();
			}
		}
		//check data
		for (int i=0; i<numChannel; i++) {
			for (int j=0; j<numSlices; j++) {
				int index = i*numSlices+j;
				if (data[index]!=dataCompare[index]) {
					log.error("Event: {}, Cell: {}, slice {}, is wrong, got {} expected {}, startCell: {}", this.currentEvent, i, j, data[index], dataCompare[index], startCellData[i]);
					throw new RuntimeException();
				}
			}
		}
		this.currentEvent++;
		return input;
	}
	
	public void initCompareDataFile(SourceURL url) throws Exception {
		this.compareStream = new FitsStream(url);
		this.compareStream.init();
	}

	@Parameter(required=true, description = "A URL to the DRS calibration data (in FITS formats)")
	public void setUrl(URL url) {
		try {
			initCompareDataFile(new SourceURL(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
