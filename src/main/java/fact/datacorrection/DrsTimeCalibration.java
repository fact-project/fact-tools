package fact.datacorrection;

import fact.hexmap.FactPixelMapping;
import fact.utils.LinearTimeCorrectionKernel;
import fact.io.hdureader.FITSStream;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import static com.google.common.primitives.Doubles.max;

/**
 * Applies DRS4 time calibration by using
 *  long_term_constants_median.time.drs.fits
 *
 *  The common reference point is the 976.5625 kHz DRS4 reference clock. This means the time
 *  of sampling physical cell zero is assumed to be equal for all 160 DRS4 chips in the camera.
 *
 *  After finding the corrected sampling time of each sample in 'dataKey', the data is resampled
 *  using a linearTimeCorrectionKernel, in order to obtain a time series which may be treated
 *  as if the 2GHz DRS4 sampling process was flawless.
 *
 * The resampling time series starts at the maximum corrected start time of all pixels and always
 * contains 300 samples. In case there are no more supporting points in the original time series,
 * i.e. in case we would need to extrapolate instead of interpolate, the last sample is repeated.
 *
 * This means the end of the time series is likely to contain unphysical data. Most processors disregard the
 * end of the time series and therefor this might not matter a lot, but features like PedVar, which are
 * extracted at random positions of the timeline should be restricted to a timeframe between sample 10 and 250 I would
 * say.
 *
 * @author Dominik Neise &lt;neised@phys.ethz.ch&gt;
 *
 */
public class DrsTimeCalibration implements StatefulProcessor{


    @Parameter(required = false, description = "", defaultValue = "The standard file provided in the jar")
    SourceURL url = new SourceURL(DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits"));


    @Parameter(required = false, description = "")
    String outputKey = "DataCalibrated";


    @Parameter(required = false, description = "")
    String dataKey = "DataCalibrated";

    @Parameter(required = false, description = "name of column in FITS file to find DRS4 time calibration constants.")
    private String drsTimeKey = "CellOffset";

    private Data drsTimeData;
    public double[][] true_sampling_time;

    private FactPixelMapping m;
    private LinearTimeCorrectionKernel linearTimeCorrectionKernel = new LinearTimeCorrectionKernel();

    /**
     * We save the sampling constants into a 2D Array once before the process starts.
     * The dimension is 2*1024 so we dont have to worry about overlaps in the ringbuffer.
     *
     * @param processContext
     * @throws Exception
     */
    @Override
    public void init(ProcessContext processContext) throws Exception {
        m = FactPixelMapping.getInstance();
        double[] absoluteTimeOffsets = loadDrsTimeCalibConstants(url);

        true_sampling_time = new double[160][2048];
        for(int chip = 0; chip < 160; chip++){
            System.arraycopy(absoluteTimeOffsets, chip*1024, true_sampling_time[chip], 0, 1024);
            System.arraycopy(absoluteTimeOffsets, chip*1024, true_sampling_time[chip], 1024, 1024);
        }
        for(int chip = 0; chip < 160; chip++){
            for(int i = 0; i < 2048; i++){
                true_sampling_time[chip][i] = i - true_sampling_time[chip][i];
            }
        }
    }


    @Override
    public Data process(Data data) {

        short[] startCells = (short[]) data.get("StartCellData");
        double[] dataCalibrated = (double[]) data.get(dataKey);
        int npix = 1440;
        int roi = 300;


        double [] timeCalibratedValues = new double[roi * npix];
        double [][] samplingTimes = new double[npix][roi];

        double [] firstSamplingTimes = new double[npix];

        //We want to get the latest sampling point at the start of the timeseries.
        for (int pix = 0; pix < npix; pix++){
            int chip = m.getPixelFromId(pix).drs_chip;

            System.arraycopy(true_sampling_time[chip], startCells[pix], samplingTimes[pix], 0, roi);

            firstSamplingTimes[pix]= samplingTimes[pix][0];
        }
        double maximumFirstSamplingTime = max(firstSamplingTimes);

        //at this point the samplingTimes array contains the 't' values for each entry in 'DataCalibrated'
        double[] currentPixelsDataCalibrated = new double[roi];
        for (int pix = 0; pix < npix; pix++){
            System.arraycopy(dataCalibrated, pix * roi, currentPixelsDataCalibrated, 0, roi);
            linearTimeCorrectionKernel.fit(samplingTimes[pix], currentPixelsDataCalibrated);
            for (int slice = 0; slice < roi; slice++){
                timeCalibratedValues[pix*roi + slice] = linearTimeCorrectionKernel.interpolate(maximumFirstSamplingTime + slice);
            }
        }

        data.put(outputKey, timeCalibratedValues);
        //data.put("firstSamplingTime", firstSamplingTimes );
        return data;
    }

    protected double[] loadDrsTimeCalibConstants(SourceURL  in) {
        try {

            FITSStream stream = new FITSStream(in);
            stream.init();
            drsTimeData = stream.readNext();

            if (!drsTimeData.containsKey(drsTimeKey))
            {
                throw new RuntimeException("Drs time data is missing key + " + drsTimeKey + "!");
            }
            return (double[]) drsTimeData.get(drsTimeKey);

        } catch (Exception e) {

            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void resetState() throws Exception {}

    @Override
    public void finish() throws Exception {

    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setDrsTimeKey(String drsTimeKey) {
        this.drsTimeKey = drsTimeKey;
    }

}
