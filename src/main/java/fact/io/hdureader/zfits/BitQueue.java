package fact.io.hdureader.zfits;

/**
 * A queue storing single bits in one integer variable.
 * This is a FIFO queue. So new bytes are added to the end of the queue.
 * <p>
 * <p>
 * Created by mackaiver on 18/11/16.
 */
public class BitQueue {

    private int queueAsInt = 0;

    public int queueLength = 0;

    public void addByte(byte b) {
        queueAsInt |= (b & 0xFF) << queueLength;
        queueLength += 8;
    }

    public void addShort(short s) {
        queueAsInt |= (s & 0xFFFF) << queueLength;
        queueLength += 16;
    }

    /**
     * remove the n first entries in this queue
     *
     * @param n nmber of bits to remove.
     */
    public void remove(int n) {
        queueAsInt >>= n;
        queueLength -= n;
    }

    /**
     * get the first element in the queue without removing it.
     *
     * @return the first byte in the queue.
     */
    public int peekByte() {
        return queueAsInt & 0x00FF;
    }

    /**
     * Representation of this queue as a string of bits.
     *
     * @return a string containing 0s and 1s
     */
    public String bitString() {
        return String.format("%" + 16 + "s", Integer.toBinaryString(queueAsInt)).replace(' ', '0');
    }
}
