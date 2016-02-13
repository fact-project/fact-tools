package fact.rta;

import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.Serializable;

/**
 * Created by kai on 24.01.16.
 */
public class Background implements Processor {

    @Service(required = true)
    fact.PredictionService predictor;

    @Parameter(required = false, description = "Prediction threshold")
    double predictionThreshold = 0.5;

    @Parameter
    String signalClassName = "1";


    Keys offKeys = new Keys("Theta_Off_?");
    String targetKey = "Theta";

    @Override
    public Data process(Data data) {
        double backgroundEvents = 0;

        Serializable originalValue = data.remove(targetKey);
        //loop over background regions and see if its a signal event.
        for (String key : offKeys.select(data)) {
            Serializable offValue = data.get(key);
            data.put(targetKey, offValue);

            ProbabilityDistribution distribution = predictor.predict(data);
            if (distribution != null){
                backgroundEvents += distribution.getProbability(signalClassName);
            }
        }
        //restore original state of the values
        data.put(targetKey, originalValue);
        data.put("@background", backgroundEvents);
        return data;
    }

}
