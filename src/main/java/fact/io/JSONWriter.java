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


/**
 * <p>
 * Writes a keys from the data item to .json files.
 * The format will be:
 * <pre>
 * [
 *   {"key1": value, ...},
 *   ...,
 *   {"key1": value, ...}
 * ]
 * </p>
 * </pre>
 * <p>
 * <code>keys</code> is evaluated using the stream.Keys class, so wild cards
 * <code>*</code>, <code>?</code> and negations with <code>!</code> are possible.
 * This will write all keys ending with `Pointing` to the json file but not AzPointing.
 * <pre>
 * &lt;fact.io.JSONWriter keys="*Pointing,!AzPointing" url="file:test.json" /&gt;
 * </pre>
 * </p>
 * <p>
 * The writer also supports the .jsonl format.
 * http://jsonlines.org/
 * To use the .jsonl format provide the key jsonl="true" in the xml.
 *
 * In this case the format will be:
 * <pre>
 * {"key1": value, ...}
 * ...
 * {"key1": value, ...}
 * </pre>
 * </p>
 * <p>
 * To be able to store special float values we use the extension of the json standard
 * found in most implementations. E. g. Google's gson, most JavaScript parsers and python's json module.
 * So we are using Infinity, -Infinity and NaN by default.
 * python's pandas das not support this format directly, so use json to load the data and then create the DataFrame:
 * <pre>
 * import json
 * import pandas as pd
 * with open('test.json', 'r') as f:
 *     data = json.load(f)
 * df = pd.DataFrame(data)
 * </pre>
 *
 * If you do not want this behaviour, you can use
 * <code>specialDoubleValuesAsString="true"</code>
 * to convert these values to json compatible strings containing "inf", "-inf" or "nan"
 * </p>
 * <p>
 * fact.container.PixelSet is converted to an array of chids by default,
 * if you want to have the full output of this container, set
 * <code>pixelSetsAsInt="false"</code>
 * </p>
 * <p>
 * The following keys are added by default to the output:
 * EventNum, TriggerType, NROI, NPIX
 * </p>
 * <p>
 * By default, the JSONWriter overwrites an existing file, if you want to append
 * (which actually only makes sense if <code>jsonl="true</code>),
 * you can use:
 * <code>append="true"</code>
 * </p>
 * Created by bruegge on 7/30/14.
 * Refactored by maxnoe on 2/2/2016
 */
public class JSONWriter implements StatefulProcessor {


    @Parameter(required = true)
    private Keys keys = new Keys("");
    @Parameter(required = false, description = "Defines how many significant digits are used for double values", defaultValue="null")
    private Integer doubleSignDigits = null;
    @Parameter(required = false, description = "If true, use jsonl format instead of json format", defaultValue = "false")
    private boolean jsonl = false;
    @Parameter(required = false, description = "If true, append to existing file else overwrite", defaultValue = "false")
    private  boolean append = false;
    @Parameter(required = false, description = "If true, PixelSets are written out as int arrays of chids", defaultValue = "true")
    private boolean pixelSetsAsInt = true;
    @Parameter(required = false, description = "If true, Infinity, -Infinity and NaN are converted to strings 'inf', '-inf' and 'nan'", defaultValue = "false")
    private boolean specialDoubleValuesAsString = false;

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
        bw = new BufferedWriter(new FileWriter(new File(url.getFile()), append));

        GsonBuilder gsonBuilder  = new GsonBuilder().serializeSpecialFloatingPointValues();
        gsonBuilder.enableComplexMapKeySerialization();

        if (specialDoubleValuesAsString){
            SpecialDoubleValuesAdapter specialDoubleValuesAdapter = new SpecialDoubleValuesAdapter();
            gsonBuilder.registerTypeAdapter(double.class, specialDoubleValuesAdapter);
            gsonBuilder.registerTypeAdapter(Double.class, specialDoubleValuesAdapter);
        }

        if (doubleSignDigits != null) {
            SignDigitsAdapter signDigitsAdapter = new SignDigitsAdapter();
            signDigitsAdapter.setSignDigits(doubleSignDigits);
            gsonBuilder.registerTypeAdapter(double.class, signDigitsAdapter);
            gsonBuilder.registerTypeAdapter(Double.class, signDigitsAdapter);
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
                bw.newLine();
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

    public void setAppend(boolean append) {
        this.append = append;
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

    public void setSpecialDoubleValuesAsString(boolean specialDoubleValuesAsString) {
        this.specialDoubleValuesAsString = specialDoubleValuesAsString;
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

    public class SignDigitsAdapter extends TypeAdapter<Double> {

        private int signDigits;

        public Double read(JsonReader reader) throws IOException {
            return null;
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

    public class SpecialDoubleValuesAdapter extends TypeAdapter<Double> {

        public Double read(JsonReader reader) throws IOException {
            return null;
        }

        public void write(JsonWriter writer, Double value) throws IOException {
            if (value == Double.NEGATIVE_INFINITY) {
                writer.value("-inf");
            }
            else if (value == Double.POSITIVE_INFINITY) {
                writer.value("inf");
            }
            else if (value.isNaN()) {
                writer.value("nan");
            }
            else{
                writer.value(value);
            }
        }
    }
}
