package fact.filter;

import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.net.URL;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <p>
 * This processor handles the Pulsar time correction. It requires a Pulsar corrected time
 * events file either as URL and will read the BAT (Barycentered Arrival Time / MJD)
 * data from that. Furthermore it will read the phase of the Crab Pulsar from
 * the same file. The corrected time and phase datum is then applied to the corresponding
 * FactEvent processed by the process method.
 * </p>
 *
 * @author Max Ahnen &lt;mknoetig@phys.ethz.ch&gt;
 *
 */
class PulsarTimeEvent
{
    public Integer eventNum=0;
    public Integer unixTimeSec=0;
    public Integer unixTimeMuSec=0;
    public Integer gpsUnixTimeSec=0;
    public Integer gpsUnixTimeMuSec=0;
    public Double mjd=0.;
    public Double bat=0.;
    public Double phase=0.;

    public PulsarTimeEvent(){};
};

public class PulsarTimeCorrection implements Processor {

    static Logger log = LoggerFactory.getLogger(PulsarTimeCorrection.class);

    @Parameter(required=true,description="Key of the event mjd output")
    private String mjdKey;
    @Parameter(required=true,description="Key of the event barycentered arrival time (bat(mjd)) output")
    private String batKey;
    @Parameter(required=true,description="Key of the event Crab phase output")
    private String phaseKey;

    /**
     * This is the structure to hold the pulsar timing data in [double,double,double]: [mjd bat(mjd) phase].
     * It is filled by the constructor and
     * can be accessed by the key: EventNum
     */
    public TreeMap<Integer, Double[]> pulsarData = new TreeMap<Integer, Double[]>();

    /**
     * This is the structure to hold the pulsar timing data in [int,int]: [seconds microseconds].
     * It is filled by the constructor and
     * can be accessed by the key: EventNum
     */
    public TreeMap<Integer, Integer[]> pulsarTimes = new TreeMap<Integer, Integer[]>();

    /**
     * This is the method called in the process loop.
     * All it does is to call pulsarData with the EventNum and get back the
     * phase corresponding to the EventNum
     */
    @Override
    public Data process(Data input) {
        //Check that the eventnum exists
        Utils.isKeyValid(input, "EventNum", Integer.class);

        Integer triggerType = new Integer( input.get( "TriggerType").toString() );

        //Check that the trigger type is 4, equivalent to the physics trigger
        if ( !triggerType.equals(4) ) {
            log.error("Non-pysics trigger type detected: "+input.get("TriggerType")+" Please cut on physics triggers for gps time correction.");
            throw new RuntimeException(
                    "Non-pysics trigger type detected: "+input.get("TriggerType")+" Please cut on physics triggers for gps time correction.");
        }

        Double[] bufPulsarData = pulsarData.get( input.get("EventNum") );
        //check that the correct files were chosen
        if (bufPulsarData==null)
        {
            log.error("Couldn't find corresponding EventNum. Be sure to have equal Pulsar EventTiming file and run file.");
            throw new RuntimeException(
                    "Couldn't find corresponding EventNum. Be sure to have equal Pulsar EventTiming file and run file.");
        }

        //get the data in buffers and check for consistency
        Double mjd 	= bufPulsarData[0];
        Double bat 	= bufPulsarData[1];
        Double phase= bufPulsarData[2];

        Integer[] bufPulsarTimes = pulsarTimes.get( input.get("EventNum") );
        //check that the correct files were chosen
        if (bufPulsarTimes==null)
        {
            log.error("Couldn't find corresponding EventNum. Be sure to have equal Pulsar EventTiming file and run file.");
            throw new RuntimeException(
                    "Couldn't find corresponding EventNum. Be sure to have equal Pulsar EventTiming file and run file.");
        }

        //get the data in buffers and check for consistency
        Integer unixTimeSec 	= bufPulsarTimes[0];
        Integer unixTimeMuSec 	= bufPulsarTimes[1];
        int[] utc = (int[]) input.get( "UnixTimeUTC");

        if ( !unixTimeSec.equals(utc[0]) )
        {
            log.error("UnixTimeSec in Pulsar Time Correction file not equal to UnixTimeSec in stream");
            throw new RuntimeException(
                    "UnixTimeSec in Pulsar Time Correction file not equal to UnixTimeSec in stream");
        }
        if ( !unixTimeMuSec.equals(utc[1]) )
        {
            log.error("UnixTimeMuSec in Pulsar Time Correction file not equal to UnixTimeMuSec in stream");
            throw new RuntimeException(
                    "UnixTimeMuSec in Pulsar Time Correction file not equal to UnixTimeMuSec in stream");
        }

        input.put(mjdKey, mjd);
        input.put(batKey, bat);
        input.put(phaseKey, phase);
        return input;
    }

    // This method is called from the setter that is called in the beginning.
    // It loads the SourceURL file into memory for further processing.
    protected void loadPulsarTimeCorrection(SourceURL  in) {
        try {
            //open file and get the data as a list
            ArrayList<PulsarTimeEvent> fileData = getPulsarTimeDataFromFile( in.getPath() ); //make a list of event number + the different event data
            this.pulsarData = getPulsarDataMapFromList( fileData ); //copy this event number list into a map that is accessible by id: event number
            this.pulsarTimes = getPulsarTimeMapFromList( fileData ); //copy this event number list into a map that is accessible by id: event number

        } catch (Exception e) {

            log.error("Failed to load pulsar timing data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();

            this.pulsarData = null;
            this.pulsarTimes = null;
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
    * This method actually opens the data file and gets the data out from it.
    * the return is a list of PulsarTimeEvents
    */
    private ArrayList<PulsarTimeEvent> getPulsarTimeDataFromFile(String fileName) {

        ArrayList<PulsarTimeEvent> fileContents = new ArrayList<PulsarTimeEvent>();
        DataInputStream pulsarFile=null;
        try{
            pulsarFile = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));

            // count the available bytes form the input stream
            int count = pulsarFile.available();

            // create buffer
            byte[] bs = new byte[count];

            // read data into buffer
            pulsarFile.read(bs);

            ByteBuffer bb = ByteBuffer.wrap(bs);
            bb.order( ByteOrder.LITTLE_ENDIAN);
            while( bb.hasRemaining()) {

                //check the file header
                Integer n_evts = bb.getInt();
                Boolean btCheck = bb.get() != 0;
                Boolean pValueCheck = bb.get() != 0;
                Boolean toothgapCheck = bb.get() != 0;

                if (!btCheck) {
                    log.error("btCheck failed");
                    throw new RuntimeException("Bootstrapped |Mean Delta T| < 5e-6s && |Sigma Delta T| < 10e-6s test not passed.");
                }
                if (!pValueCheck) {
                    log.error("pValueCheck failed");
                    throw new RuntimeException("At least one p-value < 0.0001");
                }
                if (!toothgapCheck) {
                    log.error("toothgapCheck failed");
                    throw new RuntimeException("Not all toothgaps found");
                }

                for (int i = 0; i < n_evts; i++) {
                    PulsarTimeEvent pulsarEventBuf = new PulsarTimeEvent(); //buffer for saving data

                    pulsarEventBuf.unixTimeSec = bb.getInt();
                    pulsarEventBuf.unixTimeMuSec = bb.getInt();
                    pulsarEventBuf.gpsUnixTimeSec = bb.getInt();
                    pulsarEventBuf.gpsUnixTimeMuSec = bb.getInt();
                    pulsarEventBuf.eventNum = bb.getInt();
                    pulsarEventBuf.phase = bb.getDouble();
                    pulsarEventBuf.mjd = bb.getDouble();
                    pulsarEventBuf.bat = bb.getDouble();

                    fileContents.add(pulsarEventBuf);
                }
                pulsarFile.close();
            }
        }
        catch(IOException e) {
            log.error("Exception caught while reading pulsar event time data file: " + fileName +" "+ e.toString());
        }

        return fileContents;
    }

    /*
    * this method makes a nice int map from the PulsarTimeEvent List
    */
    private TreeMap<Integer, Integer[]> getPulsarTimeMapFromList( ArrayList<PulsarTimeEvent> PulsarTimeEventList ) {

        TreeMap<Integer, Integer[]> timeMap = new TreeMap<Integer, Integer[]>();
        for(int i = 0; i < PulsarTimeEventList.size(); i++){

            PulsarTimeEvent myPTEvent = PulsarTimeEventList.get(i);
            Integer key = Integer.valueOf( myPTEvent.eventNum ); //copy event number into Integer type
            Integer[] column = new Integer[ 4 ];
            column[0] = Integer.valueOf( myPTEvent.unixTimeSec ); //with this I will check if the time of the event is equal
            column[1] = Integer.valueOf( myPTEvent.unixTimeMuSec );
            column[2] = Integer.valueOf( myPTEvent.gpsUnixTimeSec ); //copy correctedTimes into a list of Integers ( Seconds, Microseconds )
            column[3] = Integer.valueOf( myPTEvent.gpsUnixTimeMuSec );

            timeMap.put(key,column);
        }
        return timeMap;
    }

    /*
    * this method makes a nice double map from the PulsarTimeEvent List
    */
    private TreeMap<Integer, Double[]> getPulsarDataMapFromList( ArrayList<PulsarTimeEvent> PulsarDataEventList ) {

        TreeMap<Integer, Double[]> dataMap = new TreeMap<Integer, Double[]>();
        for(int i = 0; i < PulsarDataEventList.size(); i++){

            PulsarTimeEvent myPTEvent = PulsarDataEventList.get(i);
            Integer key = Integer.valueOf( myPTEvent.eventNum ); //copy event number into Integer type
            Double[] column = new Double[ 3 ];
            column[0] = Double.valueOf( myPTEvent.mjd );
            column[1] = Double.valueOf( myPTEvent.bat );
            column[2] = Double.valueOf( myPTEvent.phase );

            dataMap.put(key,column);
        }
        return dataMap;
    }

    // ----------------- getters and setters -------------------
    public String getMjdKey() {
        return mjdKey;
    }
    public void setMjdKey(String mjdKey) {
        this.mjdKey = mjdKey;
    }
    public String getBatKey() {
        return batKey;
    }
    public void setBatKey(String batKey) {
        this.batKey = batKey;
    }
    public String getPhaseKey() {
        return phaseKey;
    }
    public void setPhaseKey(String phaseKey) {
        this.phaseKey = phaseKey;
    }

    public void setUrl(URL url) {
        try
        {
            loadPulsarTimeCorrection(new SourceURL(url));
        } catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

}