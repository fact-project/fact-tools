package fact.utils;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by maxnoe on 08.08.16.
 */
public class BoardTimeToSeconds implements Processor {

    @Parameter(defaultValue = "BoardTime", description = "Key for the FAD Board clock counter values")
    private String boardTimeKey = "BoardTime";

    @Parameter(defaultValue = "EventTime", description = "outputKey for the event time in seconds relative to the first Event")
    private String outputKey = "eventTime";

    private int[] t0 = null;

    public double secondsPerCount = 100e-6; // according to Dom there are 100 microseconds per count


    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, boardTimeKey);

        int[] boardTime = (int[]) item.get(boardTimeKey);
        // According to Dom, Max Ahnen said that we gain time resolution if we take the mean of the
        // board counters. We have to subtract the initial value of the counters, to make them comparable
        if (t0 ==  null){
            t0 = new int[boardTime.length];
            for (int i=0; i < t0.length; i++){
                t0[i] = boardTime[i];
            }
        }

        double time = 0.0;
        for (int i=0; i < boardTime.length; i++){
            // one count of the counter is equivalent to 100 microseconds
            time += (boardTime[i] - t0[i]) * 100e-6;
        }
        time /= boardTime.length;

        item.put(outputKey, time);
        return item;
    }

    public void setBoardTimeKey(String boardTimeKey) {
        this.boardTimeKey = boardTimeKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
