package fact.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 12.08.14.
 */
public class RemappingTest {

    short[] data = new short[1440 * 300];
    short[] remappedData = new short[1440 * 300];

    @Before
    public void createMockList() {
        //should be 300
        int roi = data.length / 1440;
        //write the pixelnumber into the array
        for (short pix = 0; pix < 1440; pix++) {
            for (int slice = 0; slice < 300; slice++) {
                int pos = roi * pix + slice;
                data[pos] = pix;
            }
        }
    }

    @Test
    public void testRemapping() {
        //pretending the data array is given by softid lets remap it to chid
        Remapping re = new Remapping();
        int roi = data.length / 1440;
        assertTrue("First array element should contain 0", data[0] == 0);
        re.remapFromSoftIdToChid(data, remappedData);
        //chid 0 is softid 1348
        assertTrue("Remapping went wrong. First element should be 1348", remappedData[0] == 1348);
        //chid 1 is softid 1419
        assertTrue("Remapping went wrong. Element should be 890", remappedData[1 * roi] == 1419);
        //chid 0 is softid 393
        assertTrue("Remapping went wrong. Element should be 890", remappedData[393 * roi] == 0);
    }


}
