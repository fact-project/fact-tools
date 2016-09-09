package fact.rta.db;

import fact.rta.RTADataBase;
import fact.rta.rest.RTASignal;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.lang.annotation.*;
import java.lang.reflect.Field;

import static org.reflections.Reflections.log;

/**
 * Created by mackaiver on 07/09/16.
 */
public class FACTRun {
    final private static Logger log = LoggerFactory.getLogger(FACTRun.class);


    //    // our binding annotation
    @BindingAnnotation(FACTRun.BindRun.RunBinderFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface BindRun
    {

        class RunBinderFactory implements BinderFactory
        {
            public Binder build(Annotation annotation)
            {
                return (Binder<FACTRun.BindRun, FACTRun>) (q, bind, argument) -> {


                        Field[] fields = argument.getClass().getDeclaredFields();
                        for (Field f : fields){
                            try {
                                System.out.println("binding name: " + f.getName());
                                q.bind(f.getName(), f.get(argument));
                            } catch (IllegalAccessException e) {
                                log.error("Could not access field value in statement: " + q.toString());
                            }
                        }

                };
            }
        }
    }


    final String source;
    public final int runID;
    public final int night;
    public final DateTime startTime;
    public final DateTime endTime;
    public final Duration onTime;



    public final RTADataBase.HEALTH health;

    public FACTRun(int night, int runID, String source, DateTime start, DateTime end, Duration onTime, RTADataBase.HEALTH health) {
        this.source = source;
        this.runID = runID;
        this.night = night;
        this.startTime = start;
        this.endTime = end;
        this.onTime = onTime;
        this.health = health;
    }

    private Duration fetchOnTimeFromSomeWhere() {
        //TODO: fetch this from rundb or something
        return Duration.standardSeconds(290);
    }

    public FACTRun(Data item) {

        this.source = (String) item.get("Source");
        this.runID = (int) item.get("RUNID");
        this.night = (int) item.get("NIGHT");
        this.onTime = fetchOnTimeFromSomeWhere();
        this.startTime = DateTime.parse((String) item.get("DATE-OBS"));
        this.endTime = DateTime.parse((String) item.get("DATE-END"));
        this.health = RTADataBase.HEALTH.UNKNOWN;
    }


    @Override
    public String toString() {
        return "FACTRun{" +
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
        if (!(o instanceof FACTRun)) return false;

        FACTRun factRun = (FACTRun) o;

        return runID == factRun.runID && night == factRun.night;
    }

    @Override
    public int hashCode() {
        int result = runID;
        result = 31 * result + night;
        return result;
    }
}
