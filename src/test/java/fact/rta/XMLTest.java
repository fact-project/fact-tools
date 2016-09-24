package fact.rta;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Created by kai on 23.09.16.
 */
public class XMLTest {

    @Test
    public void rtaXML() {
        try {
            URL xml = this.getClass().getResource("/rta.xml");
            String[] args = {xml.getFile()};
            stream.run.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run the example_process.xml");
        }
    }
}
