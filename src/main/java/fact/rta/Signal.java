package fact.rta;

import fact.auxservice.AuxFileService;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;

import fact.auxservice.AuxiliaryServiceName;
import org.joda.time.DateTime;

import org.joda.time.format.DateTimeFormatter;
import org.jpmml.evaluator.ProbabilityDistribution;
import stream.*;
import stream.annotations.Parameter;
import stream.annotations.Service;


import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.SortedSet;


/**
 * Created by kai on 24.01.16.
 */
public class Signal implements Processor {

    @Service(required = true)
    private   fact.PredictionService predictor;

    @Parameter
    private String signalClassName = "1";

    @Service(required = true)
    private  RTAWebService webService;

    @Service(required = true)
    private AuxFileService auxService;


    private static double thetaDegreesToThetaSquaredInMM(double theta){
        double pixelsize = 9.5;
        double fovPerPixel = 0.11;
        return Math.pow(theta*(fovPerPixel/pixelsize), 2);
    }


    @Override
    public Data process(Data data){
        DateTime dT = AuxiliaryService
                .unixTimeUTCToDateTime(data)
                .orElseThrow(() -> new RuntimeException("Could not get time stamp from current event."));

        SortedSet<AuxPoint> points;
        try {
            points = auxService.getAuxiliaryDataForWholeNight(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, dT);
        } catch (IOException e) {
            throw new RuntimeException("Could not get auxPoints from file " + e.getMessage());
        }


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




            webService.updateEvent(OffsetDateTime.parse(eventTimeStamp.toString()), data, points);

        }
        return data;
    }
}
