package fact.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;

public class FitsStream extends AbstractStream {
	static Logger log = LoggerFactory.getLogger(FitsStream.class);
	final static int blockSize = 2880;
	private BufferedInputStream b;
	private LinkedHashMap<String, String> typeMap = new LinkedHashMap<String, String>(15);
	int roi = 300;
	private int eventBytes;
	private DataInputStream dataStream;
	
	public FitsStream(SourceURL url){
		this(url.getFile());
	}
	
	public FitsStream(String path){
		try {
			
			//simpl,e filename cheack to see wether file is zipped or not
			InputStream fileStream = new FileInputStream(path);
			if (path.endsWith(".gz")){
				InputStream gzipStream = new GZIPInputStream(fileStream);
				b = new BufferedInputStream(gzipStream);
			} else {
				b = new BufferedInputStream(fileStream);
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init() throws Exception {
		//read the header of the fits file
		byte[] headerBytes = new byte[3* blockSize];
		int numberOfbytes = b.read(headerBytes);
		if (numberOfbytes < 3*blockSize){
			log.error("Cannot read header of fits file. Its to short.");
		}
		//Im creating a BufferdReader which allows me to read the bytes with the set encoding.
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headerBytes), "US-ASCII"));
		//the first header is completely irrelevant to us
		reader.skip(blockSize);
		
		char[] cbuf = new char[80];
		while((reader.read(cbuf))  > 0){
			String valueName = new String(cbuf);
			System.out.println(valueName);
			if(valueName.startsWith("TTYPE")){
				valueName =  valueName.split("=|'")[2]; 
				reader.read(cbuf);
				String valueType = new String(cbuf);
				if(valueType.startsWith("TFORM")){
					valueType =  valueType.split("=|'")[2];
					typeMap.put(valueName.trim(), valueType.trim());
				}
			}
			if (valueName.substring(0, 3).equals("NROI")){
				roi = Integer.parseInt(valueName.split("=|/")[1].trim()); 
			}
			if (valueName.startsWith("NAXIS1")){
				eventBytes = Integer.parseInt(valueName.split("=|/")[1].trim()); 
			}
		}
		//TODO: for all keys save the length and the type in a format which allows for efficient use of the 'case' statement
		for( String key: typeMap.keySet()){
			String type = typeMap.get(key);
			//get lenmgth and type from the string now
		}
		
		System.out.println(eventBytes);
		System.out.println(roi);
		System.out.println(typeMap.toString());
		
	}

	@Override
	public Data readNext() throws Exception {
		dataStream = new DataInputStream(b);
		

		// TODO Auto-generated method stub
		return null;
	}
}
