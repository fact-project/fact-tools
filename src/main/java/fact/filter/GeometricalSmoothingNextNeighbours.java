package fact.filter;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *@author Max Ahnen and Sebastian Mueller &lt;mknoetig@phys.ethz.ch&gt;
 *
 */
public class GeometricalSmoothingNextNeighbours implements Processor {
    static Logger log = LoggerFactory.getLogger(GeometricalSmoothingNextNeighbours.class);

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Parameter (required = true, description = "Weight ratio between the center pixel and its neighbour pixels", defaultValue = "1")
    private double centerToBorderRatio = 1;

    @Parameter (required = true, description = "The key to the double array to smooth")
    String key;

    @Parameter (required = true, description = "The outputKey to which the smoothed data will be written to the stream")
    String outputKey;
    
    private int npix;
    
    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        Utils.isKeyValid(item, "NPIX", Integer.class);
        double[] data = (double[]) item.get(key);
        npix = (Integer) item.get("NPIX");

        int roi = data.length / npix;
        double[] smoothedData= new double[data.length];
        //foreach pixel

        for(int slice = 0; slice < roi; slice++) {
            for(int pix = 0; pix < npix; pix++) {

                int pos = pix * roi + slice;
                //double pix_value = data[pos];

                FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);
                double buf = data[pos] * centerToBorderRatio;

                for(int neighbour = 0; neighbour < neighbours.length; neighbour++) {
                    buf+=data[ (neighbours[neighbour].chid * roi) + slice];
                }

                buf /= (neighbours.length + centerToBorderRatio);
                smoothedData[pos] = buf;
            }
        }

        item.put(outputKey, smoothedData);
        return item;
    }

    /*
     * Getter and Setter
     */
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

    public void setCenterToBorderRatio(double centerToBorderRatio) {
        this.centerToBorderRatio = centerToBorderRatio;
    }
}
