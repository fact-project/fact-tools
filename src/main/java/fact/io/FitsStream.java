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
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;

public class FitsStream extends AbstractStream {
	static Logger log = LoggerFactory.getLogger(FitsStream.class);
	final static int blockSize = 2880;
	private BufferedInputStream bufferedStream;
	int roi = 300;
	private int eventBytes;
	private DataInputStream dataStream;
	private int[] lengthArray;
	private String[] nameArray;
	private String[] typeArray;
	
	public FitsStream(SourceURL url){
		this(url.getFile());
	}
	
	public FitsStream(String path){
		try {
			
			//simpl,e filename cheack to see wether file is zipped or not
			InputStream fileStream = new FileInputStream(path);
			if (path.endsWith(".gz")){
				InputStream gzipStream = new GZIPInputStream(fileStream);
				bufferedStream = new BufferedInputStream(gzipStream);
			} else {
				bufferedStream = new BufferedInputStream(fileStream);
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
		byte[] headerBytes = new byte[5* blockSize ];
		//mark position in bufferedStream so we can reset it later ti that position
		bufferedStream.mark(10* blockSize);
		int numberOfbytes = bufferedStream.read(headerBytes);
		if (numberOfbytes < 3*blockSize){
			log.error("Cannot read header of fits file. Its to short.");
		}
		//Im creating a BufferdReader which allows me to read the bytes with the set encoding.
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headerBytes), "US-ASCII"));
		//the first header is completely irrelevant to us
		reader.skip(blockSize);
		
		int numberOfFields = 0;
		char[] cbuf = new char[80];
		while((reader.read(cbuf))  > 0){
			String valueName = new String(cbuf);
			System.out.println(valueName);
			if(valueName.startsWith("TFIELDS")){
				valueName =  valueName.split("=|/")[1];
				numberOfFields = Integer.parseInt(valueName.trim());
			}
			else if(valueName.trim().startsWith("END"))
			{
				break;
			}
		}
		if(numberOfFields == 0){
			throw new IOException("Fits file appears to have 0 fields");
		}
		typeArray = new String[numberOfFields];
		nameArray = new String[numberOfFields];
		lengthArray = new int[numberOfFields];
		
		
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headerBytes), "US-ASCII"));
		//the first header is completely irrelevant to us
		reader.skip(blockSize);
		// read number of fields
		while((reader.read(cbuf))  > 0){
			String valueName = new String(cbuf);
			System.out.println(valueName);
			if (valueName.substring(0, 3).equals("NROI"))
			{
				roi = Integer.parseInt(valueName.split("=|/")[1].trim()); 
			}
			else if (valueName.startsWith("NAXIS1"))
			{
				eventBytes = Integer.parseInt(valueName.split("=|/")[1].trim()); 
			}
			//keys name
			else if(valueName.startsWith("TTYPE"))
			{
				String[] split = valueName.split("=|'");
				int number = Integer.parseInt((split[0].replaceAll("\\D+", "")).trim());
				nameArray[number-1] =  split[2];
			}
			//type name . ie. J,I,b....
			else if(valueName.startsWith("TFORM"))
			{
				String[] split = valueName.split("=|'");
				int number = Integer.parseInt((split[0].replaceAll("\\D+", "")).trim());
				typeArray[number-1] =  split[2].replaceAll("\\d+", "").trim();
				lengthArray[number-1] =  Integer.parseInt((split[2].replaceAll("\\D+", "")).trim());

//				typeMap.put(valueName.trim(), valueType.trim());
			}
			else if(valueName.trim().startsWith("END"))
			{
				break;
			}
			
			
		}
		bufferedStream.reset();
		bufferedStream.skip(4*blockSize);
		dataStream = new DataInputStream(bufferedStream);

//		System.out.println("numberOffields: " + numberOfFields + " namemap.size" + nameMap.size());
		//TODO: for all keys save the length and the type in a format which allows for efficient use of the 'case' statement
//		System.out.println(eventBytes);
//		System.out.println(roi);

		
	}

	@Override
	public Data readNext() throws Exception {
		Data item = DataFactory.create();
		
		for(int n = 0; n < nameArray.length;  n++ ){
			
			if(typeArray[n].equals("J")){
				int numberOfelements = lengthArray[n];
				
				if(numberOfelements > 1){
					int[] el =  new int[numberOfelements];
					for(int i = 0;  i < numberOfelements; i++ ){
						el[i] = dataStream.readInt();
					}		
					item.put(nameArray[n], el);
				} else if (numberOfelements ==1) {
					item.put(nameArray[n], dataStream.readInt());
				}
			}
			
			if(typeArray[n].equals("B")){
				int numberOfelements = lengthArray[n];
				
				if(numberOfelements > 1){
					byte[] el =  new byte[numberOfelements];
					for(int i = 0;  i < numberOfelements; i++ ){
						el[i] = dataStream.readByte();
					}		
					item.put(nameArray[n], el);
				} else if (numberOfelements ==1) {
					item.put(nameArray[n], dataStream.readByte());
				}
			}
			
			
			if(typeArray[n].equals("I")){
				int numberOfelements = lengthArray[n];
				
				if(numberOfelements > 1){
					short[] el =  new short[numberOfelements];
					for(int i = 0;  i < numberOfelements; i++ ){
						el[i] = dataStream.readShort();
					}		
					item.put(nameArray[n], el);
				} else if (numberOfelements ==1) {
					item.put(nameArray[n], dataStream.readShort());
				}
			}
		}
//		System.out.println("read event " + item.toString());
//			String value = typeMap.get(key);
//			String type = value.replaceAll("\\d+", "");
//			String number = (value.replaceAll("\\D+", ""));
//			int numberOfElements = 1;
//			if(!number.trim().equals("")){
//				numberOfElements = Integer.parseInt(number.trim());
//			}
//			if(numberOfElements == 0){
//				break;
//			}
//			if(type.equals("J")){
//				dataStream.readInt();
//			}
//			if(type.equals("B")){
//				dataStream.readUnsignedByte();
//			}
//			if(type.equals("I")){
//				//this is a 16 bit int
//				dataStream.readByte();
//			}
//			if(type.equals("E")){
//				dataStream.readFloat();
//			}
//			if(type.equals("D")){
//				dataStream.readDouble();
//			}
////			System.out.println(type);
////			System.out.println(numberOfElements);
//		}


		
		new fact.processors.Short2Float().process(item);
		
		
		return item;
	}
	
}