package fact.rta.rest;

import fact.rta.RTADataBase;
import fact.rta.db.Signal;



import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mackaiver on 07/09/16.
 */
public class LightCurve {

    double alpha = 0.2;
    double predictionThreshold = 0.95;
    double thetaThreshold = 0.04;

    OffsetDateTime startTime = OffsetDateTime.parse("2011-10-01T00:00:00+00:00");
    OffsetDateTime endTime= OffsetDateTime.now(ZoneOffset.UTC);

    int binningInMinutes = 5;

    List<LightCurveBin> bins = new ArrayList<>();

    public static class LightCurveFromDB{
        private LightCurve lc;
        private RTADataBase.DBInterface dbInterface;

        public LightCurveFromDB(RTADataBase.DBInterface dbInterface) {
            lc = new LightCurve();
            this.dbInterface = dbInterface;

        }

        public LightCurveFromDB withStartTime(OffsetDateTime startTime){
            lc.startTime = startTime;
            return this;
        }

        public LightCurveFromDB withEndTime(OffsetDateTime endTime){
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

            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

            final List<Signal> signalEntries = dbInterface.getSignalEntriesBetweenDates(lc.startTime.format(pattern), lc.endTime.format(pattern));

//            Map<String, List<Signal>> map = signalEntries.stream().collect(Collectors.groupingBy(s -> s.run.source));

            //fill a treemap
            TreeMap<OffsetDateTime, Signal> dateTimeRTASignalTreeMap = new TreeMap<>();
            signalEntries.forEach(a -> dateTimeRTASignalTreeMap.put(a.eventTimestamp, a));

            if(dateTimeRTASignalTreeMap.firstEntry() == null ||
                    dateTimeRTASignalTreeMap.lastEntry() == null){
                return lc;
            }
            lc.startTime = dateTimeRTASignalTreeMap.firstEntry().getValue().run.startTime;
            lc.endTime= dateTimeRTASignalTreeMap.lastEntry().getValue().run.endTime;

            //iterate over all the bins
            for (int bin = 0; lc.startTime.plusMinutes(bin).isBefore(lc.endTime); bin += lc.binningInMinutes) {
                //get all entries in bin
                SortedMap<OffsetDateTime, Signal> subMap = dateTimeRTASignalTreeMap.subMap(lc.startTime.plusMinutes(bin), lc.startTime.plusMinutes(bin + lc.binningInMinutes));

                Supplier<Stream<Signal>> streamSupplier = () -> subMap.entrySet().stream().map(Map.Entry::getValue);

                //get on time in this bin by summing the ontimes per event in each row
                double onTimeInBin = streamSupplier.get().mapToDouble(a -> a.onTimePerEvent).sum();

                //select gamma like events and seperate signal and background region
                int signal = (int) streamSupplier.get()
                        .filter(s -> s.prediction > lc.predictionThreshold) //select gamma-like events
                        .filter(s -> s.theta < lc.thetaThreshold) //from the signal region
                        .count();

                int background = (int) streamSupplier.get()
                            .filter(s -> s.prediction > lc.predictionThreshold) //select gamma-like events
                            .filter(s-> (
                                            s.theta_off_1 < lc.thetaThreshold ||
                                            s.theta_off_2 < lc.thetaThreshold ||
                                            s.theta_off_3 < lc.thetaThreshold ||
                                            s.theta_off_4 < lc.thetaThreshold ||
                                            s.theta_off_5 < lc.thetaThreshold
                            )) //from the 5 background regions
                            .count();

                LightCurveBin lightCurveBin = new LightCurveBin(lc.startTime.plusMinutes(bin*lc.binningInMinutes),
                                                                lc.startTime.plusMinutes((bin+1)*lc.binningInMinutes),
                                                                background,
                                                                signal,
                                                                lc.alpha,
                                                                onTimeInBin,
                                                                "Bla");
                lc.bins.add(lightCurveBin);

            }
            return lc;
        }
    }


    private static class LightCurveBin {
        public final OffsetDateTime startTime;
        public final OffsetDateTime endTime;
        public final int backgroundEvents;
        public final int signalEvents;
        public final double alpha;
        public final double onTimeInBin;
        public final String sourceName;
        public final double excess;


        public LightCurveBin(OffsetDateTime startTime,
                             OffsetDateTime endTime,
                             int backgroundEvents,
                             int signalEvents,
                             double alpha,
                             double onTimeInBin,
                             String sourceName) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.backgroundEvents = backgroundEvents;
            this.signalEvents = signalEvents;
            this.alpha = alpha;
            this.onTimeInBin = onTimeInBin;
            this.sourceName = sourceName;
            this.excess = signalEvents - backgroundEvents*alpha;
        }
    }
}
