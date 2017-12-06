package fact.rta;

import fact.rta.db.Run;
import fact.rta.db.Signal;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;

import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * JDBI interface to describe the signal table for sql operations
 * Created by kai on 24.05.16.
 */
public interface RTADataBase {

        enum HEALTH {
            OK,
            BROKEN,
            UNKNOWN,
            IN_PROGRESS
        }


        @SqlUpdate("INSERT OR IGNORE INTO fact_run " +
                "(night, run_id, start_time, end_time, on_time_seconds, source, health) " +
                "values (:night, :run_id, :start_time, :end_time, :on_time_seconds, :source, :health)")
        void insertRun(@BindFields Run run);

        @SqlUpdate("INSERT OR IGNORE INTO signal " +
                "(event_timestamp, analysis_timestamp, night, run_id, prediction,estimated_energy, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5)" +
                "values " +
                "(:event_timestamp," +
                " :analysis_timestamp," +
                " :run.night," +
                " :run.run_id," +
                " :prediction," +
                " :estimated_energy," +
                " :theta_on," +
                " :theta_off_1," +
                " :theta_off_2," +
                " :theta_off_3," +
                " :theta_off_4," +
                " :theta_off_5" + ")"
        )
        void insertSignal(@BindFields Signal signal);


        @SqlBatch("INSERT OR IGNORE INTO signal " +
                "(event_timestamp, analysis_timestamp, night, run_id, prediction,estimated_energy, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5)" +
                "values " +
                "(:event_timestamp," +
                " :analysis_timestamp," +
                " :run.night," +
                " :run.run_id," +
                " :prediction," +
                " :estimated_energy," +
                " :theta_on," +
                " :theta_off_1," +
                " :theta_off_2," +
                " :theta_off_3," +
                " :theta_off_4," +
                " :theta_off_5" + ")"
        )
        void insertSignals(@BindFields Iterator<Signal> signals);


        @SqlQuery("SELECT * FROM signal JOIN fact_run USING (run_id,night) WHERE event_timestamp BETWEEN :start AND :end")
        @RegisterConstructorMapper(Signal.class)
        List<Signal> getSignalEntriesBetweenDates(ZonedDateTime start, ZonedDateTime end );


        @SqlQuery("SELECT * FROM signal JOIN fact_run USING (run_id,night)")
        @RegisterConstructorMapper(Signal.class)
        List<Signal> getAllSignalEntries();



        @SqlUpdate("UPDATE fact_run SET health = :health WHERE   (night = :run.night AND run_id = :run.run_id)")
        void updateRunHealth(HEALTH health, @BindFields("run") Run run);

        @SqlUpdate("UPDATE fact_run SET on_time_seconds = :on_time_seconds WHERE   night = :run.night AND run_id = :run.run_id")
        void updateRunWithOnTime(@BindFields("run") Run run, double on_time_seconds);


        @SqlQuery("SELECT * from fact_run WHERE run_id = :run_id AND night = :night")
        @RegisterConstructorMapper(Run.class)
        Run getRun(int night, int run_id);



        @SqlQuery("SELECT * from fact_run")
        @RegisterConstructorMapper(Run.class)
        Set<Run> getAllRuns();

        @SqlUpdate("CREATE TABLE IF NOT EXISTS fact_run " +
                "(night INTEGER NOT NULL, " +
                "run_id INTEGER NOT NULL," +
                "on_time_seconds REAL," +
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

}

