package fact.filter;

import fact.Utils;
import fact.datacorrection.DrsCalibration;
import fact.io.hdureader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.SourceURL;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class DrsTimeCalibration implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

	@Parameter(required=false,description="Key of the StartCellData in the data fits file",defaultValue="StartCellData")
	private String startCellKey = "StartCellData";
	@Parameter(required=true,description="Key of the time calibration constants (relative to the start cell of each pixel)")
	private String outputKey = null;
	@Parameter(required=false, description="name of column in FITS file to find DRS4 time calibration constants.")
	private String drsTimeKey = "CellOffset";
	@Parameter(required = false, description = "file with the drs time calib constants", defaultValue="classpath:/long_term_constants_median.time.drs.fits")
	private SourceURL url = new SourceURL(DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits"));

	private Map<String, Serializable> fitsHeader = new HashMap<>();
	private Reader reader;
	private int numberOfSlices = 1024;
	private int numberOfTimemarker = 160;

	private int npix;


	Data drsTimeData = null;
	private double[] absoluteTimeOffsets = new double[numberOfSlices*numberOfTimemarker];


	@Override
	public void init(ProcessContext context) throws Exception {
		try {
			loadDrsTimeCalibConstants(url);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, "NPIX", Integer.class);
		Utils.mapContainsKeys(input, startCellKey, "NROI");

		npix = (Integer) input.get("NPIX");
		int roi = (Integer) input.get("NROI");
		short[] startCell = (short[]) input.get(startCellKey);
		if (startCell==null) {
			throw new RuntimeException("Couldn't find StartCellData");
		}
		double[] relativeTimeOffsets = new double[roi*npix];
		for (int px = 0 ; px < npix ; px++){
			int patch = px / 9;
			double offsetAtStartCell = absoluteTimeOffsets[patch*numberOfSlices + startCell[px]];
			for (int slice = 0 ; slice < roi ; slice++){
				int cell = patch*numberOfSlices + (slice + startCell[px])%numberOfSlices;
				relativeTimeOffsets[px*roi+slice] = absoluteTimeOffsets[cell]-offsetAtStartCell;
			}
		}

		input.put(outputKey, relativeTimeOffsets);
		return input;
	}

	protected void loadDrsTimeCalibConstants(SourceURL  in) {
		try {
			URL url=new URL(in.getProtocol(),in.getHost(),in.getPort(),in.getFile());
			FITS fits = new FITS(url);
			HDU eventHDU = fits.getHDU("DrsCellTimes");
			if (eventHDU!=null) {
				BinTable eventsTable = eventHDU.getBinTable();

				//read each headerline and try to get the right datatype
				//from smallest to largest datatype
				//if no number can be found simply save the string.
				Header header = eventHDU.header;
				fitsHeader = header.asMapOfSerializables();

				Boolean ztable = eventHDU.header.getBoolean("ZTABLE").orElse(false);
				if (ztable) {
					reader = ZFITSHeapReader.forTable(eventsTable);
				} else {
					reader = BinTableReader.forBinTable(eventsTable);
				}
			}
			if(!reader.hasNext()){
				drsTimeData= null;
			}

			OptionalTypesMap<String, Serializable> nextRow = reader.getNextRow();
			Data item = DataFactory.create(nextRow);
			item.putAll(fitsHeader);

			drsTimeData= item;
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

	public void setStartCellKey(String startCellKey) {
		this.startCellKey = startCellKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setNumberOfSlices(int numberOfSlices) {
		this.numberOfSlices = numberOfSlices;
	}

	public void setNumberOfTimemarker(int numberOfTimemarker) {
		this.numberOfTimemarker = numberOfTimemarker;
	}

	public void setUrl(SourceURL url) {
		this.url = url;
	}

	public void setDrsTimeKey(String drsTimeKey) {
		this.drsTimeKey = drsTimeKey;
	}


	@Override
	public void resetState() throws Exception {

	}

	@Override
	public void finish() throws Exception {

	}
}
