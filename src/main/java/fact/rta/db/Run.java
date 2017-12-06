package fact.rta.db;

import fact.rta.RTADataBase;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Created by mackaiver on 07/09/16.
 */
public class Run {

    public String source;
    public int run_id;
    public int night;
    public ZonedDateTime start_time;
    public ZonedDateTime end_time;
    public long on_time_seconds;

    public RTADataBase.HEALTH health;

    @JdbiConstructor
    public Run(int night, int run_id, String source, ZonedDateTime start_time, ZonedDateTime end_time, long on_time_seconds, RTADataBase.HEALTH health) {
        this.source = source;
        this.run_id = run_id;
        this.night = night;
        this.start_time = start_time;
        this.end_time = end_time;
        this.on_time_seconds = on_time_seconds;
        this.health = health;
    }

    public Run(Data item) {

        this.source = ((String) item.get("SourceName")).trim();
        this.run_id = (int) item.get("RUNID");
        this.night = (int) item.get("NIGHT");
        this.on_time_seconds = Duration.ZERO.getSeconds();

        this.start_time = LocalDateTime.parse((String) item.get("DATE-OBS")).atZone(ZoneOffset.UTC);
        this.end_time = LocalDateTime.parse((String) item.get("DATE-END")).atZone(ZoneOffset.UTC);
        this.health = RTADataBase.HEALTH.UNKNOWN;
    }


    @Override
    public String toString() {
        return "Run{" +
                "source='" + source + '\'' +
                ", runID=" + run_id +
                ", night=" + night +
                ", startTime=" + start_time +
                ", endTime=" + end_time +
                ", onTime=" + on_time_seconds + " seconds" +
                ", health=" + health +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Run)) return false;

        Run factRun = (Run) o;

        return run_id == factRun.run_id && night == factRun.night;
    }

    @Override
    public int hashCode() {
        int result = run_id;
        result = 31 * result + night;
        return result;
    }
}
