package fact.filter;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

// prototype for 2D image convolution. Specific convolutions may inherit from 
// this and need only to override getConvolution() with their specific needs

public class GeometricalConvolution implements Processor {
    static Logger log = LoggerFactory.getLogger(GeometricalConvolution.class);

    @Parameter (required = true,  description = "The key to the double array to be convoluted")
    String key;

    @Parameter (required = true,  description = "The outputKey to which the convoluted data will be written to the stream")
    String outputKey;
    
    protected int npix;
    protected double[] data;
    protected int roi;
    protected FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data item) {
        init(item);
        double[] convolutedData= new double[data.length];

        for(int slice = 0; slice < roi; slice++) {
            for(int pix = 0; pix < npix; pix++) {
                convolutedData[pixAndSlice2ArrayIndex(pix, slice)] = 
                    getConvolution(pix, slice);
            }
        }

        item.put(outputKey, convolutedData);
        return item;
    }

    protected void init(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        Utils.isKeyValid(item, "NPIX", Integer.class);  
        data = (double[]) item.get(key);
        npix = (Integer) item.get("NPIX");  

        roi = data.length / npix;   
    }

    protected double getConvolution(int pix, int slice) {
        // 2D default Convolution Kernel:
        //         ___
        //     ___/ 0 \___
        //    / 0 \___/ 0 \
        //    \___/ 1 \___/
        //    / 0 \___/ 0 \
        //    \___/ 0 \___/
        //        \___/
        return data[pixAndSlice2ArrayIndex(pix,slice)];    
    }

    protected int pixAndSlice2ArrayIndex(int pix, int slice) {
        return pix * roi + slice;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}