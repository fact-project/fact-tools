package fact.io;

import com.google.gson.Gson;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modification of the JSONWriter processor by bruegge on 7/30/14.
 * Processor writes every key:value pair in the data-item into a json file. You can provide a list of Keys you want to exclude.
 *
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
 * Created by jebuss <jens.buss@udo.edu> on 1/16/15.
 */
public class JSONWriterExcludeKeys implements StatefulProcessor {


    @Parameter(required = true)
    private String[] keys;

    @Parameter(required = true)
    private URL url;

    private Set<String> excludeKeySet;

    private Gson gson = new Gson();
    private StringBuffer b = new StringBuffer();
    private BufferedWriter bw;

    @Override
    public Data process(Data data) {
        Data item = DataFactory.create();

        Set<String> keySet = data.keySet();

        for(String key : keySet) {
            if (excludeKeySet.contains(key)){
                continue;
            }
            if (data.containsKey(key)) {
                item.put(key, data.get(key));
            }
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

       excludeKeySet = new HashSet<>(Arrays.asList(keys));
    }

    @Override
    public void resetState() throws Exception {}

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


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }


}
