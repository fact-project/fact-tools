package fact.rta;

import fact.rta.db.Run;
import fact.rta.db.Signal;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A singleton wrapping the dbi. don't know whether that's a good idea yet.
 * Created by kai on 24.05.16.
 */
public class RTADataBase {


    public enum HEALTH {
        OK,
        BROKEN,
        UNKNOWN,
        IN_PROGRESS
    }


    /**
     * JDBI interface to describe the signal table for sql operations
     * Created by kai on 24.05.16.
     */
    public static interface DBInterface {

        @SqlUpdate("INSERT OR IGNORE INTO fact_run " +
                "(night, run_id, start_time, end_time, on_time, source, health) " +
                "values (:night, :run_id, :start_time, :end_time, :on_time, :source, :health)")
        void insertRun(@Run.BindRun Run run);

        @SqlUpdate("INSERT OR IGNORE INTO signal " +
                "(event_timestamp, analysis_timestamp, night, run_id, prediction,estimated_energy, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5)" +
                "values " +
                "(:event_timestamp," +
                " :analysis_timestamp," +
                " :night," +
                " :run_id," +
                " :prediction," +
                " :estimated_energy," +
                " :theta," +
                " :theta_off_1," +
                " :theta_off_2," +
                " :theta_off_3," +
                " :theta_off_4," +
                " :theta_off_5" + ")"
        )
        void insertSignal(@Signal.BindSignal() Signal signal);


        @SqlQuery("SELECT * FROM signal JOIN fact_run USING (run_id,night)  WHERE event_timestamp BETWEEN :start AND :end")
        @RegisterMapper(Signal.SignalMapper.class)
        List<Signal> getSignalEntriesBetweenDates(@Bind("start") String start, @Bind("end") String end );


        @SqlQuery("SELECT * FROM signal JOIN fact_run USING (run_id,night)")
        @RegisterMapper(Signal.SignalMapper.class)
        List<Signal> getAllSignalEntries();



        @SqlUpdate("UPDATE fact_run SET health = :health WHERE   (night = :night AND run_id = :run_id)")
        void updateRunHealth(@Bind("health") HEALTH health, @Bind("run_id") int run_id, @Bind("night") int night);

        @SqlUpdate("UPDATE fact_run SET on_time = :on_time WHERE   night = :night AND run_id = :run_id")
        void updateRunWithOnTime(@Run.BindRun Run run, @Bind("on_time") double onTime);


        @SqlQuery("SELECT * from fact_run WHERE run_id = :run_id AND night = :night")
        @RegisterMapper(Run.RunMapper.class)
        Run getRun(@Bind("night") int night, @Bind("run_id") int runID);



        @SqlQuery("SELECT * from fact_run")
        @RegisterMapper(Run.RunMapper.class)
        Set<Run> getAllRuns();

        @SqlUpdate("CREATE TABLE IF NOT EXISTS fact_run " +
                "(night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "on_time REAL," +
                "start_time DATETIME," +
                "end_time DATETIME," +
                "source varchar(50)," +
                "health varchar(50)," +
                "PRIMARY KEY (night, run_id))")
        void createRunTableIfNotExists();


        @SqlUpdate("CREATE TABLE IF NOT EXISTS signal " +
                "(" +
                    "event_timestamp DATETIME PRIMARY KEY, " +
                    "analysis_timestamp DATETIME," +
                    "night INTEGER NOT NULL, " +
                    "run_id INTEGER NOT NULL," +
                    "prediction REAL NOT NULL," +
                    "estimated_energy REAL NOT NULL," +
                    "theta_on REAL NOT NULL," +
                    "theta_off_1 REAL," +
                    "theta_off_2 REAL," +
                    "theta_off_3 REAL," +
                    "theta_off_4 REAL," +
                    "theta_off_5 REAL," +
                    "FOREIGN KEY(night, run_id) REFERENCES fact_run(night, run_id)" +
                ")")
        void createSignalTableIfNotExists();

        /**
         * close with no args is used to close the connection
         */
        void close();
    }
}
