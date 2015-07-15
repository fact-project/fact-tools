package fact.tutorial;

import fact.hexmap.ui.overlays.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.HashSet;

/**
 * Created by kai on 15.07.15.
 */
public class Cleaning implements Processor {

    @Parameter(required = false, description = "The threshold for selecting pixels. Every pixel with a value > threshold" +
            "will be selected")
    private double threshold = 15.0;


    @Override
    public Data process(Data item) {

        double[] photons = (double[]) item.get("photons");
        HashSet<Integer> selectedPixel = new HashSet<>();

        for (int pixel = 0; pixel < 1440; pixel++) {
            if(photons[pixel] > threshold){
                selectedPixel.add(pixel);
            }
        }

        item.put("showerPixel", selectedPixel);
        item.put("@overlay", new PixelSetOverlay(selectedPixel));
        return item;
    }
}
