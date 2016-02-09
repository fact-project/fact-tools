package fact.filter;

import fact.Utils;
import fact.datacorrection.DrsCalibration;
import fact.io.FitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;


public class DrsTimeCalibration implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);
	
	@Parameter(required=false, defaultValue="StartCellData",
			description="Key of the StartCellData in the data fits file")
	private String startCellKey = "StartCellData";
	@Parameter(required=true, defaultValue="meta:timeCalibConst",
			description="Key of the time calibration constants (relative to the start cell of each pixel)")
	private String outputKey = "meta:timeCalibConst";
	@Parameter(required=false, defaultValue="CellOffset",
			description="name of column in FITS file to find DRS4 time calibration constants.")
	private String drsTimeKey = "CellOffset";
	@Parameter(required = false, defaultValue="classpath:/long_term_constants_median.time.drs.fits",
			description = "file with the drs time calib constants")
	private SourceURL url = new SourceURL(
			DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits"));

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
	public Data process(Data item) {
		Utils.isKeyValid(item, "NPIX", Integer.class);
		Utils.mapContainsKeys(item, startCellKey, "NROI");
		
		npix = (Integer) item.get("NPIX");
		int roi = (Integer) item.get("NROI");
		short[] startCell = (short[]) item.get(startCellKey);
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
		
		item.put(outputKey, relativeTimeOffsets);
		return item;
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

	@Override
	public void resetState() throws Exception {

	}

	@Override
	public void finish() throws Exception {

	}
}
