package fact.rta.db;


import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Set;


/**
 * The Signal object which contains the information which will be saved in the database for every
 * 'signal-like' event.
 */
public class Signal {

    final private static Logger log = LoggerFactory.getLogger(Signal.class);


    /**
     * The SignalMapper is needed by the JDBI library to be able to save this object into the database.
     * This would work autmatically except for the DateTime objects which we need to parse
     * using the appropriate methods since JDBI doesn't do that by itself.
     */
    public static class SignalMapper implements ResultSetMapper<Signal>
    {
        public Signal map(int index, ResultSet r, StatementContext ctx) throws SQLException
        {
            ZonedDateTime eventTimestamp = ZonedDateTime.parse(r.getString("event_timestamp"));
            ZonedDateTime analyisTimestamp = ZonedDateTime.parse(r.getString("analysis_timestamp"));
            double theta_on = r.getDouble("theta_on");
            double theta_off_1 = r.getDouble("theta_off_1");
            double theta_off_2 = r.getDouble("theta_off_2");
            double theta_off_3 = r.getDouble("theta_off_3");
            double theta_off_4 = r.getDouble("theta_off_4");
            double theta_off_5 = r.getDouble("theta_off_5");
            double prediction = r.getDouble("prediction");
            double estimated_energy = r.getDouble("estimated_energy");
            Run run = new Run.RunMapper().map(index, r, ctx);

            return new Signal(eventTimestamp,
                    analyisTimestamp,
                    theta_on,
                    theta_off_1,
                    theta_off_2,
                    theta_off_3,
                    theta_off_4,
                    theta_off_5,
                    prediction,
                    estimated_energy,
                    run);
        }

    }


    /**
     * The binding annotations maps member names to coliumn names in the database.
     * Maybe this can work automatically? I've seen this work in Google GSON.
     */
    @BindingAnnotation(BindSignal.SignalBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindSignal
    {

        class SignalBinderFactory implements BinderFactory
        {
            public Binder build(Annotation annotation)
            {
                return (Binder<BindSignal, Signal>) (q, bind, s) -> {
                    q.bind("night", s.run.night );
                    q.bind("run_id", s.run.runID);
                    q.bind("prediction", s.prediction);
                    q.bind("estimated_energy", s.estimated_energy);
                    q.bind("theta", s.theta);
                    q.bind("theta_off_1", s.theta_off_1);
                    q.bind("theta_off_2", s.theta_off_2);
                    q.bind("theta_off_3", s.theta_off_3);
                    q.bind("theta_off_4", s.theta_off_4);
                    q.bind("theta_off_5", s.theta_off_5);
                    q.bind("event_timestamp" ,s.eventTimestamp.toString());
                    q.bind("analysis_timestamp" ,s.analysisTimestamp.toString());
                };
            }
        }
    }

    public final Run run;
    public final double prediction;
    public final double estimated_energy;
    public final double theta_off_1;
    public final double theta_off_2;
    public final double theta_off_3;
    public final double theta_off_4;
    public final double theta_off_5;
    public final double theta;
    public final ZonedDateTime eventTimestamp;
    public final ZonedDateTime analysisTimestamp;

    public double onTimePerEvent;

    public Signal(ZonedDateTime eventTimestamp, ZonedDateTime analysisTimestamp, Data item, Run run) {

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
        this.estimated_energy= (double) item.get("energy");

        this.eventTimestamp = eventTimestamp;
        this.analysisTimestamp = analysisTimestamp;
    }

    public Signal(ZonedDateTime eventTimestamp,
                  ZonedDateTime analysisTimestamp,
                  double theta,
                  double theta_off_1,
                  double theta_off_2,
                  double theta_off_3,
                  double theta_off_4,
                  double theta_off_5,
                  double prediction,
                  double estimated_energy,
                  Run run) {

        this.run = run;
        this.analysisTimestamp = analysisTimestamp;
        this.theta = theta;
        this.theta_off_1 = theta_off_1;
        this.theta_off_2 = theta_off_2;
        this.theta_off_3 = theta_off_3;
        this.theta_off_4 = theta_off_4;
        this.theta_off_5 = theta_off_5;
        this.prediction = prediction;
        this.estimated_energy= estimated_energy;
        this.eventTimestamp = eventTimestamp;
    }


}
