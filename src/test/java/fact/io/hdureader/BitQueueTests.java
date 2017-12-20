package fact.io.hdureader;

import fact.io.hdureader.zfits.BitQueue;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the BitQueue implementation
 * Created by mackaiver on 14/12/16.
 */
public class BitQueueTests {

    @Test
    public void bitQueueTest() {
        BitQueue q = new BitQueue();

        q.addByte(Byte.parseByte("00110011", 2));
        assertThat(q.bitString(), is("00000000" + "00110011"));
        assertThat(q.queueLength, is(8));

        q.addByte(Byte.parseByte("00000000", 2));
        assertThat(q.bitString(), is("00000000" + "00110011"));
        assertThat(q.queueLength, is(16));

        q.remove(2);
        assertThat(q.bitString(), is("00000000" + "00001100"));
        assertThat(q.queueLength, is(14));

        q.remove(8);
        assertThat(q.bitString(), is("00000000" + "00000000"));
        assertThat(q.queueLength, is(6));

        q.addByte(Byte.parseByte("01010101", 2));
        assertThat(q.bitString(), is("00010101" + "01000000"));
        assertThat(q.queueLength, is(14));
        assertThat(q.bitString(), is("00010101" + "01000000"));

    }
}
