package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * If key refers to an int[] of showerpixel. this will calculate the number of islands
 * @author kaibrugge
 *
 */
public class NumberOfIslands implements Processor {

    @Parameter(required = true, description = "Key refering to an array of integer containing pixel Ids")
    private String key;

    @Parameter(required = true)
    private String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, int[].class);

        int[] showerPixel = (int[]) input.get(key);
        int numIslands = Utils.breadthFirstSearch(Utils.arrayToList(showerPixel)).size();
        input.put(outputKey, numIslands);
        return input;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public void setOutputKey(String outputkey) {
        this.outputKey = outputkey;
    }

}
