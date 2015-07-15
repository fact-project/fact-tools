package fact.tutorial;

import stream.Data;
import stream.Processor;

/**
 * Created by kai on 15.07.15.
 */
public class Size implements Processor {
    @Override
    public Data process(Data data) {
        double size = 0;
        int[] showerPixel = (int[]) data.get("showerPixel");
        double[] photons = (double[]) data.get("photons");

        for (Integer pixel: showerPixel){
            size += photons[pixel];
        }

        data.put("size", size);
        return data;
    }
}
