/**
 * 
 */
package fact.io;

import java.io.Serializable;
import java.net.URL;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.data.DataFactory;
import stream.io.CsvWriter;
import fact.Constants;
import fact.data.EventUtils;

/**
 * <p>
 * This class writes out FACT events in CSV format. The format for each event is
 * exactly 1440 * ROI double values. By default the data is expected to be
 * contained in the &quot;Data&quot; property of the input.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class WriterTest extends CsvWriter {

	static Logger log = LoggerFactory.getLogger(WriterTest.class);
	String[] keys = {"DataCalibrated"};
	CsvWriter writer = null;
	private boolean writeTreeDescriptor = false;


	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
	}




	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		try {
			if (writer == null) {
				//File outFile = new File(file);
				log.debug("Creating new output-stream to '{}'", url);
				url = new URL(urlString);
				writer = new CsvWriter(url);
			}
			if(writeTreeDescriptor){
				writeTreeDescriptor = false;
				Data headerItem = DataFactory.create();
				try{
					headerItem.put("header", generateHeaderString(data) );
					writer.process(headerItem);
				} catch(ClassCastException e ){
					Log.error("Could not create the TreeDescriptionHeader. Wrong Datatypes");
				}
			}


			//			List<Data> pixels = EventExpander.expand(data, 1440, key, 0, 300);
			Data item  = DataFactory.create();
			for(int i = 0; i < keys.length; i++){
				Serializable value = null;
				if(data.containsKey(keys[i])){
					value = data.get(keys[i]);
				} else {
					log.info(Constants.ERROR_WRONG_KEY + keys+ ",  " + this.getClass().getSimpleName() );
					return null;
				}
				//Check if value is of the right type
				if (value.getClass().isArray()) {
					try{
						Serializable[] arr = (Serializable[]) value;
						double[] s  = EventUtils.toDoubleArray(arr);
						for(int k = 0; k < s.length; k++){
							item.put(keys[i] + "_" + k, s[k]);
						}
					} catch(ClassCastException e){
						//pass
					}
					Class<?> type = value.getClass().getComponentType();
					if(type == float.class){
						float[] s = ((float[]) value);
						for(int k = 0; k < s.length; k++){
							item.put(keys[i] + "_" + k, s[k]);
						}
					}
					else if(type == double.class){
						double[] s = ((double[]) value);
						for(int k = 0; k < s.length; k++){
							item.put(keys[i] + "_" + k, s[k]);
						}
					}
					else if(type == int.class){
						int[] s = ((int[]) value);
						for(int k = 0; k < s.length; k++){
							item.put(keys[i] + "_" + k, s[k]);
						}
					}
					else if(type == String.class){
						String[] s = ((String[]) value);
						for(int k = 0; k < s.length; k++){
							item.put(keys[i] + "_" + k, s[k]);
						}
					}
				} else {
					item.put(keys[i],value);
				}
			}
			writer.process(item);

			//			for (Data pixel : pixels) {
			//				log.debug("Writing pixel {}", pixel);
			//				writer.process(pixel);
			//			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to write file: "
					+ e.getMessage());
		}
		
		return data;
	}

	/**
	 * @see stream.io.CsvWriter#close()
	 */
	@Override
	public void finish() throws Exception {
		writer.finish();
	}

	private String generateHeaderString(Data data) throws ClassCastException{
		String headerString = new String();
		for(String key: keys){
			if (data.containsKey(key)){
				Serializable v = data.get(key);
				Class<? extends Serializable> valueType = v.getClass();
				if(valueType.isArray()){
					Class<?> type = v.getClass().getComponentType();
					if(type == Float.class){
						headerString += key+"[" + ((Float[]) v).length + "]/"+"F" ;
					}
					else if(type == float.class){
						headerString += key+"[" + ((float[]) v).length + "]/"+"F" ;
					}
					else if(type == Double.class){
						headerString += key+"[" + ((Double[]) v).length + "]/"+"D" ;
					}
					else if(type == double.class){
						headerString += key+"[" + ((float[]) v).length + "]/"+"D" ;
					}
					else if(type == Integer.class){
						headerString += key+"[" + ((Integer[]) v).length + "]/"+"I" ;
					}
					else if(type == int.class){
						headerString += key+"[" + ((int[]) v).length + "]/"+"I" ;
					}
					else if(type == String.class){
						headerString += key+"[" + ((String[]) v).length + "]/"+"C" ;
					}
				}
				else if(valueType.isAssignableFrom(int.class) || valueType.isAssignableFrom(Integer.class) ){
					headerString += key+"/"+"I";
				}
				else if(valueType.isAssignableFrom(float.class) || valueType.isAssignableFrom(Float.class) ){
					headerString += key+"/"+"F";
				}
				else if(valueType.isAssignableFrom(double.class) || valueType.isAssignableFrom(Double.class) ){
					headerString += key+"/"+"D";
				} else if(valueType.isAssignableFrom(String.class)){
					headerString += key+"/"+"C";
				}
			}
			headerString += ":";
		}
		headerString.replaceAll(":$", "");
		return headerString;
	}

	/**
	 * @return the key
	 */
	public String[] getKeys() {
		return keys;
	}
	/**
	 * @param key
	 *            the key to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}



	public boolean isWriteTreeDescriptor() {
		return writeTreeDescriptor;
	}

	public void setWriteTreeDescriptor(boolean writeTreeDescriptor) {
		this.writeTreeDescriptor = writeTreeDescriptor;
	}

}