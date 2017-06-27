package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;

import stream.annotations.Parameter;
import sun.text.resources.JavaTimeSupplementary;

import java.time.*;

/**
 * This Processor converts the FACT UnixTime {seconds, microseconds} to a DateTime instance.
 * Be aware that this object has only miliseconds precision.
 * Created by maxnoe on 07.02.16.
 */
public class UnixTimeUTC2DateTime implements Processor {

    @Parameter(description = "Input key containing an int[2] array with  {seconds, microseconds}", defaultValue = "UnixTimeUTC", required = false)
    private String inputKey = "UnixTimeUTC";

    @Parameter(description = "Output key for the DateTime instance", defaultValue = "timestamp", required = false)
    private String outputKey = "timestamp";

    @Override
    public Data process(Data data) {

        Utils.mapContainsKeys(data, inputKey);

        int[] unixTimeUTC = (int[]) data.get(inputKey);
        data.put(outputKey, Utils.unixTimeUTCToZonedDateTime(unixTimeUTC));

        return data;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
