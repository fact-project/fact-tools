package fact.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * Writes a file containing a hopefully valid JSON String on each line.
 * Heres a simple Pyhton script to read it:

 import json

 def main():
    with open('test.json', 'r') as file:
        for line in file:
            event = json.loads(line)
            print(event['NROI'])

 if __name__ == "__main__":
    main()
 *
 *
 * Keep in mind that some events might have keys missing.
 * Created by bruegge on 7/30/14.
 */
public class JSONWriter implements StatefulProcessor {


    @Parameter(required = true)
    private String[] keys;

    @Parameter(required = true)
    private URL url;

    private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    private StringBuffer b = new StringBuffer();
    private BufferedWriter bw;

    @Override
    public Data process(Data data) {
        Data item = DataFactory.create();

        //converts unix time to simple date format
        if(data.containsKey("UnixTimeUTC") && data.get("UnixTimeUTC") != null) {
            int[] timestamp = (int[]) data.get("UnixTimeUTC");
            String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(((long) timestamp[0]) * 1000));
            data.put("Timestamp", date);
        }

        String[] evKeys = {"EventNum", "TriggerType", "NROI", "NPIX", "Timestamp"};
        for(String key : evKeys) {
            if (data.containsKey(key)) {
                item.put(key, data.get(key));
            }
        }

        for (String key: keys){
            item.put(key, data.get(key));
        }
        try {
            b.append(gson.toJson(item));
            bw.write(b.toString());
            bw.newLine();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        b.delete(0, b.length());
        return data;
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {
        bw= new BufferedWriter(new FileWriter(new File(url.getFile())));
    }

    @Override
    public void resetState() throws Exception {}

    @Override
    public void finish() throws Exception {
        if(bw != null) {
            bw.close();
        }
    }


    public String[] getKeys() {
        return keys;
    }
    public void setKeys(String[] keys) {
        this.keys = keys;
    }


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }


}
