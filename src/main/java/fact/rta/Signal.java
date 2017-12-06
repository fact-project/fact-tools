package fact.rta;

import fact.auxservice.AuxiliaryService;
import org.jpmml.evaluator.ProbabilityDistribution;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Set;


/**
 * Created by kai on 24.01.16.
 */
public class Signal implements Processor {

    @Service(required = true)
    private  fact.PredictionService predictor;

    @Parameter
    private String signalClassName = "1";

    @Parameter
    private boolean rtamode = false;



    private static double thetaDegreesToThetaSquaredInMM(double theta){
        double pixelsize = 9.5;
        double fovPerPixel = 0.11;
        return Math.pow(theta*(fovPerPixel/pixelsize), 2);
    }


    @Override
    public Data process(Data data){
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
                data.put("background:thetasquare:" + offPosition , thetaDegreesToThetaSquaredInMM(thetaOffs[offPosition]));
            }

            ZonedDateTime eventTimeStamp = AuxiliaryService.unixTimeUTCToDateTime(data).
                    orElseThrow(() -> new IllegalArgumentException("No valid eventTimestamp in event."));

            try {
                if(rtamode && distribution.getProbability(signalClassName) > 0.5) {
                    WebSocketService.getService().updateEvent(ZonedDateTime.parse(eventTimeStamp.toString()), data);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while trying to update event.");
            }

        }
        return data;
    }
}
