package fact.datacorrection;

import fact.Constants;
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

/**
 * Applies DRS4 time calibration by using long_term_constants_median.time.drs.fits
 * <p>
 * The common reference point is the 976.5625 kHz DRS4 reference clock. This means the time
 * of sampling physical cell zero is assumed to be equal for all 160 DRS4 chips in the camera.
 * <p>
 * After finding the corrected sampling time of each sample in 'dataKey', the data is resampled
 * using a linearTimeCorrectionKernel, in order to obtain a time series which may be treated
 * as if the 2GHz DRS4 sampling process was flawless.
 * <p>
 * The resampling time series starts at the maximum corrected start time of all pixels and always
 * contains 300 samples. In case there are no more supporting points in the original time series,
 * i.e. in case we would need to extrapolate instead of interpolate, the last sample is repeated.
 * <p>
 * This means the end of the time series is likely to contain unphysical data. Most processors disregard the
 * end of the time series and therefor this might not matter a lot, but features like PedVar, which are
 * extracted at random positions of the timeline should be restricted to a timeframe between sample 10 and 250 I would
 * say.
 *
 * @author Dominik Neise &lt;neised@phys.ethz.ch&gt;
 */
public class DrsTimeCalibration implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(DrsCalibration.class);

    @Parameter(required = false, description = "Key of the StartCellData in the data fits file", defaultValue = "StartCellData")
    public String startCellKey = "StartCellData";

    @Parameter(required = false, description = "name of column in FITS file to find DRS4 time calibration constants.")
    public String drsTimeKey = "CellOffset";

    @Parameter(required = false, description = "file with the drs time calib constants", defaultValue = "classpath:/long_term_constants_median.time.drs.fits")
    public URL url = DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits");

    @Parameter(required = true, description = "key to the drs amplitude calibrated voltage curves")
    public String dataKey = null;

    @Parameter(required = true, description = "OutputKey for the calibrated voltage curves")
    public String outputKey = null;

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
    public Data process(Data item) {
        int roi = (Integer) item.get("NROI");
        short[] startCell = (short[]) item.get(startCellKey);

        if (startCell == null) {
            throw new RuntimeException("Couldn't find StartCellData");
        }

        double[] relativeTimeOffsets = new double[roi * Constants.N_PIXELS];
        for (int px = 0; px < Constants.N_PIXELS; px++) {
            int patch = px / 9;
            double offsetAtStartCell = absoluteTimeOffsets[patch * numberOfSlices + startCell[px]];
            for (int slice = 0; slice < roi; slice++) {
                int cell = patch * numberOfSlices + (slice + startCell[px]) % numberOfSlices;
                relativeTimeOffsets[px * roi + slice] = absoluteTimeOffsets[cell] - offsetAtStartCell;
            }
        }

        double[] data = (double[]) item.get(dataKey);
        roi = data.length / Constants.N_PIXELS;
        TimeCorrectionKernel tcKernel = new LinearTimeCorrectionKernel();

        double[] calibratedValues = new double[roi * Constants.N_PIXELS];
        for (int chid = 0; chid < Constants.N_PIXELS; chid++) {
            double[] realtimes = new double[roi];
            double[] values = new double[roi];

            for (int slice = 0; slice < roi; slice++) {
                realtimes[slice] = slice - relativeTimeOffsets[chid * roi + slice];
                values[slice] = data[chid * roi + slice];
            }
            tcKernel.fit(realtimes, values);

            for (int slice = 0; slice < roi; slice++) {
                calibratedValues[chid * roi + slice] = tcKernel.interpolate((double) slice);
            }

        }

        item.put(outputKey, calibratedValues);
        return item;
    }

    protected void loadDrsTimeCalibConstants(URL in) throws IOException {
        FITS fits = new FITS(in);
        BinTable calibrationTable = fits.getBinTableByName("DrsCellTimes").orElseThrow(() -> new RuntimeException("No Bintable with \"DrsCellTimes\""));

        BinTableReader reader = BinTableReader.forBinTable(calibrationTable);


        OptionalTypesMap<String, Serializable> row = reader.getNextRow();

        absoluteTimeOffsets = row.getDoubleArray(drsTimeKey).orElseThrow(() -> new RuntimeException(drsTimeKey + "is not in the File"));
    }


    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
