package fact.rta.rest;

import fact.rta.RTADataBase;
import fact.rta.db.Signal;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by mackaiver on 07/09/16.
 */


public class LightCurve {

    List<LightCurveBin> bins = new ArrayList<>();

    double alpha = 0.2;
    double predictionThreshold = 0.95;
    double thetaThreshold = 0.04;

    DateTime startTime = DateTime.parse("2011-10-01T00:00:00+00:00").withZoneRetainFields(DateTimeZone.UTC);
    DateTime endTime= DateTime.now().withZoneRetainFields(DateTimeZone.UTC);

    int binningInMinutes = 5;


    public static class LightCurveFromDB{
        private LightCurve lc;
        private RTADataBase.DBInterface dbInterface;

        public LightCurveFromDB(RTADataBase.DBInterface dbInterface) {
            lc = new LightCurve();
            this.dbInterface = dbInterface;

        }

        public LightCurveFromDB withStartTime(DateTime startTime){
            lc.startTime = startTime;
            return this;
        }

        public LightCurveFromDB withEndTime(DateTime endTime){
            lc.endTime = endTime;
            return this;
        }

        public LightCurveFromDB withBinning(int binningInMinutes){
            lc.binningInMinutes = binningInMinutes;
            return this;
        }

        public LightCurveFromDB withOffSources(int numberOfOffSources){
            lc.alpha = 1.0/numberOfOffSources;
            return this;
        }

        public LightCurveFromDB withPredictionThreshold(double predictionThreshold){
            lc.predictionThreshold = predictionThreshold;
            return this;
        }


        public LightCurveFromDB withThetaThreshold(double thetaThreshold){
            lc.thetaThreshold = thetaThreshold;
            return this;
        }

        public LightCurve create(){
//            ArrayList<LightCurveBin> lc = new ArrayList<>();

            final List<Signal> signalEntries = dbInterface.getSignalEntriesBetweenDates(lc.startTime.toString("YYYY-MM-dd HH:mm:ss"), lc.endTime.toString("YYYY-MM-dd HH:mm:ss"));

            //fill a treemap
            TreeMap<DateTime, Signal> dateTimeRTASignalTreeMap = new TreeMap<>();
            signalEntries.forEach(a -> dateTimeRTASignalTreeMap.put(a.eventTimestamp, a));

            //iterate over all the bins
            for (int bin = 0; bin < lc.binningInMinutes; bin++) {
                //get all entries in bin
                SortedMap<DateTime, Signal> subMap = dateTimeRTASignalTreeMap.subMap(lc.startTime, lc.startTime.plusMinutes(bin));
                Stream<Signal> rtaSignalStream = subMap.entrySet().stream().map(Map.Entry::getValue);

                //get on time in this bin by summing the ontimes per event in each row
                double onTimeInBin = rtaSignalStream.mapToDouble(a -> a.onTimePerEvent).sum();

                //select gamma like events and seperate signal and background region
                Stream<Signal> gammaLike = rtaSignalStream.filter(s -> s.prediction > lc.predictionThreshold);

                int signal = (int) gammaLike
                        .filter(s -> s.theta < lc.thetaThreshold)
                        .count();

                int background = (int) gammaLike
                        .filter(s-> (
                                s.theta_off_1 < lc.thetaThreshold ||
                                        s.theta_off_2 < lc.thetaThreshold ||
                                        s.theta_off_3 < lc.thetaThreshold ||
                                        s.theta_off_4 < lc.thetaThreshold ||
                                        s.theta_off_5 < lc.thetaThreshold
                        ))
                        .count();

                LightCurveBin lightCurveBin = new LightCurveBin(lc.startTime, lc.startTime.plusMinutes(bin), background, signal, lc.alpha, onTimeInBin);
                lc.bins.add(lightCurveBin);

            }
            return lc;
        }
    }


    private static class LightCurveBin {
        public final DateTime startTime;
        public final DateTime endTime;
        public final int backgroundEvents;
        public final int signalEvents;
        public final double alpha;
        public final double onTimeInBin;


        public LightCurveBin(DateTime startTime, DateTime endTime, int backgroundEvents, int signalEvents, double alpha, double onTimeInBin) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.backgroundEvents = backgroundEvents;
            this.signalEvents = signalEvents;
            this.alpha = alpha;
            this.onTimeInBin = onTimeInBin;
        }
    }
}
