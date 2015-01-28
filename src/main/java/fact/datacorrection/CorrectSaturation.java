package fact.datacorrection;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import static org.apache.commons.math3.util.FastMath.exp;

/**
 * Created by jbuss on 28.01.15.
 */
public class CorrectSaturation implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(required=true, description="")
    private String timeOverThresholdKey = null;

    @Parameter(required=true, description="")
    private String thresholdKey = null;

    @Parameter(required=true, description="")
    private String amplitudesKey = null;

    @Parameter(required=false)
    private int searchWindowLeft = 20;
    @Parameter(required=false)
    private int searchWindowRight = 30;

    private int npix;
    private double threshold = 1800.0;
    private double saturationThreshold = 1900.0;
    private double estBaseline = 0.0;
    private int nBslSlices = 40;

    @Override
    public Data process(Data input) {

        if (searchWindowLeft >= searchWindowRight)
        {
            throw new RuntimeException("searchWindowLeft is equal or larger than searchWindowRight: "+searchWindowLeft+" >= "+searchWindowRight);
        }

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.isKeyValid(input, timeOverThresholdKey, int[].class);
        Utils.isKeyValid(input, thresholdKey, Double.class);
        Utils.mapContainsKeys(input, dataKey, thresholdKey, amplitudesKey, "NPIX");

        npix = (Integer) input.get("NPIX");
        threshold = (Double) input.get(thresholdKey);

        int[] timeOverThresholdArray 	 = (int[]) input.get(timeOverThresholdKey);
        double[] amplitudes = (double[]) input.get(amplitudesKey);
        int roi = (Integer) input.get("NROI");

        //get array with time series
        double[] data = (double[]) input.get(dataKey);

        for(int pix = 0 ; pix < npix; pix++){
            int firstSlice = roi*pix;
            int lastSlice  = roi*(pix+1);

            //check if Amplitude fullfills saturation cirterion
            if (amplitudes[pix] < saturationThreshold){
                continue;
            }

            //estimate baseline
            for (int slice = firstSlice+searchWindowLeft; slice < firstSlice+searchWindowLeft+nBslSlices; slice++){
                estBaseline += data[slice];
            }
            estBaseline /= nBslSlices;

            //get width
            int width = timeOverThresholdArray[pix];

            //estimate amplitude of saturated pulse (this formular is from MTreatSaturation.cc in Mars_Trunk Revision: 18096)
            Double estAmplitude = (threshold-estBaseline)/(0.898417 - 0.0187633*width + 0.000163919*width*width - 6.87369e-7*width*width*width + 1.13264e-9*width*width*width*width);

            //estimate arrivaltime
            int estArrivalTime =
            for (int slice = firstSlice; slice < lastSlice; slice++){
                Int arrivalTime  = slice - estArrivalTime,
                Double amplitude = estAmplitude*(1-1/(1+exp(x/2.14)))*exp(-x/38.8)+estBaseline;
                if (amplitude > data[slice]){
                    data[slice] = amplitude;
                }
            }

        }


        return input;
    }
}
