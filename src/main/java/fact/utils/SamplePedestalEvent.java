/**
 *
 */
package fact.utils;

import fact.Utils;
import fact.container.PreviousEventInfoContainer;
import fact.io.hdureader.FITSStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.expressions.version2.Condition;
import stream.expressions.version2.ConditionFactory;
import stream.io.JSONStream;
import stream.io.SourceURL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.ceil;

/**
 * This processors changes the order of the pixels in the data from SoftId to Chid
 *
 * @author mbulinski
 *
 */
public class SamplePedestalEvent implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(SamplePedestalEvent.class);

    @Parameter(required = false, defaultValue="0", description = "The seed to make the results reproductable if desired.")
    private long seed;

    @Parameter(required = true, description = "The file containing the noise database for the noise to be used.")
    private SourceURL noiseDatabase;

    @Parameter(required = true, description = "The folder containing the data files.")
    private String dataFolder;

    @Parameter(required = false, defaultValue = "", description = "The value to prepend all keys from the noise file." +
                    " (Exp: prependKey='Noise_', 'ZdPointing'->'Noise_ZdPointing'")
    private String prependKey = "";

    @Parameter(required = true, description = "The condition for the noise which will be used")
    private String noiseCondition;
    private Condition condition;

    @Parameter(required = true, description="The binning of the zenith-angle. If single value: steplenght, bins into the given steplength"+
                                            "If multivalue: Each value is the length of a single step starting with zenith=0.")
    private String[] binning;

    @Parameter(required = true, description = "The binning key of the double value from the noise database.")
    private String dbBinningKey;

    @Parameter(required = true, description = "The binning key of the double value from the item.")
    private String itemBinningKey;

    // Data for faster binning
    List<Data> database; // The database containing index,NIGHT,RUN_ID,${dbBinningKey},currents
    double[] bins;     // The bins containing the max_values of the different bins
    List<List<Integer>> bins_index;// The indexes of the database that are in each bin
    Random randGenerator;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, this.itemBinningKey, Double.class);
        double zd_input =(double)input.get(this.itemBinningKey);

        int binNum = this.getBin(zd_input);
        int sampleSize = this.bins_index.get(binNum).size();
        if (sampleSize==0) {
            log.error("Samplesize of bin: "+binNum+" is too small");
            return null;
        }
        int rand = this.randGenerator.nextInt(sampleSize);
        int index = this.bins_index.get(binNum).get(rand);
        Data item = this.getNoiseEvent(index);
        // insert the result into the input item
        for (String s : item.keySet()) {
            input.put(this.prependKey+s, item.get(s));
        }

        return input;
    }


    public void setNoiseCondition(String noiseCondition) {
        this.noiseCondition = noiseCondition;
        ConditionFactory factory = new ConditionFactory();
        this.condition = factory.create(noiseCondition);
    }


    public void setBinning(String[] binning) {
        this.binning = binning;
        if (binning.length==1) { // single value
            double steplength = Double.parseDouble(this.binning[0]);
            int numBins = (int)ceil(90.0/steplength);
            this.bins = new double[numBins];
            for (int i=0; i<numBins; i++) {
                this.bins[i] = steplength*(i+1);
            }
        } else {
            //this.bins = this.binning;
        }
    }


    public void setSeed(long seed) {
        this.seed = seed;
    }

    private String createPathFromNightAndRunId(int night, int runID) {
        String nightStr = String.format("%d", night);
        String year = nightStr.substring(0,4);
        String month = nightStr.substring(4,6);
        String day = nightStr.substring(6,8);
        return String.format("%s/%s/%s/%d_%03d", year, month, day, night, runID);
    }

    private String getPathWithExt(String fullpath) {
        // test .fz
        File noiseFile = new File(fullpath+".fits.fz");
        if (noiseFile.exists()) {
            return fullpath+".fz";
        }
        // test .gz
        noiseFile = new File(fullpath+".fits.gz");
        if (noiseFile.exists()) {
            return fullpath+".gz";
        }
        throw new RuntimeException("Couldn't find data file: "+fullpath+".fits.*");
    }

    private String getDrsPath(String startFolder, int night, int drs0, int drs1) {
        String drspath = String.format("%s/%s.drs.fits.gz", startFolder, createPathFromNightAndRunId(night, drs0));
        File drsFile = new File(drspath);
        if (drsFile.exists()) {
            return drspath;
        }

        drspath = String.format("%s/%s.drs.fits.gz", startFolder, createPathFromNightAndRunId(night, drs1));
        drsFile = new File(drspath);
        if (drsFile.exists()) {
            return drspath;
        }
        throw new RuntimeException("Couldn't find drs file: "+drspath+".*");
    }

    private Data getNoiseEvent(int index) {
        Data dbItem = this.database.get(index);
        int night = (int)dbItem.get("NIGHT");
        int runid = (int)dbItem.get("RUNID");
        int noiseNr = (int)dbItem.get("eventNr");
        int drs0 = (int)dbItem.get("drs0");
        int drs1 = (int)dbItem.get("drs1");
        double currents = (double)dbItem.get("currents");
        String source = (String)dbItem.get("source");
        //String filename = (String)dbItem.get("filename");
        //String fullpath = this.noiseFolder+"/"+filename;
        String fullpath = this.dataFolder+"/"+createPathFromNightAndRunId(night, runid);
        fullpath = getPathWithExt(fullpath);

        String drsPath = getDrsPath(this.dataFolder, night, drs0, drs1);

        Data item;
        PreviousEventInfoContainer previousEventInfo = new PreviousEventInfoContainer();
        try {
            FITSStream fits = new FITSStream(new SourceURL("file:"+fullpath));
            fits.init();
            //calculate how many previous events should be read
            int prevEventsCount = 20;
            int startPrevEvents = noiseNr - prevEventsCount;
            if (startPrevEvents<0) {
                startPrevEvents = 0;
                prevEventsCount = noiseNr;
            }
            fits.getReader().skipToRow(startPrevEvents);
            for (int i=0; i<prevEventsCount; i++) {
                item = fits.readNext();

                Utils.isKeyValid(item, "StartCellData", short[].class);
                Utils.isKeyValid(item, "NROI", Integer.class);
                Utils.isKeyValid(item, "UnixTimeUTC", int[].class);

                int[] eventTime = (int[]) item.get("UnixTimeUTC");
                short[] startCellArray = (short[])item.get("StartCellData");
                int length = (Integer) item.get("NROI");

                short[] stopCellArray = new short[startCellArray.length];
                //calculate the stopcellArray for the current event
                for (int j = 0; j < startCellArray.length; ++j) {
                    //there are 1024 capacitors in the ringbuffer
                    stopCellArray[j] = (short) ((startCellArray[j] + length) % 1024);
                }

                previousEventInfo.addNewInfo(startCellArray, stopCellArray, eventTime);
            }
            item = fits.readNext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        item.put("source", source);
        item.put("currents", currents);
        item.put("noiseNr", noiseNr);
        item.put("drspath", drsPath);
        item.put("prevEvents", previousEventInfo);
        return item;
    }

    //load the database and apply the condition and binning
    private void prepareNoiseDatabase() {
        this.database = new ArrayList<Data>(100000);
        this.bins_index = new ArrayList<List<Integer>>(this.bins.length);
        for(int i=0; i<this.bins.length; i++) {
            this.bins_index.add(new ArrayList<Integer>(100));
        }
        try {
	    log.info("Noise db: "+noiseDatabase.toString());
            JSONStream databaseStream = new JSONStream(noiseDatabase);
            databaseStream.init();

            for(Data item = databaseStream.readNext(); item!=null; item = databaseStream.readNext()) {
                Boolean b = (Boolean)this.condition.get(item);
                if (b == null? false : !b.booleanValue()) // if condition doesn't applies ignore element
                    continue;
                // add the entry into the database
                this.database.add(item);
                // add index to the correct bin_index
                double value = (double)item.get(this.dbBinningKey);
                int binNum = this.getBin(value);
                this.bins_index.get(binNum).add(this.database.size()-1);
            }
            log.info("Database consists of: "+this.database.size() + " Pedestals.");
            for(int i=0;i<this.bins.length;i++){
                log.info("Bin: "+i+", edge: " + this.bins[i] + " Size: " + this.bins_index.get(i).size());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getBin(double data) {
        int i=0;
        while(i<this.bins.length) {
            if (data<this.bins[i])
                return i;
            i++;
        }
        throw new RuntimeException("Data has a value outside of the binning");
    }

    public void setNoiseFolder(String dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void setPrependKey(String prependKey) {
        this.prependKey = prependKey;
    }

    public void setDbBinningKey(String dbBinningKey) {
        this.dbBinningKey = dbBinningKey;
    }

    public void setItemBinningKey(String itemBinningKey) {
        this.itemBinningKey = itemBinningKey;
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.info(this.noiseDatabase.toString());
        if (this.seed==0) {
            this.randGenerator = new Random();
        } else {
            this.randGenerator = new Random(this.seed);
        }
        prepareNoiseDatabase();
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
