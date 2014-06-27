package fact.filter;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;

public class DrsTimeCalibration implements Processor {
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);
	
	private String startCellKey = "StartCellData";
	private String outputKey = null;
	
	private int numberOfSlices = 1024;
	private int numberOfTimemarker = 160;
	
	Data drsTimeData = null;
	
	private double[] absoluteTimeOffsets = new double[numberOfSlices*numberOfTimemarker];
	
	@Override
	public Data process(Data input) {
		
		
		
		short[] startCell = (short[]) input.get("StartCellData");
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
		// TODO Auto-generated method stub
		return input;
	}
	
	// executed in this.setTimeCalibConstantsFileName
	private void loadDrsTimeCalibConstants(SourceURL inputUrl){
		try {
			CsvStream stream = new CsvStream(inputUrl, " ");
			stream.setHeader(false);
			stream.init();
			drsTimeData = stream.readNext();
			
			for (int i = 0 ; i < numberOfSlices*numberOfTimemarker ; i++){
				String key = "column:" + (i+1);
				this.absoluteTimeOffsets[i] = (Double) drsTimeData.get(key);
			}
			
		} catch (Exception e) {
			log.error("Failed to load time DRS data: {}", e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
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
