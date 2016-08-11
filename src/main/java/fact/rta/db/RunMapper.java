package fact.rta.db;

import fact.rta.RTADataBase;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;


import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * Created by kai on 14.06.16.
 */
public class RunMapper implements ResultSetMapper<RTADataBase.FACTRun>
{
    public RTADataBase.FACTRun map(int index, ResultSet r, StatementContext ctx) throws SQLException
    {
        return new RTADataBase().new FACTRun(
                r.getInt("night"),
                r.getInt("run_id"),
                r.getString("source"),
                DateTime.parse(r.getString("start_time")),
                DateTime.parse(r.getString("end_time")),
                r.getDouble("relative_on_time"),
                RTADataBase.HEALTH.valueOf(r.getString("health"))
        );
    }
}
