/**
 *
 */
package fact.utils;

import fact.Utils;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITSStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.expressions.version2.Condition;
import stream.expressions.version2.ConditionFactory;
import stream.io.CsvStream;
import stream.io.SourceURL;
import stream.shell.Run;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static java.lang.Math.ceil;

/**
 * This processors changes the order of the pixels in the data from SoftId to Chid
 *
 * @author mbulinski
 *
 */
public class InsertNoise implements Processor{
    static Logger log = LoggerFactory.getLogger(InsertNoise.class);

    @Parameter(required = false, defaultValue="0", description = "The seed to make the results reproductable if desired.")
    private long seed;

    @Parameter(required = true, description = "The file containing the noise database for the noise to be used.")
    private String noiseDatabase;

    @Parameter(required = true, description = "The folder containing the noise files.")
    private String noiseFolder;

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
    List<Data> database; // The database containing index,NIGHT,RUN_ID,Zd,NSB
    double[] bins;     // The bins containing the max_values of the different bins
    List<List<Integer>> bins_index;// The indexes of the database that are in each bin
    Random randGenerator;

    @Override
    public Data process(Data input) {
        if (bins_index == null) {
            prepareNoiseDatabase();
        }
        Utils.isKeyValid(input, this.itemBinningKey, Double.class);
        double zd_input =(double)input.get(this.itemBinningKey);

        int binNum = this.getBin(zd_input);
        int sampleSize = this.bins_index.get(binNum).size();
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
        if (this.seed==0)
            this.randGenerator = new Random();
        else
            this.randGenerator = new Random(this.seed);
    }


    private Data getNoiseEvent(int index) {
        Data dbItem = this.database.get(index);
        int night = (int)dbItem.get("NIGHT");
        int runid = (int)dbItem.get("RUNID");
        int noiseNr = (int)dbItem.get("noiseNr");
        double currents = (double)dbItem.get("currents");
        String filename = (String)dbItem.get("filename");

        File noiseFile = new File(filename);
        if (!noiseFile.exists()) { //check if file is gzip file
            throw new RuntimeException("Couldn't find noisefile: "+filename);
        }
        Data item;
        try {
            FITSStream fits = new FITSStream(new SourceURL(filename));
            //skip to the event num
            BinTableReader bintable = (BinTableReader)fits.getReader();
            bintable.goToRow(noiseNr);
            item = fits.readNext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        item.put("currents", currents);
        item.put("noiseNr", noiseNr);
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
            CsvStream databaseStream = new CsvStream(new SourceURL(this.noiseDatabase));
            Data item = databaseStream.readNext();
            while(item!=null) {
                Boolean b = (Boolean)this.condition.get(item);
                if (b == null? false : !b.booleanValue()) // if condition doesn't applies ignore element
                    continue;
                // add the entry into the database
                this.database.add(item);
                // add index to the correct bin_index
                double value = (double)item.get(this.dbBinningKey);
                int binNum = this.getBin(value);
                this.bins_index.get(binNum).add(this.database.size()-1);
                // get next item
                item = databaseStream.readNext();
            }
            log.info("Database consists of: ",this.database.size()," Pedestals.");
            for(int i=0;i<this.bins.length;i++){
                log.info("Bin: ",i,", edge: ",this.bins[i], "size: ", this.bins_index.get(i).size());
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



    public void setNoiseDatabase(String noiseDatabase) {
        this.noiseDatabase = noiseDatabase;
    }

    public void setNoiseFolder(String noiseFolder) {
        this.noiseFolder = noiseFolder;
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
}
