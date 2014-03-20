/**
 * 
 */
package fact.io;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.data.DataFactory;
import stream.io.CsvWriter;

/**
 * <p>
 * This class writes out the data referred to by the "keys" attribute in the xml in a format thats hopefully readable by root.
 * So far you can only write out Number types and strings. That should be enough. 
 * </p>
 * 
 * @author Kai;
 * 
 */
public class RootASCIIWriter extends CsvWriter {

	static Logger log = LoggerFactory.getLogger(RootASCIIWriter.class);
	private boolean writeTreeDescriptor = true;


	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		setSeparator(" ");
		setHeader(false);
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		String[] tempKeys = keys;
		if(keys == null){
			log.error("No keys specified");
			throw new RuntimeException("You have to specify the keys to write");
		}
		if(writeTreeDescriptor){
			writeTreeDescriptor = false;
			Data headerItem = DataFactory.create();
			try{
				headerItem.put(" \n ", generateHeaderString(data) );
				keys = null;
				super.process(headerItem);
				keys = tempKeys;
			} catch(ClassCastException e ){
				log.error("Could not create the TreeDescriptionHeader. Wrong Datatypes");
			}
		}
		if(containsNanOrInfs(data)){
			return data;
		}
		
		Data item  = DataFactory.create();
        for (String key : keys) {
            Serializable value;
            if (data.containsKey(key)) {
                value = data.get(key);
            } else {
//				log.info(Constants.ERROR_WRONG_KEY + keys[i]+ ",  " + this.getClass().getSimpleName() );
                return null;
            }
            //Check if value is of the right type
            if (value.getClass().isArray()) {
                Class<?> type = value.getClass().getComponentType();
                if (value instanceof Number[]) {
                    Number[] s = (Number[]) value;
                    for (int k = 0; k < s.length; k++) {
                        item.put(key + "_" + k, s[k]);
                    }
                } else if (type == float.class) {
                    float[] s = ((float[]) value);
                    for (int k = 0; k < s.length; k++) {
                        if (s[k] == Float.NaN || s[k] == Float.NEGATIVE_INFINITY || s[k] == Float.POSITIVE_INFINITY) {
                            return data;
                        }
                        item.put(key + "_" + k, s[k]);
                    }
                } else if (type == double.class) {
                    double[] s = ((double[]) value);
                    for (int k = 0; k < s.length; k++) {
                        //check for nans or infs and gtfo
                        if (s[k] == Double.NaN || s[k] == Double.NEGATIVE_INFINITY || s[k] == Double.POSITIVE_INFINITY) {
                            return data;
                        }
                        item.put(key + "_" + k, s[k]);
                    }
                } else if (type == int.class) {
                    int[] s = ((int[]) value);
                    for (int k = 0; k < s.length; k++) {
                        item.put(key + "_" + k, s[k]);
                    }
                } else if (type == String.class) {
                    String[] s = ((String[]) value);
                    for (int k = 0; k < s.length; k++) {
                        item.put(key + "_" + k, s[k]);
                    }
                }
            } else {
                item.put(key, value.toString());
            }
        }

		//this will be a dirty hack to have the superclass iterate over the whole keyset in the dataitem
		//intstead of taking the keys specified by the user. since we created a whole new data item only containing the 
		// wanted keys this will output only the right keys
		keys= null;
		super.process(item);
		keys = tempKeys;
		return data;
	}

	private String generateHeaderString(Data data) throws ClassCastException{
		String headerString = "";
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
						headerString += key+"[" + ((double[]) v).length + "]/"+"D" ;
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
		//remove last colon
		headerString = headerString.substring(0, headerString.length()-1);
		return headerString;
	}

	private boolean  containsNanOrInfs(Data item){
		for(String key : keys){
            if(item.get(key) != null){
			    String repr = item.get(key).toString();
                if(repr.toLowerCase().equals("inf") ||repr.toLowerCase().equals("nan")){
                    return true;
                }
            }
        }
		return false;
	}


	public boolean isWriteTreeDescriptor() {
		return writeTreeDescriptor;
	}

	public void setWriteTreeDescriptor(boolean writeTreeDescriptor) {
		this.writeTreeDescriptor = writeTreeDescriptor;
	}

}