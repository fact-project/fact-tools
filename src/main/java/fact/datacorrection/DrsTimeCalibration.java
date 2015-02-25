package fact.datacorrection;

import fact.hexmap.FactPixelMapping;
import fact.io.FitsStream;
import fact.utils.LinearTimeCorrectionKernel;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import static com.google.common.primitives.Doubles.max;

/**
 * Created by Kai on 11.02.15.
 */
public class DrsTimeCalibration implements StatefulProcessor{


    @Parameter(required = false, description = "", defaultValue = "The standard file provided in the jar")
    SourceURL url = null;


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

            FitsStream stream = new FitsStream(in);
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
}
