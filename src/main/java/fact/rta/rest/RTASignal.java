package fact.rta.rest;


import fact.rta.RTAWebService;
import fact.rta.db.FACTRun;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Set;

public class RTASignal {

    final private static Logger log = LoggerFactory.getLogger(RTASignal.class);
    // our binding annotation
    @BindingAnnotation(BindSignal.SignalBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindSignal
    {

        class SignalBinderFactory implements BinderFactory
        {
            public Binder build(Annotation annotation)
            {
                return (Binder<BindSignal, RTASignal>) (q, bind, s) -> {
                    try {
                        q.bind("night", s.run.night );
                        q.bind("run_id", s.run.runID);
                        Field[] fields = s.getClass().getDeclaredFields();
                        for (Field f : fields){
                            if(!f.getName().equals("run")) {
                                q.bind(f.getName(), f.get(s));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        log.error("Could not access field value in statement: " + q.toString());
                    }
                };
            }
        }
    }

    public final FACTRun run;
    public final double prediction;
    public final double theta_off_1;
    public final double theta_off_2;
    public final double theta_off_3;
    public final double theta_off_4;
    public final double theta_off_5;
    public final double theta;
    public final DateTime eventTimestamp;
    public final DateTime analysisTimestamp;

    public double onTimePerEvent;

    public RTASignal(DateTime eventTimestamp, DateTime analysisTimestamp, Data item, FACTRun run) {

        this.run = run;
        this.theta = (double) item.get("Theta");
        Set<String> offKeys = new Keys("Theta_Off_?").select(item);
        double[] thetaOffs = offKeys.stream().mapToDouble(s -> (double) item.get(s)).toArray();
        this.theta_off_1 = thetaOffs[0];
        this.theta_off_2 = thetaOffs[1];
        this.theta_off_3 = thetaOffs[2];
        this.theta_off_4 = thetaOffs[3];
        this.theta_off_5 = thetaOffs[4];
        this.prediction = (double) item.get("signal:prediction");

        this.eventTimestamp = eventTimestamp;
        this.analysisTimestamp = analysisTimestamp;
    }

    public RTASignal(DateTime eventTimestamp,
                     DateTime analysisTimestamp,
                     double theta,
                     double theta_off_1,
                     double theta_off_2,
                     double theta_off_3,
                     double theta_off_4,
                     double theta_off_5,
                     double prediction,
                     FACTRun run) {

        this.run = run;
        this.analysisTimestamp = analysisTimestamp;
        this.theta = theta;
        this.theta_off_1 = theta_off_1;
        this.theta_off_2 = theta_off_2;
        this.theta_off_3 = theta_off_3;
        this.theta_off_4 = theta_off_4;
        this.theta_off_5 = theta_off_5;
        this.prediction = prediction;
        this.eventTimestamp = eventTimestamp;
    }


}
