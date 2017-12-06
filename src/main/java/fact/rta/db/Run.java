package fact.rta.db;

import fact.rta.RTADataBase;
import org.joda.time.Duration;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Created by mackaiver on 07/09/16.
 */
public class Run {
    final private static Logger log = LoggerFactory.getLogger(Run.class);


    public static class RunMapper implements ResultSetMapper<Run>
    {
        public Run map(int index, ResultSet r, StatementContext ctx) throws SQLException
        {
            return new Run(
                    r.getInt("night"),
                    r.getInt("run_id"),
                    r.getString("source"),
                    ZonedDateTime.parse(r.getString("start_time")).withZoneSameInstant(ZoneOffset.UTC),
                    ZonedDateTime.parse(r.getString("end_time")).withZoneSameInstant(ZoneOffset.UTC),
                    Duration.standardSeconds(r.getLong("on_time")),
                    RTADataBase.HEALTH.valueOf(r.getString("health"))
            );
        }
    }

    @BindingAnnotation(Run.BindRun.RunBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindRun
    {
        class RunBinderFactory implements BinderFactory
        {
            public Binder build(Annotation annotation)
            {
                return (Binder<Run.BindRun, Run>) (q, bind, argument) -> {
                    q.bind("night", argument.night);
                    q.bind("run_id", argument.runID);
                    q.bind("source", argument.source);
                    q.bind("end_time", argument.endTime);
                    q.bind("start_time", argument.startTime);
                    q.bind("on_time", argument.onTime.getStandardSeconds());
                    q.bind("health", argument.health);
                };
            }
        }
    }


    public final String source;
    public final int runID;
    public final int night;
    public final ZonedDateTime startTime;
    public final ZonedDateTime endTime;
    public final Duration onTime;



    public final RTADataBase.HEALTH health;

    public Run(int night, int runID, String source, ZonedDateTime start, ZonedDateTime end, Duration onTime, RTADataBase.HEALTH health) {
        this.source = source;
        this.runID = runID;
        this.night = night;
        this.startTime = start;
        this.endTime = end;
        this.onTime = onTime;
        this.health = health;
    }

    public Run(Data item) {

        this.source = ((String) item.get("SourceName")).trim();
        this.runID = (int) item.get("RUNID");
        this.night = (int) item.get("NIGHT");
        this.onTime = Duration.ZERO;

        this.startTime = LocalDateTime.parse((String) item.get("DATE-OBS")).atZone(ZoneOffset.UTC);
        this.endTime = LocalDateTime.parse((String) item.get("DATE-END")).atZone(ZoneOffset.UTC);
        this.health = RTADataBase.HEALTH.UNKNOWN;
    }


    @Override
    public String toString() {
        return "Run{" +
                "source='" + source + '\'' +
                ", runID=" + runID +
                ", night=" + night +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", onTime=" + onTime.getStandardSeconds() + " seconds" +
                ", health=" + health +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Run)) return false;

        Run factRun = (Run) o;

        return runID == factRun.runID && night == factRun.night;
    }

    @Override
    public int hashCode() {
        int result = runID;
        result = 31 * result + night;
        return result;
    }
}