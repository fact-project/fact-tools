package fact.rta;

import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

/**
 * Use a regression model to estimate energy of the event in the stream.
 * Created by kai on 24.01.16.
 */
public class Energy implements Processor {

    @Service(required = true)
    fact.PredictionService predictor;


    @Override
    public Data process(Data data) {
        Double estimatedEnergy = predictor.estimate(data);
        if (estimatedEnergy != null){
            data.put("@energy", estimatedEnergy);
        }
        return data;
    }

}
