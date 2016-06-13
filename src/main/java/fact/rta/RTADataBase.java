package fact.rta;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * JDBI interface to describe the signal table for sql operations
 * Created by kai on 24.05.16.
 */
public class RTADataBase {

    final private static Logger log = LoggerFactory.getLogger(RTADataBase.class);

    // our binding annotation
    @BindingAnnotation(BindSignal.SignalBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindSignal
    {

        public static class SignalBinderFactory implements BinderFactory
        {
            public Binder build(Annotation annotation)
            {
                return new Binder<BindSignal, RTASignal>()
                {
                    public void bind(SQLStatement q, BindSignal bind, RTASignal s)
                    {
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
//                            e.printStackTrace();
                        }

//                        (timestamp, night, run_id, prediction, theta_on. theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5
                    }
                };
            }
        }
    }




    public final class FACTRun {


        final String source;
        final int runID;


        final int night;

        final DateTime startTime;
        final DateTime endTime;
        final double onTime;

        FACTRun(Data item) {

            this.source = (String) item.get("Source");
            this.runID = (int) item.get("RUNID");
            this.night = (int) item.get("NIGHT");
            this.onTime = (double) item.get("OnTime");
            this.startTime = DateTime.parse((String) item.get("DATE-OBS"));
            this.endTime = DateTime.parse((String) item.get("DATE-END"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FACTRun factRun = (FACTRun) o;
            return startTime.equals(factRun.startTime);
        }

        @Override
        public int hashCode() {
            return startTime.hashCode();
        }

        public double getOnTime() {
            return onTime;
        }

        public String getSource() {
            return source;
        }

        public int getRunID() {
            return runID;
        }

        public int getNight() {
            return night;
        }

        public DateTime getStartTime() {
            return startTime;
        }

        public DateTime getEndTime() {
            return endTime;
        }

    }


    final class RTASignal {

        public final FACTRun run;
        public final double prediction;
        public final double theta_off_1;
        public final double theta_off_2;
        public final double theta_off_3;
        public final double theta_off_4;
        public final double theta_off_5;
        public final double theta;
        public final DateTime eventTimeStamp;

        RTASignal(DateTime eventTimeStamp, Data item, FACTRun run) {
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

            this.eventTimeStamp = eventTimeStamp;
        }
    }

    public interface DBInterface {

        @SqlUpdate("INSERT OR IGNORE INTO fact_run (night, run_id, start_time, end_time, on_time, source) values (:night, :runID, :startTime, :endTime, :onTime, :source)")
        void insertRun(@BindBean FACTRun run);

        @SqlUpdate("INSERT OR IGNORE INTO signal (timestamp, night, run_id, prediction, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5)" +
                "values(:eventTimeStamp, :night, :run_id, :prediction, :theta, :theta_off_1, :theta_off_2, :theta_off_3, :theta_off_4, :theta_off_5)")
        void insertSignal(@BindSignal() RTASignal signal );


        @SqlQuery("SELECT * from signal")
        List<String> getSignalEntries();

//        @SqlBatch("insert into something (id, name) values (:id, :name)")
//        @BatchChunkSize(1000)
//        void insertAll(@BindBean Iterator<Something> somethings);

        @SqlUpdate("CREATE TABLE IF NOT EXISTS fact_run " +
                "(night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "on_time FLOAT," +
                "start_time VARCHAR(50)," +
                "end_time VARCHAR(50)," +
                "source varchar(50)," +
                "PRIMARY KEY (night, run_id))")
        void createRunTable();

        @SqlUpdate("CREATE TABLE IF NOT EXISTS signal " +
                "(timestamp VARCHAR(50) PRIMARY KEY, " +
                "night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "prediction FLOAT NOT NULL," +
                "theta_on FLOAT NOT NULL," +
                "theta_off_1 FLOAT," +
                "theta_off_2 FLOAT," +
                "theta_off_3 FLOAT," +
                "theta_off_4 FLOAT," +
                "theta_off_5 FLOAT," +
                "FOREIGN KEY(night, run_id) REFERENCES fact_run(night, run_id))")
        void createSignalTable();
        /**
         * close with no args is used to close the connection
         */
        void close();
    }
}
