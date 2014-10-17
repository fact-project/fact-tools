package fact.filter;

import fact.Constants;
import fact.Utils;
import fact.io.FitsStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;


public class DrsTimeCalibration implements Processor {
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);
	
	@Parameter(required=false,description="Key of the StartCellData in the data fits file",defaultValue="StartCellData")
	private String startCellKey = "StartCellData";
	@Parameter(required=true,description="Key of the time calibration constants (relative to the start cell of each pixel)")
	private String outputKey = null;
	
	private int numberOfSlices = 1024;
	private int numberOfTimemarker = 160;

	private String drsTimeKey = "COL";
	
	Data drsTimeData = null;
	private double[] absoluteTimeOffsets = new double[numberOfSlices*numberOfTimemarker];
	
	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys(input, startCellKey, "NROI");
		
		short[] startCell = (short[]) input.get(startCellKey);
		if (startCell==null)
		{
			log.info("Couldn't find StartCellData");
		}
		int roi = (Integer) input.get("NROI");
		double[] relativeTimeOffsets = new double[roi*Constants.NUMBEROFPIXEL];
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++){
			int timemarker = px / 9;
			double offsetAtStartCell = absoluteTimeOffsets[timemarker*numberOfSlices + startCell[px]];
			for (int slice = 0 ; slice < roi ; slice++){
				int cell = timemarker*numberOfSlices + (slice + startCell[px])%numberOfSlices;
				relativeTimeOffsets[px*roi+slice] = absoluteTimeOffsets[cell]-offsetAtStartCell;
			}
		}
		
		input.put(outputKey, relativeTimeOffsets);
		return input;
	}
		
	protected void loadDrsTimeCalibConstants(SourceURL  in) {
		try {

			FitsStream stream = new FitsStream(in);
			stream.init();
			drsTimeData = stream.readNext();
			log.debug("Read DRS Time data: {}", drsTimeData);
			
			if (!drsTimeData.containsKey(drsTimeKey))
			{
				throw new RuntimeException("Drs time data is missing key + " + drsTimeKey + "!");
			}
			this.absoluteTimeOffsets = (double[]) drsTimeData.get(drsTimeKey);

		} catch (Exception e) {

			log.error("Failed to load DRS data: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();
			this.absoluteTimeOffsets = null;

			throw new RuntimeException(e.getMessage());
		}
	}

	public String getStartCellKey() {
		return startCellKey;
	}

	public void setStartCellKey(String startCellKey) {
		this.startCellKey = startCellKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	public int getNumberOfSlices() {
		return numberOfSlices;
	}

	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	public int getNumberOfTimemarker() {
		return numberOfTimemarker;
	}

	public void setNumberOfTimemarker(int numberOfTimemarker) {
		this.numberOfTimemarker = numberOfTimemarker;
	}

	public void setUrl(URL url) {
		try {
			loadDrsTimeCalibConstants(new SourceURL(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
