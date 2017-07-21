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

import java.io.Serializable;
import java.net.URL;


public class DrsTimeCalibration implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

	@Parameter(required=false,description="Key of the StartCellData in the data fits file",defaultValue="StartCellData")
	private String startCellKey = "StartCellData";
	@Parameter(required=true,description="Key of the time calibration constants (relative to the start cell of each pixel)")
	private String outputKey = null;
	@Parameter(required=false, description="name of column in FITS file to find DRS4 time calibration constants.")
	private String drsTimeKey = "CellOffset";
	@Parameter(required = false, description = "file with the drs time calib constants", defaultValue="classpath:/long_term_constants_median.time.drs.fits")
	private URL url = DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits");

	private int numberOfSlices = 1024;
	private int numberOfTimemarker = 160;

	private int npix;


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

	protected void loadDrsTimeCalibConstants(URL  in) {
		try {

			FITS fits = new FITS(in);
			BinTable calibrationTable = fits.getBinTableByName("DrsCellTimes").orElseThrow(() -> new RuntimeException("No Bintable with \"DrsCellTimes\""));

			BinTableReader reader = BinTableReader.forBinTable(calibrationTable);


			OptionalTypesMap<String, Serializable> row = reader.getNextRow();

			absoluteTimeOffsets = row.getDoubleArray(drsTimeKey).orElseThrow(()->new RuntimeException(drsTimeKey+"is not in the File"));


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
