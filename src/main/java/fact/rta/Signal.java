package fact.rta;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.Closest;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jpmml.evaluator.ProbabilityDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.*;
import stream.annotations.Parameter;
import stream.annotations.Service;


import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;


/**
 * Created by kai on 24.01.16.
 */
public class Signal implements Processor {
    private Logger log = LoggerFactory.getLogger(Signal.class);

    @Service(required = true)
    fact.PredictionService predictor;

    @Parameter
    String signalClassName = "1";

    @Service(required = true)
    RTAWebService webService;

    @Service(required = true)
    AuxiliaryService auxService;

    public static double thetaDegreesToThetaSquaredInMM(double theta){
        double pixelsize = 9.5;
        double fovPerPixel = 0.11;
        return Math.pow(theta*(fovPerPixel/pixelsize), 2);
    }


    @Override
    public Data process(Data data)  {

        ProbabilityDistribution distribution = predictor.predict(data);
        if (distribution != null){

            //put signal prediction and thetsquared into data item
            data.put("signal:prediction", distribution.getProbability(signalClassName));
            double theta = (double) data.get("Theta");
            data.put("signal:thetasquare", thetaDegreesToThetaSquaredInMM(theta));

            //put thetsquared for each offposition into the data item
            Set<String> offKeys = new Keys("Theta_Off_?").select(data);
            double[] thetaOffs = offKeys.stream().mapToDouble(s -> (double) data.get(s)).toArray();

            for (int offPosition = 0; offPosition < offKeys.size(); offPosition++) {
                data.put("background:thetasquare:"+offPosition , thetaDegreesToThetaSquaredInMM(thetaOffs[offPosition]));
            }

            DateTime eventTimeStamp = AuxiliaryService.unixTimeUTCToDateTime(data).
                    orElseThrow(() -> new IllegalArgumentException("No valid eventTimestamp in event."));


            webService.updateEvent(eventTimeStamp, data);

        }
        return data;
    }
}
