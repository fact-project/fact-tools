package fact.io.zfits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.util.parser.ParseException;

public class FitsHeader {
	static Logger log = LoggerFactory.getLogger(FitsHeader.class);

	public static enum ValueType {
		NONE(null),
		STRING(String.class),
		BOOLEAN(Boolean.class),
		INT(Integer.class),
		FLOAT(Float.class);

		private final Class<?> typeClass;
		private ValueType(Class<?> typeClass) {
			this.typeClass = typeClass; 
		}
		public Class<?> getTypeClass() {
			return this.typeClass;
		}
	}
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

	public Map<String, FitsHeaderEntry> getKeyMap() {
		return this.keyMap;
	}
	public FitsHeader(List<String> block) throws ParseException {
		log.info("Block size: "+block.size());
		keyMap = new HashMap<String, FitsHeaderEntry>();
		for (String line : block) {
			ValueType type = ValueType.NONE;
			line = line.trim();
			if (line.startsWith("COMMENT")) { //ignore comment only lines
				continue;
			}
			//get the key and everything else
			String[] tmp = line.split("=", 2);
			String key = tmp[0].trim(); //key
			line = tmp[1].trim(); //everything else
			//split the value and the comment from everything else
			tmp = line.split("/", 2);
			String value = tmp[0].trim();
			//String comment = tmp[1].trim(); //comment
			
			//check if we found an String
			if (value.startsWith("'")) {
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
					throw new ParseException("Unknown value while parsing tableheads: "+value);
				}
			}
			
			keyMap.put(key, new FitsHeaderEntry(type, value));
		}
	}
	
	public boolean check(String key) {
		return this.keyMap.containsKey(key);
	}

	public boolean check(String key, ValueType expectedType) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			return false;
		if (entry.getType()!=expectedType)
			return false;
		return true;
	}

	public boolean check(String key, ValueType expectedType, String expectedValue) throws ParseException {
		//if (!expectedType.getTypeClass().isInstance(expectedValue))
		//	throw new ParseException("The expectedValue is not of type: "+expectedType.toString());
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			return false;
		if (entry.getType()!=expectedType)
			return false;
		if (!entry.getValue().equals(expectedValue))
			return false;
		return true;
	}
	
	public void checkThrow(String key) throws ParseException {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			throw new ParseException("Missing header entry: '"+key+"'");
	}

	public void checkThrow(String key, ValueType expectedType) throws ParseException {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			throw new ParseException("Missing header entry: '"+key+"'");
		if (entry.getType()!=expectedType)
			throw new ParseException("Header entry: '"+key+"' got the wrong type: "+entry.getType().toString());
	}

	public void checkThrow(String key, ValueType expectedType, String expectedValue) throws ParseException {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry == null)
			throw new ParseException("Missing header entry: '"+key+"'");
		if (entry.getType()!=expectedType)
			throw new ParseException("Header entry: '"+key+"' got the wrong type: "+entry.getType().toString());
		if (!entry.getValue().equals(expectedValue))
			throw new ParseException("Header entry: '"+key+"' got the wrong value: "+entry.getValue()+", expected: "+expectedValue);
	}
	
	public String getKeyValue(String key) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry==null)
			throw new NullPointerException("The key: '"+key+"' is missing in the header");
		return entry.getValue();
	}
	
	public String getKeyValue(String key, String missingKeyValue) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry==null)
			return missingKeyValue;
		return entry.getValue();
	}

	/*public <N> N getKeyValue(String key, Class<N> type) {
		return this.keyMap.get(key).getValue(type);
	}
	
	public <N> N getKeyValue(String key, Class<N> type, N missingKeyValue) {
		FitsHeaderEntry entry = this.keyMap.get(key);
		if (entry==null)
			return missingKeyValue;
		return entry.getValue(type);
	}*/
	
	public String toString() {
		String s = "Entries: "+this.keyMap.size()+"\n";
		for(String key : this.keyMap.keySet()){
			FitsHeaderEntry entry = this.keyMap.get(key);
			s += "\tK: "+String.format("%8s",key)+", V: "+entry.getValue()+", T:"+entry.getType()+"\n";
		}
		return s;
	}
}