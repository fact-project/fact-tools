package fact.rta;

import fact.rta.db.RunMapper;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.StatementCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.jdbc4.JDBC4PreparedStatement;
import stream.Data;
import stream.Keys;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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




    public final class FACTRun {


        final String source;
        final int runID;


        final int night;

        final DateTime startTime;
        final DateTime endTime;
        final Double relativeOnTime;


        final HEALTH health;

        public FACTRun(int night, int runID, String source, DateTime start, DateTime end, double relativeOnTime, HEALTH health){
            this.source = source;
            this.runID = runID;
            this.night = night;
            this.startTime = start;
            this.endTime = end;
            this.relativeOnTime = relativeOnTime;
            this.health = health;
        }

        public FACTRun(Data item) {

            this.source = (String) item.get("Source");
            this.runID = (int) item.get("RUNID");
            this.night = (int) item.get("NIGHT");
            this.relativeOnTime = 0.0;
            this.startTime = DateTime.parse((String) item.get("DATE-OBS"));
            this.endTime = DateTime.parse((String) item.get("DATE-END"));
            this.health = HEALTH.UNKNOWN;
        }


        public double getRelativeOnTime() {
            return relativeOnTime;
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

        public HEALTH getHealth() {
            return health;
        }

        @Override
        public String toString() {
            return "FACTRun{" +
                    "source='" + source + '\'' +
                    ", runID=" + runID +
                    ", night=" + night +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", relativeOnTime=" + relativeOnTime +
                    ", health=" + health +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FACTRun)) return false;

            FACTRun factRun = (FACTRun) o;

            return runID == factRun.runID && night == factRun.night;

        }

        @Override
        public int hashCode() {
            int result = runID;
            result = 31 * result + night;
            return result;
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


    public enum HEALTH {
        OK,
        BROKEN,
        UNKNOWN,
        IN_PROGRESS
    }


    public interface DBInterface {

        @SqlUpdate("INSERT OR IGNORE INTO fact_run (night, run_id, start_time, end_time, on_time, source, health) values (:night, :runID, :startTime, :endTime, :onTime, :source, :health)")
        void insertRun(@BindBean FACTRun run);

        @SqlUpdate("INSERT OR IGNORE INTO signal (timestamp, night, run_id, prediction, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5)" +
                "values(:eventTimeStamp, :night, :run_id, :prediction, :theta, :theta_off_1, :theta_off_2, :theta_off_3, :theta_off_4, :theta_off_5)")
        void insertSignal(@BindSignal() RTASignal signal );


        @SqlUpdate("UPDATE fact_run SET on_time = :on_time WHERE   night = :night AND run_id = :run_id")
        void updateRunWithOnTime(@Bind("relative_on_time") double relativeOnTime, @Bind("run_id") int run_id, @Bind("night") int night);

        @SqlQuery("SELECT * FROM signal")
        List<String> getSignalEntries();

        @SqlUpdate("UPDATE fact_run SET health = :health WHERE   (night = :night AND run_id = :run_id)")
        void updateRunHealth(@Bind("health") HEALTH health, @Bind("run_id") int run_id, @Bind("night") int night);



        @SqlQuery("SELECT * from fact_run WHERE run_id = :run_id AND night = :night")
        @RegisterMapper(RunMapper.class)
        FACTRun getRun(@Bind("night") int night, @Bind("run_id") int runID);

        @SqlUpdate("CREATE TABLE IF NOT EXISTS fact_run " +
                "(night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "on_time FLOAT," +
                "start_time VARCHAR(50)," +
                "end_time VARCHAR(50)," +
                "source varchar(50)," +
                "health varchar(50)," +
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
