package fact.rta;

import fact.rta.db.FACTRun;
import fact.rta.db.RTASignalMapper;
import fact.rta.rest.RTASignal;
import fact.rta.db.RunMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * JDBI interface to describe the signal table for sql operations
 * Created by kai on 24.05.16.
 */
public class RTADataBase {

    public enum HEALTH {
        OK,
        BROKEN,
        UNKNOWN,
        IN_PROGRESS
    }


    public interface DBInterface {

        @SqlUpdate("INSERT OR IGNORE INTO fact_run " +
                "(night, run_id, start_time, end_time, on_time, source, health) " +
                "values (:night, :runID, :startTime, :endTime, :onTime, :source, :health)")
        void insertRun(@FACTRun.BindRun FACTRun run);

        @SqlUpdate("INSERT OR IGNORE INTO signal " +
                "(event_timestamp, analysis_timestamp, night, run_id, prediction, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5, on_time_per_event)" +
                "values " +
                "(:eventTimestamp," +
                " :analysisTimestamp," +
                " :night," +
                " :runId," +
                " :prediction," +
                " :theta," +
                " :theta_off_1," +
                " :theta_off_2," +
                " :theta_off_3," +
                " :theta_off_4," +
                " :theta_off_5," +
                " :on_time_per_event)"
        )
        void insertSignal(@RTASignal.BindSignal() RTASignal signal);


        @SqlQuery("SELECT * FROM signal JOIN fact_run USING (run_id,night)  WHERE timestamp BETWEEN :start AND :end")
        @RegisterMapper(RTASignalMapper.class)
        List<RTASignal> getSignalEntries(@Bind("start") String start, @Bind("end") String end );

        @SqlUpdate("UPDATE fact_run SET health = :health WHERE   (night = :night AND run_id = :run_id)")
        void updateRunHealth(@Bind("health") HEALTH health, @Bind("run_id") int run_id, @Bind("night") int night);

        @SqlUpdate("UPDATE fact_run SET on_time = :on_time WHERE   night = :night AND run_id = :run_id")
        void updateRunWithOnTime(@Bind("on_time") double onTime, @Bind("run_id") int run_id, @Bind("night") int night);


        @SqlQuery("SELECT * from fact_run WHERE run_id = :run_id AND night = :night")
        @RegisterMapper(RunMapper.class)
        FACTRun getRun(@Bind("night") int night, @Bind("run_id") int runID);

        @SqlUpdate("CREATE TABLE IF NOT EXISTS fact_run " +
                "(night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "on_time INTEGER," +
                "start_time VARCHAR(50)," +
                "end_time VARCHAR(50)," +
                "source varchar(50)," +
                "health varchar(50)," +
                "PRIMARY KEY (night, run_id))")
        void createRunTable();

        @SqlUpdate("CREATE TABLE IF NOT EXISTS signal " +
                "(" +
                    "event_timestamp VARCHAR(50) PRIMARY KEY, " +
                    "analysis_timestamp VARCHAR(50)," +
                    "night INTEGER NOT NULL, " +
                    "run_id INTEGER NOT NULL," +
                    "prediction FLOAT NOT NULL," +
                    "theta_on FLOAT NOT NULL," +
                    "theta_off_1 FLOAT," +
                    "theta_off_2 FLOAT," +
                    "theta_off_3 FLOAT," +
                    "theta_off_4 FLOAT," +
                    "theta_off_5 FLOAT," +
                    "on_time_per_event FLOAT," +
                    "FOREIGN KEY(night, run_id) REFERENCES fact_run(night, run_id)" +
                ")")
        void createSignalTable();

        /**
         * close with no args is used to close the connection
         */
        void close();
    }
}
