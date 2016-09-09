package fact.rta.db;

import fact.rta.rest.RTASignal;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * Created by kai on 14.06.16.
 */
public class RTASignalMapper implements ResultSetMapper<RTASignal>
{
    public RTASignal map(int index, ResultSet r, StatementContext ctx) throws SQLException
    {
        DateTime eventTimestamp = DateTime.parse(r.getString("event_timestamp"));
        DateTime analyisTimestamp = DateTime.parse(r.getString("analysis_timestamp"));
        double theta_on = r.getDouble("theta_on");
        double theta_off_1 = r.getDouble("theta_off_1");
        double theta_off_2 = r.getDouble("theta_off_2");
        double theta_off_3 = r.getDouble("theta_off_3");
        double theta_off_4 = r.getDouble("theta_off_4");
        double theta_off_5 = r.getDouble("theta_off_5");
        double prediction = r.getDouble("prediction");
        FACTRun run = new RunMapper().map(index, r, ctx);

        return new RTASignal(eventTimestamp,
                analyisTimestamp,
                theta_on,
                theta_off_1,
                theta_off_2,
                theta_off_3,
                theta_off_4,
                theta_off_5,
                prediction,
                run);
    }

}
