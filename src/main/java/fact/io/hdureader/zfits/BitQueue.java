package fact.io.hdureader.zfits;

/**
 * Created by mackaiver on 18/11/16.
 */
public class BitQueue {

    public int queueAsInt = 0;
    public int queueLength = 0;

    public void addByte(byte b) {
        queueAsInt |= (b & 0xFF) << queueLength;
        queueLength += 8;
    }

    public void addShort(short s) {
        queueAsInt |= (s & 0xFFFF) << queueLength;
        queueLength += 16;
    }

    public void remove(int n) {
        queueAsInt >>= n;
        queueLength -= n;
    }

    public int peekByte() {
        return queueAsInt & 0x00FF;
    }

    public String bitString() {
        return String.format("%" + 16 + "s", Integer.toBinaryString(queueAsInt)).replace(' ', '0');
    }
}
