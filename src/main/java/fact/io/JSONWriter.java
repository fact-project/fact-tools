package fact.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import fact.container.PixelSet;
import stream.Data;
import stream.Keys;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.Arrays;

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
    private Keys keys = new Keys("");
    @Parameter(required = false, description = "Defines how many significant digit are used for double values", defaultValue="null")
    private Integer doubleSignDigits = null;
    @Parameter(required = false, description = "If true a list of data items is written (and therefore the output file is a valid"
    		+ "json object", defaultValue = "false")
    private boolean jsonl = false;

    @Parameter(required = false, description = "If true, PixelSets are written out as int arrays of chids", defaultValue = "true")
    private boolean pixelSetsAsInt = true;

    @Parameter(required = true)
    private URL url;

    private Gson gson;
    private StringBuffer b = new StringBuffer();
    private BufferedWriter bw;
    private String[] defaultKeys = {"EventNum", "TriggerType", "NROI", "NPIX"};
    
    boolean isFirstLine = true;

    @Override
    public Data process(Data data) {
        Data item = DataFactory.create();

        for (String key: defaultKeys ){
            item.put(key, data.get(key));
        }

        for (String key: keys.select(data) ){
            item.put(key, data.get(key));
        }

        try {
        	if (isFirstLine)
        	{
        		isFirstLine = false;
        	}
        	else
        	{
        		if (!jsonl)
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

        GsonBuilder gsonBuilder  = new GsonBuilder().serializeSpecialFloatingPointValues();
        if (doubleSignDigits != null) {
            DoubleAdapter doubleAdapter = new DoubleAdapter();
            doubleAdapter.setSignDigits(doubleSignDigits);
            gsonBuilder.registerTypeAdapter(double.class, doubleAdapter)
                    .registerTypeAdapter(Double.class, doubleAdapter)
                    .enableComplexMapKeySerialization();
        }

        if (pixelSetsAsInt){
            gsonBuilder.registerTypeAdapter(PixelSet.class, new PixelSetAdapter());
        }

        gson = gsonBuilder.create();

        if (!jsonl)
        {
        	bw.write("[");
        }
    }

    @Override
    public void resetState() throws Exception {}

    @Override
    public void finish() throws Exception {
        try {

            if(bw != null) {
                if (!jsonl)
                {
                    bw.write("]");
                }
            }
        } catch (IOException e){
            // ignore stream bw was closed apparently
        } finally {
            if (bw != null){
                bw.close();
            }
        }
    }



    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setJsonl(boolean jsonl) {
        this.jsonl = jsonl;
    }

    public void setDoubleSignDigits(int doubleSignDigits) {
		this.doubleSignDigits = doubleSignDigits;
	}

	public void setPixelSetsAsInt(boolean pixelSetsAsInt) {
        this.pixelSetsAsInt = pixelSetsAsInt;
    }

    public class PixelSetAdapter extends TypeAdapter<PixelSet>{

        @Override
        public void write(JsonWriter jsonWriter, PixelSet pixelSet) throws IOException {
            if (pixelSet == null){
                jsonWriter.nullValue();
            }
            jsonWriter.beginArray();
            for (int chid: pixelSet.toIntArray()){
                jsonWriter.value(chid);
            }
            jsonWriter.endArray();
        }

        @Override
        public PixelSet read(JsonReader jsonReader) throws IOException {
            return null;
        }
    }

    public class DoubleAdapter extends TypeAdapter<Double> {

        private int signDigits;

        public Double read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL)
            {
                reader.nextNull();
                return null;
            }
            double x = reader.nextDouble();
            return x;
        }

        public void write(JsonWriter writer, Double value) throws IOException {
            if (value.isNaN() || value.isInfinite())
            {
                writer.value(value);
            }
            else
            {
                BigDecimal bValue = new BigDecimal(value);
                bValue = bValue.round(new MathContext(signDigits));
                writer.value(bValue.doubleValue());
            }
        }

        public void setSignDigits(int sD){
            signDigits = sD;
        }

    }

}
