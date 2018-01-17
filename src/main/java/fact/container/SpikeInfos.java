package fact.container;

import fact.Constants;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpikeInfos implements Serializable {

    private static final long serialVersionUID = -7653148029952880947L;

    List<Integer> spPixel = new ArrayList<Integer>();
    List<Integer> spLogSlice = new ArrayList<Integer>();
    List<Integer> spPhysSpike = new ArrayList<Integer>();
    List<Double> spHeight = new ArrayList<Double>();
    List<Double> spTopSlope = new ArrayList<Double>();

    PixelSet spikesSet = new PixelSet();

    IntervalMarker[] spikeMarker = new IntervalMarker[Constants.N_PIXELS];

    public void addSpike(int px, int sl, short startCell, double spikeHeight, double averTopSlope) {

        spikesSet.addById(px);

        spPixel.add(px);
        spLogSlice.add(sl);
        spPhysSpike.add((sl + startCell) % 1024);
        spHeight.add(spikeHeight);
        spTopSlope.add(averTopSlope);

        spikeMarker[px] = new IntervalMarker(sl, sl + 1);
    }

    public void addInfosToDataItem(Data item, int spikeLength, String name) {
        int[] spPixelArr = new int[spPixel.size()];
        int[] spLogSliceArr = new int[spLogSlice.size()];
        int[] spPhysSliceArr = new int[spPhysSpike.size()];
        double[] spHeightArr = new double[spHeight.size()];
        double[] spTopSlopeArr = new double[spTopSlope.size()];
        for (int i = 0; i < spPixel.size(); i++) {
            spPixelArr[i] = spPixel.get(i);
            spLogSliceArr[i] = spLogSlice.get(i);
            spPhysSliceArr[i] = spPhysSpike.get(i);
            spHeightArr[i] = spHeight.get(i);
            spTopSlopeArr[i] = spTopSlope.get(i);
        }


        item.put(name + "N" + spikeLength, spPixelArr.length);
        item.put(name + "Pixel" + spikeLength, spPixelArr);
        item.put(name + "LogSlices" + spikeLength, spLogSliceArr);
        item.put(name + "PhysSlices" + spikeLength, spPhysSliceArr);
        item.put(name + "Heights" + spikeLength, spHeightArr);
        item.put(name + "TopSlope" + spikeLength, spTopSlopeArr);
        item.put(name + "Set" + spikeLength, spikesSet);
        item.put(name + "Marker" + spikeLength, spikeMarker);
    }

}
