package fact.io.zfits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.util.parser.ParseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class containing the information about the fitsheader.
 * 
 * @author Michael Bulinski
 */
public class FitsHeader {

	/**
	 * The posible Valuetypes of the values in the fits header.
	 */
	public enum ValueType {
		NONE(null),
		STRING(String.class),
		BOOLEAN(Boolean.class),
		INT(Integer.class),
		FLOAT(Float.class);

		private final Class<?> typeClass;
		ValueType(Class<?> typeClass) {
			this.typeClass = typeClass;
		}
	}

	/**
	 * Entry of a card in the fits header
	 * @author Michael Bulinski
	 */
	public static class FitsHeaderEntry {
		private ValueType type;
		private String value;

		public FitsHeaderEntry(ValueType type, String value) {
			this.type = type;
			this.value = value;
		}
		public ValueType getType() {
			return this.type;
		}
		public String getValue() {
			return this.value;
		}
		public <N> N getValue(Class<N> type) {
			return type.cast(type);
		}
	}
	
	private Map<String, FitsHeaderEntry> keyMap = null;

	/**
	 * Returns the header entries. The keys of the map are the keys of the header and the values are the corresponding values of the entry.
	 * @return The header entries of the fits header. 
	 */
	public Map<String, FitsHeaderEntry> getKeyMap() {
		return this.keyMap;
	}

	/**
	 * Parse the blocks of a fits header.
	 * @param block The block given as strings with 80 characters width.
	 * @throws ParseException if the given block is not a fits header or has errors.
	 */
	public FitsHeader(List<String> block) throws ParseException {
		//log.info("Block size: "+block.size());
		keyMap = new HashMap<String, FitsHeaderEntry>();
		for (String line : block) {
			ValueType type = ValueType.NONE;
			
			if (line.startsWith("COMMENT")) { //ignore comment only lines
				continue;
			} else if (line.startsWith("HISTORY")) { //ignore history lines
				continue;
			} else if (line.startsWith("        ")) { //ignore comment only lines
				continue;
			}
			
			line = line.trim();
			//get the key and everything else
			String[] tmp = line.split("=", 2);
			if (tmp.length != 2) {
				throw new ParseException("The card does not contain a key value pair: '"+line+"'");
			}
			String key = tmp[0].trim(); //key
			line = tmp[1].trim(); //everything else
			
			//split the value and the comment from everything else
			tmp = line.split("/", 2);
			if (tmp.length==2) {
				//String comment = tmp[1].trim(); //comment
			}
			String value = tmp[0].trim();
			
			//check the type of the value
			if (value.startsWith("'")) { //we found a String
				value = value.replaceAll("'", "");
				type = ValueType.STRING;
			} else {
				if (value.isEmpty() || value.startsWith("T") || value.startsWith("F")) {
					type = ValueType.BOOLEAN;
				} else if (value.matches("\\d*\\.\\d*")) {
					type = ValueType.FLOAT;
				} else if (value.matches("\\d+")) {
					type = ValueType.INT;
				} else {
					throw new ParseException("Unknown value while parsing tableheads: '"+value+"'");
				}
			}
			
			keyMap.put(key, new FitsHeaderEntry(type, value));
		}
	}
	
	/**
	 * Checks if the key is present in the header
	 * @param key The key to check.
	 * @return True if the key is present.
	 */
	public boolean check(String key) {
		return this.keyMap.containsKey(key);
	}

	/**
	 * Checks if the key is present in the header and is of the expected type.
	 * @param key The key to check.
	 * @param expectedType The expected type.
	 * @return True if the key is present and of the expected type.
	 */
	public boolean check(String key, ValueType expectedType) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			return false;
		if (entry.getType()!=expectedType)
			return false;
		return true;
	}

	/**
	 * Checks if the key is present in the header and is of the expected type and has the expected value.
	 * @param key The key to check.
	 * @param expectedType The expected type.
	 * @param expectedValue The expected value given in the string representation.
	 * @return True if the key is present and of the expected type and value.
	 */
	public boolean check(String key, ValueType expectedType, String expectedValue) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			return false;
		if (entry.getType()!=expectedType)
			return false;
		if (!entry.getValue().equals(expectedValue))
			return false;
		return true;
	}
	

	/**
	 * Works just like {@link FitsHeader#check(String, ValueType)} but throws a Exception instead of returning an boolean.
	 * @param key The key to check.
	 * @param expectedType The expected type of the value.
	 * @throws ParseException Thrown if the key is missing or not of the expected type.
	 */
	public void checkThrow(String key, ValueType expectedType) throws ParseException {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			throw new ParseException("Missing header entry: '"+key+"'");
		if (entry.getType()!=expectedType)
			throw new ParseException("Header entry: '"+key+"' got the wrong type: "+entry.getType().toString());
	}

	/**
	 * Works just like {@link FitsHeader#check(String, ValueType, String)} but throws a Exception instead of returning an boolean.
	 * @param key The key to check.
	 * @param expectedType The expected type of the value.
	 * @param expectedValue The expected value given in string representation.
	 * @throws ParseException Thrown if the key is missing or not of the expected type or value.
	 */
	public void checkThrow(String key, ValueType expectedType, String expectedValue) throws ParseException {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			throw new ParseException("Missing header entry: '"+key+"'");
		if (entry.getType()!=expectedType)
			throw new ParseException("Header entry: '"+key+"' got the wrong type: "+entry.getType().toString());
		if (!entry.getValue().equals(expectedValue))
			throw new ParseException("Header entry: '"+key+"' got the wrong value: "+entry.getValue()+", expected: "+expectedValue);
	}
	
	/**
	 * Returns the value of a given key.
	 * @param key The key to get the value from.
	 * @return The value of the key.
	 * @throws NullPointerException Thrown if the key is not in the header.
	 */
	public String getKeyValue(String key) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry==null)
			throw new NullPointerException("The key: '"+key+"' is missing in the header");
		return entry.getValue();
	}
	
	/**
	 * Returns the value of a given key. If the key is missing returns to value of missingKeyValue.
	 * @param key The key to get the value from.
	 * @param missingKeyValue The value to give if the key is missing.
	 * @return The value of the key or the missingKeyValue. See description.
	 */
	public String getKeyValue(String key, String missingKeyValue) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry==null)
			return missingKeyValue;
		return entry.getValue();
	}
	
	/**
	 * Returns a String representation of the header. 
	 * @return The header as a string.
	 */
	public String toString() {
		String s = "Entries: "+this.keyMap.size()+"\n";
		for(String key : this.keyMap.keySet()){
			FitsHeaderEntry entry = this.keyMap.get(key);
			s += "\tK: "+String.format("%8s",key)+", V: "+entry.getValue()+", T:"+entry.getType()+"\n";
		}
		return s;
	}
}