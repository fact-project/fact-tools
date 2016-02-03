package fact.rta;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Map;

/**
 * Created by kai on 24.01.16.
 */
public class LightCurve implements Processor {
    //TODO: use service annotation to inject rtawebservice
    static Logger log = LoggerFactory.getLogger(LightCurve.class);

    @Parameter(required = true)
    RTAWebService webService;

    @Parameter(required = false)
    int offRegions = 6;

    @Parameter(required = false , description = "Binning in seconds")
    int binning = 60;

    @Parameter(required = false , description = "Keep bins that are *history* hours old and delete older ones")
    int history = 12;

    private TreeRangeMap<DateTime, Double> lightCurve = TreeRangeMap.create();

    Range<DateTime> currentBin;

    @Override
    public Data process(Data data) {
        if (!data.containsKey("@signal")){
            log.warn("No signal in event. Ignoring.");
            return data;
        }
        int signal = (int) data.get("@signal");
        int background = (int) data.get("@background");

        double excess = signal - 1.0/offRegions  * background;

        //check if we have a new bin. If we start a new bin the excess rate is still zero
        DateTime time = DateTime.now();
        double cumulativeExcess = 0;

        Double e = lightCurve.get(time);
        if (e == null){
            currentBin = Range.closedOpen(time, time.plusSeconds(binning));
            cumulativeExcess = excess;
            lightCurve.put(currentBin, cumulativeExcess);

            webService.updateLightCurve(lightCurve);

        } else {
            cumulativeExcess += e;
            lightCurve.put(currentBin, cumulativeExcess);
        }

        System.out.println("cexcess:  " + cumulativeExcess);



        // remove old bins so we don't accumulate too many old results in memory
        Map.Entry<Range<DateTime>, Double> entry = lightCurve.getEntry(time.minusHours(history));
        if (entry != null){
            lightCurve.remove(entry.getKey());
        }



        if (data.containsKey("@datarate")){
            webService.updateDatarate((double) data.get("@datarate"));
        }
        return data;
    }


    public void setOffRegions(int offRegions) {
        this.offRegions = offRegions;
    }

    public void setBinning(int binning) {
        this.binning = binning;
    }

    public void setHistory(int history) {
        this.history = history;
    }

    public void setWebService(RTAWebService webService) {
        this.webService = webService;
    }
}
