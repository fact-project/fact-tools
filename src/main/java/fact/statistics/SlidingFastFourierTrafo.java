package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class SlidingFastFourierTrafo implements Processor {
    static Logger log = LoggerFactory.getLogger(SlidingFastFourierTrafo.class);

    @Parameter(required = true)
    String key = null;

    @Parameter(required = true)
    String outputKey = null;

    int lengthForFFT = 128;

    int stepSize = 32;

    FastFourierTransformer fftObject = new FastFourierTransformer(DftNormalization.STANDARD);

    private int npix;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");
        Utils.mapContainsKeys(item, key);

        double[] data = (double[]) item.get(key);
        int roi = data.length / npix;
        double[] result = new double[data.length];


        for (int px = 0; px < npix; px++) {
            for (int slBegin = 0; slBegin < roi; slBegin += stepSize) {
                double[] arrayForFFT = new double[lengthForFFT];
                int sl = 0;
                for (; sl < lengthForFFT && (sl + slBegin) < roi; sl++) {
                    arrayForFFT[sl] = data[px * roi + slBegin + sl];
                }
                for (; sl < lengthForFFT; sl++) {
                    arrayForFFT[sl] = 0;
                }
                Complex[] resultWindow = fftObject.transform(arrayForFFT, TransformType.INVERSE);
                for (sl = 1; sl < stepSize && (sl + slBegin) < roi; sl++) {
                    double real = resultWindow[sl].getReal();
                    double imag = resultWindow[sl].getImaginary();
                    result[px * roi + slBegin + sl] = Math.sqrt(real * real + imag * imag);
                }
            }
        }
        item.put(outputKey, result);

        return item;
    }
}
