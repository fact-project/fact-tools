package fact.rta;

import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;

/**
 * Created by kai on 24.01.16.
 */
public class Background implements Processor {

    @Parameter(required = true)
    fact.PredictionService predictor;

    @Parameter(required = false, description = "Prediction threshold")
    double predictionThreshold = 0.5;

    @Parameter
    String signalClassName = "1";


    Keys offKeys = new Keys("Theta_Off_?");
    String targetKey = "Theta";

    @Override
    public Data process(Data data) {
        int backgroundEvents = 0;

        Serializable originalValue = data.remove(targetKey);
        //loop over background regions and see if its a signal event.
        for (String key : offKeys.select(data)) {
            Serializable offValue = data.get(key);
            data.put(targetKey, offValue);

            ProbabilityDistribution distribution = predictor.predict(data);
            if (distribution != null && distribution.getProbability(signalClassName) > predictionThreshold){
                backgroundEvents += 1;
            }
        }
        //restore original state of the values
        data.put(targetKey, originalValue);
        data.put("@background", backgroundEvents);
        return data;
    }

    public void setPredictor(fact.PredictionService predictor) {
        this.predictor = predictor;
    }

    public void setPredictionThreshold(double predictionThreshold) {
        this.predictionThreshold = predictionThreshold;
    }

    public void setSignalClassName(String signalClassName) {
        this.signalClassName = signalClassName;
    }
}
