package fact.datacorrection;

import fact.Utils;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import fact.utils.LinearTimeCorrectionKernel;
import fact.utils.TimeCorrectionKernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;


public class DrsTimeCalibration implements StatefulProcessor{
	static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

	@Parameter(required=false, description="Key of the StartCellData in the data fits file",defaultValue="StartCellData")
	private String startCellKey = "StartCellData";

	@Parameter(required=false, description="name of column in FITS file to find DRS4 time calibration constants.")
	private String drsTimeKey = "CellOffset";

	@Parameter(required = false, description = "file with the drs time calib constants", defaultValue="classpath:/long_term_constants_median.time.drs.fits")
	private URL url = DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits");

	@Parameter(required = true, description = "key to the drs amplitude calibrated voltage curves")
	private String dataKey = null;

	@Parameter(required = true, description = "OutputKey for the calibrated voltage curves")
	private String outputKey = null;

	private int numberOfSlices = 1024;
	private int numberOfTimeMarker = 160;

	private double[] absoluteTimeOffsets = new double[numberOfSlices * numberOfTimeMarker];


	@Override
	public void init(ProcessContext context) {
		try {
			loadDrsTimeCalibConstants(url);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, "NPIX", Integer.class);

		int npix = (Integer) input.get("NPIX");
		int roi = (Integer) input.get("NROI");
		short[] startCell = (short[]) input.get(startCellKey);

		if (startCell == null) {
			throw new RuntimeException("Couldn't find StartCellData");
		}

		double[] relativeTimeOffsets = new double[roi* npix];
		for (int px = 0; px < npix; px++){
			int patch = px / 9;
			double offsetAtStartCell = absoluteTimeOffsets[patch*numberOfSlices + startCell[px]];
			for (int slice = 0 ; slice < roi ; slice++){
				int cell = patch*numberOfSlices + (slice + startCell[px])%numberOfSlices;
				relativeTimeOffsets[px * roi + slice] = absoluteTimeOffsets[cell] - offsetAtStartCell;
			}
		}

		npix = (Integer) input.get("NPIX");
		double[] data = (double[]) input.get(dataKey);
		roi = data.length / npix;
		TimeCorrectionKernel tcKernel = new LinearTimeCorrectionKernel();

		double [] calibratedValues = new double[roi * npix];
		for(int chid = 0; chid < npix; chid++)
		{
			double [] realtimes = new double[roi];
			double [] values = new double[roi];

			for(int slice = 0; slice < roi; slice++)
			{
				realtimes[slice] = slice - relativeTimeOffsets[chid * roi + slice];
				values[slice] = data[chid * roi + slice];
			}
			tcKernel.fit(realtimes, values);

			for(int slice = 0; slice < roi; slice++)
			{
				calibratedValues[chid * roi + slice] = tcKernel.interpolate((double) slice);
			}

		}

		input.put(outputKey, calibratedValues);
		return input;
	}

	protected void loadDrsTimeCalibConstants(URL  in) throws IOException {
		FITS fits = new FITS(in);
		BinTable calibrationTable = fits.getBinTableByName("DrsCellTimes").orElseThrow(() -> new RuntimeException("No Bintable with \"DrsCellTimes\""));

		BinTableReader reader = BinTableReader.forBinTable(calibrationTable);


		OptionalTypesMap<String, Serializable> row = reader.getNextRow();

		absoluteTimeOffsets = row.getDoubleArray(drsTimeKey).orElseThrow(()->new RuntimeException(drsTimeKey+"is not in the File"));
	}


	@Override
	public void resetState() throws Exception {

	}

	@Override
	public void finish() throws Exception {

	}
}
