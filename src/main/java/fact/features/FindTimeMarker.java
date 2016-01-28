package fact.features;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class FindTimeMarker implements Processor {
	static Logger log = LoggerFactory.getLogger(FindTimeMarker.class);

    @Parameter(required = true)
    private String key = null;
    @Parameter(required = true)
    private String outputKey = null;
    @Parameter(required = true)
    private String timeOffsetKey = null;

    double[] posRisingEdges = null;
    double[] posFallingEdges = null;
    double[] durations = null;
    double[] maxHeights = null;
    double[] integrals = null;
    double[] averageHeights = null;
    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        double[] data;
        try{
            data = (double[]) input.get(key);
        } catch (ClassCastException e){
            log.error("Could not cast types." );
            throw e;
        }
        double[] timeOffsets;
        try{
            timeOffsets = (double[]) input.get(timeOffsetKey);
        } catch (ClassCastException e){
            log.error("Could not cast types." );
            throw e;
        }
        int numberTimeMarker = npix/9;
        
        posRisingEdges = new double[numberTimeMarker];
        posFallingEdges = new double[numberTimeMarker];
        durations = new double[numberTimeMarker];
        maxHeights = new double[numberTimeMarker];
        integrals = new double[numberTimeMarker];
        averageHeights = new double[numberTimeMarker];
        double[] offsetsRis = new double[numberTimeMarker];
        double[] offsetsFal = new double[numberTimeMarker];
        int roi = data.length / npix;
        
        for(int timemarker = 0 ; timemarker < numberTimeMarker; timemarker++){
            int pos = (9*timemarker + 8) * roi;
            
            int posRisingEdge = 0;
            int posFallingEdge = 0;
            double maxHeight = 0;
            double slope = 0;
            double integral = 0;
            int sl = 1;
            
            sl = roi - 51;
            
            for(; sl < roi && posRisingEdge == 0 ; sl++){
                slope = data[pos+sl] - data[pos+sl-1];
                if (slope > 50){
                    posRisingEdge = sl;
                }
            }
            if (posRisingEdge == 0)
            {
                log.warn("Rising Edge not found");
            }
            for(; sl < roi && posFallingEdge == 0 ; sl++){
                slope = data[pos+sl] - data[pos+sl-1];
                integral = integral + data[pos+sl];
                if (maxHeight < data[pos+sl]){
                    maxHeight = data[pos+sl];
                }
                if (slope < -50 && data[pos+sl]<150){
                    posFallingEdge = sl;
                }
            }
            if (posFallingEdge == 0)
            {
                log.warn("Falling Edge not found");
            }
            posRisingEdges[timemarker] = (double)posRisingEdge;
            posFallingEdges[timemarker] = (double)posFallingEdge;
            if (timeOffsets != null){
                posRisingEdges[timemarker] += timeOffsets[timemarker*roi + posRisingEdge];
                posFallingEdges[timemarker] += timeOffsets[timemarker*roi + posFallingEdge];
                offsetsRis[timemarker] = timeOffsets[timemarker*roi + posRisingEdge];
                offsetsFal[timemarker] = timeOffsets[timemarker*roi + posFallingEdge];
//              log.info(""+offsetsFal[timemarker]);
            }
            durations[timemarker] = posFallingEdge - posRisingEdge;
            maxHeights[timemarker] = maxHeight;
            integrals[timemarker] = integral;
            averageHeights[timemarker] = integral / durations[timemarker];
            
            
        }
        
        input.put(outputKey + "_risingEdges", posRisingEdges);
        input.put(outputKey + "_fallingEdges", posFallingEdges);
        input.put(outputKey + "_durations", durations);
        input.put(outputKey + "_maxHeights", maxHeights);
        input.put(outputKey + "_integrals", integrals);
        input.put(outputKey + "_averageHeights", averageHeights);
        input.put(outputKey + "_offsetRis", offsetsRis);
        input.put(outputKey + "_offsetFal", offsetsFal);
        
        return input;
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

    public double[] getPosRisingEdges() {
        return posRisingEdges;
    }

    public void setPosRisingEdges(double[] posRisingEdges) {
        this.posRisingEdges = posRisingEdges;
    }

    public double[] getPosFallingEdges() {
        return posFallingEdges;
    }

    public void setPosFallingEdges(double[] posFallingEdges) {
        this.posFallingEdges = posFallingEdges;
    }

    public double[] getDurations() {
        return durations;
    }

    public void setDurations(double[] durations) {
        this.durations = durations;
    }

    public double[] getMaxHeights() {
        return maxHeights;
    }

    public void setMaxHeights(double[] maxHeights) {
        this.maxHeights = maxHeights;
    }

    public double[] getIntegrals() {
        return integrals;
    }

    public void setIntegrals(double[] integrals) {
        this.integrals = integrals;
    }

    public double[] getAverageHeights() {
        return averageHeights;
    }

    public void setAverageHeights(double[] averageHeights) {
        this.averageHeights = averageHeights;
    }

    public String getTimeOffsetKey() {
        return timeOffsetKey;
    }

    public void setTimeOffsetKey(String timeOffsetKey) {
        this.timeOffsetKey = timeOffsetKey;
    }

}
