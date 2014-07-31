package fact.io;

import com.google.gson.Gson;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

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
    private String url;

    private Gson gson = new Gson();
    private StringBuffer b = new StringBuffer();
    private BufferedWriter bw;

    @Override
    public Data process(Data data) {
        //b = new StringBuffer();
        Data item = DataFactory.create();

        String[] evKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};
//
        for(String key : evKeys) {
            if (data.containsKey(key)) {
                System.out.println("Putting key in there!");
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
        URL url = new URL(getUrl());
        bw= new BufferedWriter(new FileWriter(new File(url.getFile())));
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {
        bw.close();
    }




    public String[] getKeys() {
        return keys;
    }
    public void setKeys(String[] keys) {
        this.keys = keys;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
