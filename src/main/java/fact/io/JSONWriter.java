package fact.io;

import com.google.gson.Gson;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

/**
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
        for (String key : keys){
            b.append(gson.toJson(data.get(key)));
        }
        try {
            bw.write(b.toString());
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
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
