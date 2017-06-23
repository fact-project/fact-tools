package fact.io;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.*;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import fact.Utils;

/**
 * Writes a keys from the data item to .bin files
 * The amount of events in the binfiles can be calculated from the
 * size of the file divided by the size of a single Event (1440*300*8 Bytes).
 */
public class BinaryNoiseWriter implements StatefulProcessor {


    @Parameter(required = true, description = "The datakey which contains the noise data")
    private String datakey = new String("DataCalibrated");

    @Parameter(required = false, description = "If true, use gzip compression")
    private boolean gzip = false;

    @Parameter(required = true)
    private URL url;

    @Parameter(required = false, defaultValue = "64", description = "The default precision for the noise data to be written")
    private int floatPrecision;

    private StringBuffer b = new StringBuffer();
    private DataOutputStream dw;

    @Override
    public Data process(Data data) {
        Utils.isKeyValid(data, datakey, double[].class);
        try {
            double[] darr = (double[])data.get(datakey);
            for (double d : darr) {
                if (this.floatPrecision==64)
                    dw.writeDouble(d);
                else if (this.floatPrecision==32)
                    dw.writeFloat((float)d);
                else
                    dw.writeDouble(d);
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return data;
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {
        if (gzip){
            GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(new File(url.getFile())));
            dw = new DataOutputStream(gzip);
        }
        else {
            dw = new DataOutputStream(new FileOutputStream(url.getFile()));
        }
    }

    @Override
    public void resetState() throws Exception {}

    @Override
    public void finish() throws Exception {
        if (dw != null){
            dw.close();
        }
    }

    public void setFloatPrecision(int pre) { this.floatPrecision = pre; }

    public void setDatakey(String datakey) {
        this.datakey = datakey;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

}
