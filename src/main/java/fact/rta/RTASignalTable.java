package fact.rta;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

/**
 * JDBI interface to describe the signal table for sql operations
 * Created by kai on 24.05.16.
 */
public interface RTASignalTable {

    @SqlUpdate("DROP TABLE IF EXISTS RTASignal")
    void dropTable();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS RTASignal (start VARCHAR(100) PRIMARY KEY, end VARCHAR(100), source VARCHAR(100), " +
            "signal_events INT, background_events INT, prediction_threshold FLOAT, theta_square_cut FLOAT)")
    void createSignalTableIfNotExists();

    @SqlUpdate("INSERT INTO RTASignal (start, end, source, signal_events, background_events, prediction_threshold, theta_square_cut)" +
            " values (:start, :end, :source, :signal_events, :background_events,:prediction_threshold, :theta_square_cut) ")
    void insert(@Bind("start") String  start, @Bind("end") String end, @Bind("source") String source,
                @Bind("signal_events") int signal_events, @Bind("background_events") double background_events,
                @Bind("prediction_threshold") double prediction_threshold, @Bind("theta_square_cut") double theta_square_cut);

    @SqlUpdate("INSERT OR IGNORE INTO RTASignal (start, end, source, signal_events, background_events, prediction_threshold, theta_square_cut)" +
            " values (:start, :end, :source, :signal_events, :background_events,:prediction_threshold, :theta_square_cut) ")
    void insertOrIgnore(@Bind("start") String  start, @Bind("end") String end, @Bind("source") String source,
                        @Bind("signal_events") int signal_events, @Bind("background_events") double background_events,
                        @Bind("prediction_threshold") double prediction_threshold, @Bind("theta_square_cut") double theta_square_cut);

//    @SqlQuery("select name from something where id = :id")
//    String findNameById(@Bind("id") int id);

    /**
     * close with no args is used to close the connection
     */
    void close();
}
