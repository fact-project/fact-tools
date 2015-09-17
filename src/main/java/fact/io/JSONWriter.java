package fact.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fact.io.gsonTypeAdapter.DoubleAdapter;
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
    @Parameter(required = false, description = "Defines how many significant digit are used for double values", defaultValue="null")
    private Integer doubleSignDigits = null;
    @Parameter(required = false, description = "If true a list of data items is written (and therefore the output file is a valid"
    		+ "json object", defaultValue = "false")
    private boolean writeListOfItems = false;

    @Parameter(required = true)
    private URL url;

    private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    private StringBuffer b = new StringBuffer();
    private BufferedWriter bw;
    
    boolean isFirstLine = true;

    @Override
    public Data process(Data data) {
        Data item = DataFactory.create();

        String[] evKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};
        for(String key : evKeys) {
            if (data.containsKey(key)) {
                item.put(key, data.get(key));
            }
        }
        for (String key: keys){
            item.put(key, data.get(key));
        }
        try {
        	if (isFirstLine)
        	{
        		isFirstLine = false;
        	}
        	else
        	{
        		if (writeListOfItems)
        		{
        			bw.write(",");
        		}
    			bw.newLine();
        	}
            b.append(gson.toJson(item));
            bw.write(b.toString());
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        b.delete(0, b.length());
        return data;
    }


    @Override
    public void init(ProcessContext processContext) throws Exception {
        bw = new BufferedWriter(new FileWriter(new File(url.getFile())));
        if (writeListOfItems)
        {
        	bw.write("[");
        }
    }

    @Override
    public void resetState() throws Exception {}

    @Override
    public void finish() throws Exception {
        if(bw != null) {
        	if (writeListOfItems)
        	{
        		bw.write("]");
        	}
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


	public void setDoubleSignDigits(int doubleSignDigits) {
		this.doubleSignDigits = doubleSignDigits;
		DoubleAdapter doubleAdapter = new DoubleAdapter();
		doubleAdapter.setSignDigits(doubleSignDigits);
		gson = new GsonBuilder()
				.serializeSpecialFloatingPointValues()
				.registerTypeAdapter(double.class, doubleAdapter)
				.registerTypeAdapter(Double.class, doubleAdapter)
				.enableComplexMapKeySerialization()
				.create();
	}


	public void setWriteListOfItems(boolean writeListOfItems) {
		this.writeListOfItems = writeListOfItems;
	}


}
