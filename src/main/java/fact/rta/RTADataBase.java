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

//    private static RTADataBase instance;
//    private String jdbcConnection;
//
//    private RTADataBase(String jdbcConnection){
//
//        this.jdbcConnection = jdbcConnection;
//    }
//
//    /**
//     * Get an instance of this database object holding a connection to the database with the jdbconnecitons tring passed to this method.
//     * As in "jdbc:sqlite:./test.sqlite" which will return the RTADatabase Object connected to that particular database.
//     * @param jdbcConnection a jdbc string describing the connection to the db
//     * @return an instance of the RTADataBase class.
//     */
//    public static synchronized RTADataBase getInstance(String jdbcConnection){
//        if(instance == null){
//           instance = new RTADataBase(jdbcConnection);
//        }
//        return instance;
//    }

    public enum HEALTH {
        OK,
        BROKEN,
        UNKNOWN,
        IN_PROGRESS
    }

//    private DBI dbi = new DBI(jdbcConnection);
//    public RTADataBase.DBInterface dbInterface = dbi.open(RTADataBase.DBInterface.class);

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
                "(event_timestamp, analysis_timestamp, night, run_id, prediction, theta_on, theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5, on_time_per_event)" +
                "values " +
                "(:event_timestamp," +
                " :analysis_timestamp," +
                " :night," +
                " :run_id," +
                " :prediction," +
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
                "on_time FLOAT," +
                "start_time VARCHAR(50)," +
                "end_time VARCHAR(50)," +
                "source varchar(50)," +
                "health varchar(50)," +
                "PRIMARY KEY (night, run_id))")
        void createRunTableIfNotExists();


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
        void createSignalTableIfNotExists();

        /**
         * close with no args is used to close the connection
         */
        void close();
    }
}
