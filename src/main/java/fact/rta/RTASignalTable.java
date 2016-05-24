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

    @SqlUpdate("CREATE TABLE RTASignal (start VARCHAR(100) PRIMARY KEY, end VARCHAR(100), source VARCHAR(100), signal_events INT, background_events INT)")
    void createSignalTable();

    @SqlUpdate("INSERT INTO RTASignal (start, end, source, signal_events, background_events) values (:start, :end, :source, :signal_events, :background_events) ")
    void insert(@Bind("start") String  start, @Bind("end") String end, @Bind("source") String source, @Bind("signal_events") int signal_events, @Bind("background_events") int background_events);

    @SqlUpdate("INSERT OR IGNORE INTO RTASignal (start, end, source, signal_events, background_events) values (:start, :end, :source, :signal_events, :background_events) ")
    void insertOrIgnore(@Bind("start") String  start, @Bind("end") String end, @Bind("source") String source, @Bind("signal_events") int signal_events, @Bind("background_events") int background_events);


//    @SqlQuery("select name from something where id = :id")
//    String findNameById(@Bind("id") int id);

    /**
     * close with no args is used to close the connection
     */
    void close();
}
