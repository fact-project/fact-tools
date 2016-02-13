package fact.rta;

import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;
import stream.expressions.version2.AbstractExpression;
import stream.expressions.version2.Condition;
import stream.expressions.version2.ConditionFactory;

/**
 * Created by kai on 24.01.16.
 */
public class Signal implements Processor {

    @Service(required = true)
    fact.PredictionService predictor;

    @Parameter
    String signalClassName = "1";

    @Override
    public Data process(Data data) {

        ProbabilityDistribution distribution = predictor.predict(data);
        if (distribution != null){
            data.put("@signal", distribution.getProbability(signalClassName));
        }
        return data;
    }
}
