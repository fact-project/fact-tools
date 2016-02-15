package fact.rta;

import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.expressions.version2.AbstractExpression;
import stream.expressions.version2.Condition;
import stream.expressions.version2.ConditionFactory;

/**
 * Created by kai on 24.01.16.
 */
public class Signal implements Processor {

    @Parameter(required = true)
    fact.PredictionService predictor;

    @Parameter(required = false, description = "Prediction threshold")
    double predictionThreshold = 0.5;

    @Parameter
    String signalClassName = "1";

    @Override
    public Data process(Data data) {

        ProbabilityDistribution distribution = predictor.predict(data);
        if (distribution != null){
            data.put("signal:prediction", distribution.getProbability(signalClassName));
        }
        return data;
    }
}
