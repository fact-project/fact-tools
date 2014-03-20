package fact.io.zfits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtil {
	public static ByteBuffer wrap(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}
	
	public static ByteBuffer create(int size) {
		byte[] data = new byte[size];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}

	public static ByteBuffer create(long size) {
		byte[] data = new byte[(int)size];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}
	
	public static void printByteArray(byte[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(String.format("%8s", Integer.toBinaryString(array[i]&0xFF)).replace(' ', '0'));
		}
	}
}
