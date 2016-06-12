package fact.rta;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import stream.Data;
import stream.Keys;

import java.util.Set;

/**
 * JDBI interface to describe the signal table for sql operations
 * Created by kai on 24.05.16.
 */
public class RTADataBase {


    final class FACTRun {
        final String source;
        final int runID;
        final int night;
        final DateTime startTime;
        final DateTime endTime;
        final double onTime;

        FACTRun(Data item) {

            this.source = (String) item.get("Source");
            this.runID = (int) item.get("RUNID");
            this.night = (int) item.get("NIGHT");
            this.onTime = (double) item.get("onTime");
            this.startTime = DateTime.parse((String) item.get("DATE-OBS"));
            this.endTime = DateTime.parse((String) item.get("DATE-END"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FACTRun factRun = (FACTRun) o;
            return startTime.equals(factRun.startTime);
        }

        @Override
        public int hashCode() {
            return startTime.hashCode();
        }
    }


    final class RTASignal {

        final FACTRun run;
        final double prediction;
        final double theta_off_1;
        final double theta_off_2;
        final double theta_off_3;
        final double theta_off_4;
        final double theta_off_5;
        final double theta;
        final DateTime eventTimeStamp;

        RTASignal(DateTime eventTimeStamp, Data item, FACTRun run) {
            this.run = run;

            this.theta = (double) item.get("Theta");
            Set<String> offKeys = new Keys("Theta_Off_?").select(item);
            double[] thetaOffs = offKeys.stream().mapToDouble(s -> (double) item.get(s)).toArray();
            this.theta_off_1 = thetaOffs[0];
            this.theta_off_2 = thetaOffs[1];
            this.theta_off_3 = thetaOffs[2];
            this.theta_off_4 = thetaOffs[3];
            this.theta_off_5 = thetaOffs[4];
            this.prediction = (double) item.get("signal:prediction");

            this.eventTimeStamp = eventTimeStamp;
        }
    }

    public interface DBInterface {

        @SqlUpdate("INSERT OR IGNORE INTO fact_run (night, run_id, start_time, end_time, on_time, source) values (:night, :runID, :startTime, :endTime, :onTime, :source)")
        void insertRun(@BindBean FACTRun run);

        @SqlUpdate("INSERT OR IGNORE INTO signal (timestamp, night, run_id, prediction, theta_on. theta_off_1, theta_off_2, theta_off_3, theta_off_4, theta_off_5) " +
                "values (:r.timestamp, :s.run_id, :s.night, :r.prediction, :r.theta_on. :r.theta_off_1, :r.theta_off_2, :r.theta_off_3, :r.theta_off_4, :r.theta_off_5 )")
        void insertSignal(@BindBean("r") FACTRun run, @BindBean("s") RTASignal signal );


//        @SqlBatch("insert into something (id, name) values (:id, :name)")
//        @BatchChunkSize(1000)
//        void insertAll(@BindBean Iterator<Something> somethings);
        /**
         * close with no args is used to close the connection
         */
        void close();
    }
}
