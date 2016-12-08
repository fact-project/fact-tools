package fact.io.zfits;

import fact.io.zfits.HuffmanCoder.DecodingException;
import fact.io.zfits.HuffmanCoder.EncodingException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class HuffmanTest {

	private final static short[] data1 =  {-29809,28065,20278,-32513,8150,28686,22158,-28528,6076,-25500,-7728,-18355,-17653,-14284,30473,32654,-28105,8046,-16589,-27978,-2935,31363,-24578,20165,-27514,31187,-7249,-18351,26754,24050,-31108,8162,4611,17103,-27926,17894,-26937,749,-28431,27657,-17864,29504,-21937,-22586,5195,20214,-22913,24664,25683,-370,-7672,25891,-11469,-22892,-24394,1059,27534,-21069,-10521,13356,16336,30183,5282,-3007,14777,-9397,31319,-6996,-17653,13944,10325,-26819,26095,16578,-2744,11660,31094,-26030,-19808,-26972,708,26731,25455,18491,12302,-5552,-11649,30010,13552,-27732,-2165,24020,-17785,16522,2028,-30102,16830,9600,4269}; 

	private final static short[] data2 = {16,5,20,11,11,7,10,9,19,12,3,9,1,20,10,12,20,1,7,14,6,9,13,9,1,8,2,2,12,5,14,17,17,12,9,16,3,11,2,19,13,13,8,18,20,15,13,4,20,7,9,20,10,11,18,7,10,4,20,14,2,1,6,5,3,18,10,8,9,20,6,18,17,6,20,2,5,19,4,19,2,11,8,13,12,18,5,5,6,10,10,5,19,17,15,19,19,11,7};

	public byte[] shortToByteArray(short[] data) {
		byte[] byteData = new byte[data.length*2];
		for (int i=0; i<data.length; i++) {
			byteData[i*2] = (byte)data[i];
			byteData[i*2+1] = (byte)(data[i]>>8);
		}
		return byteData;
	}
	
	public short[] randomShortArray(int count, int min, int max) {
		short[] data = new short[count];
		for (int i=0; i<count; i++)
			data[i] = (short)(min + (int)(Math.random() * ((max - min) + 1)));
		return data;
	}

	public void testEnDeCoding(short[] input) throws EncodingException, DecodingException {
		byte[] compressedData = HuffmanCoder.compressData(input);
		byte[] decompressedData = HuffmanCoder.uncompressData(compressedData);
		
		//System.out.println("Normal       length: "+input.length);
		//System.out.println("Compressed   length: "+compressedData.length);
		//System.out.println("Decompressed length: "+decompressedData.length);
		byte[] byteInput = shortToByteArray(input);
		//log.info("\tCompression: {}%", (compressedData.length*1.0)/(decompressedData.length*1.0)*100);
		assertEquals("The length of the input and the decompressed data missmatch.", byteInput.length, decompressedData.length);
		for (int i=0; i<input.length; i++) {
			assertEquals("The compression didn't work right, position: '"+i+"' wrong",byteInput[i],decompressedData[i]);
		}
	}
	
	@Test
	public void testCoder() throws EncodingException, DecodingException {
		//test known data
		testEnDeCoding(data1);
		testEnDeCoding(data2);
		//test random data
		for (int i=0; i<10;i++) {
			testEnDeCoding(randomShortArray((i+1)*100000, -2000, 2000));
		}
	}
}
