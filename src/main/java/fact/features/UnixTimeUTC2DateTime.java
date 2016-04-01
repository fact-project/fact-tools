package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import stream.annotations.Parameter;

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

        long seconds = unixTimeUTC[0];
        long microseconds = unixTimeUTC[1];

        DateTime date = new DateTime((long) (seconds * 1000.0 + microseconds / 1000.0),  DateTimeZone.UTC);
        data.put(outputKey, date);

        return data;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
