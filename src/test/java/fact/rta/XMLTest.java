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
//
//    @Before
//    public void setup(){
//        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
//        l = root.getLevel();
//        root.setLevel(Level.ERROR);
//    }
//
//    @After
//    public void tearDown(){
//        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
//        root.setLevel(l);
//    }
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
