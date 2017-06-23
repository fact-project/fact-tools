/**
 *
 */
package fact.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import java.io.*;


import java.util.concurrent.Semaphore;

import fact.Utils;
/**
 * This processors changes the order of the pixels in the data from SoftId to Chid
 *
 * @author mbulinski
 *
 */
public class ApplyNoise implements Processor{
    static Logger log = LoggerFactory.getLogger(ApplyNoise.class);

    //@Parameter(required = false, description = "The sampling generator to be used: ['full']")
    private String generator = "full";

    @Parameter(required = false, defaultValue = "10", description = "The maximum amount of noise events cached")
    private int maxCountNoiseCache = 10;

    //@Parameter(required = true, description = "Key refering to an array of short containing pixel data sorted by SoftId")
    private String key;

    @Parameter(required = false, defaultValue="0", description = "The seed to make the results reproductable if desired.")
    private long seed;

    @Parameter(required = true, description = "The files containing the noise.")
    private String[] noiseFiles;

    @Parameter(required = false, defaultValue = "64", description = "The default precision for the noise data to be written")
    private int floatPrecision;

    private int bytes;

    private int[] noiseFileEventCount; //Amount of noise events per file
    private int totalNoiseEventCount = 0;
	
	private int max = 1000;

	private double[][] noiseData;

    public void setKey(String key) {
        this.key = key;
    }

    private Thread generatorThread = null;

    private final Semaphore noiseCacheAmount = new Semaphore(0);

    Random randGenerator;

    private int newElem = -1;
    private int lastElem = 0;
    private double[][] noiseDataCache;


    public void generateNoiseEventFull() throws Exception {
        //int r = ThreadLocalRandom.current().nextInt(0, this.noiseFiles.length);
        int r = randGenerator.nextInt(this.totalNoiseEventCount);
        String choosenFile = "";
        for (int i=0;;i++) {
            if (r < this.noiseFileEventCount[i]) {
                choosenFile = this.noiseFiles[i];
                break;
            } else {
                r -= this.noiseFileEventCount[i];
            }
        }


        try {
            DataInputStream dr = new DataInputStream(new FileInputStream(choosenFile));
            // skip all the unwanted noise events
            dr.skipBytes(r * 1440*300*this.bytes);
            //insert into last element
            for (int j = 0; j < 1440*300*this.bytes; j++) {
                if (this.floatPrecision==64)
                    this.noiseDataCache[this.newElem][j] = dr.readDouble();
                else if (this.floatPrecision==32)
                    this.noiseDataCache[this.newElem][j] = (double)dr.readFloat();
                else
                    this.noiseDataCache[this.newElem][j] = dr.readDouble();
            }
        } catch(Exception e){
            log.error("Error generating noise from file: "+choosenFile);
            throw new Exception(e);
        }
    }

    public void generateNextNoiseElement() throws Exception {
        // check if full
        while(this.noiseCacheAmount.availablePermits()==this.maxCountNoiseCache) {
            Thread.sleep(200);
        }
        this.newElem++;
        if (this.newElem==this.maxCountNoiseCache)
            this.newElem = 0;
        if (generator=="full") {
            generateNoiseEventFull();
        } else {
            throw new Exception("Unknown generator: "+generator);
        }
        noiseCacheAmount.release();
    }

    public void setMaxCountNoiseCache(int value) {
        this.maxCountNoiseCache = value;
    }
    public void setFloatPrecision(int pre) { this.floatPrecision = pre; this.bytes=this.floatPrecision/8; }

    public void setSeed(long seed) {
        this.seed = seed;
        if (this.seed==0)
            this.randGenerator = new Random();
        else
            this.randGenerator = new Random(this.seed);
    }

    public void setNoiseFiles(String[] outputkey) throws Exception {
        this.noiseFiles = outputkey;
        // calculate length of each file and thus the amount of elements
        this.noiseFileEventCount = new int[this.noiseFiles.length];
        // calculate length of each file and thus the amount of elements
        for (int i =0; i< this.noiseFiles.length; i++) {
            String noiseFile = this.noiseFiles[i];
            File f = new File(noiseFile);
            long size = f.length();
            int count = (int)(size /(1440*300*this.bytes));
            if (size%(1440*300*this.bytes)!=0) {
                log.info(String.format("Size: %d Count: %d", size, count));
                log.error("File: "+noiseFile+" is broken.");
                throw new Exception("One of the noisefiles is broken");
            }
            this.noiseFileEventCount[i] = count;
            this.totalNoiseEventCount += count;
        }
        log.info("Amount of noise files: ");
        log.info("Total amount of noise events: ", this.totalNoiseEventCount);
    }

    @Override
    public Data process(Data input) {
        //start the generator if it isn't running yet
        if (this.generatorThread==null) {
            this.generatorThread = new Thread() {
                public void run() {
                    noiseDataCache = new double[maxCountNoiseCache][1440*300*8];
                    while(true) {
                        try {
                            generateNextNoiseElement();
                        } catch (Exception e) {
                            System.out.println(e.getCause());
                            System.out.println(e.getMessage());
                            System.out.println(e.getStackTrace());
                            e.printStackTrace();
                            throw new RuntimeException("Bad stuff happend, "+e.getMessage());
                        }
                    }
                }
            };
            this.generatorThread.start();
        }
        Utils.isKeyValid(input, "DataCalibrated", double[].class);

        double[] data = (double[]) input.get("DataCalibrated");

        try {
            noiseCacheAmount.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        for (int i=0; i<1440*300; i++) {
            data[i] += this.noiseDataCache[this.lastElem][i];
        }
        this.lastElem++;
        if(this.lastElem==this.maxCountNoiseCache) {
            this.lastElem = 0;
        }
        input.put("DataCalibrated", data);
        return input;
    }
}
