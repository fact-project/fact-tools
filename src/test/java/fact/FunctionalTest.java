package fact;

import com.google.common.collect.Collections2;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

/**
 * Created by kai on 28.02.15.
 */
public class FunctionalTest {
    static Logger log = LoggerFactory.getLogger(FunctionalTest.class);

    @Test
    public void exampleXML() {
        try {
            String[] args = {"examples/example_process.xml"};
            stream.run.main(args);
        } catch (Exception e) {
            fail("Could not run the example_process.xml");
        }
    }

    @Test
    public void studiesXMLs() {
        File folder = new File("examples/studies/");

        int counter = 0;
        int size = folder.listFiles().length;
        ArrayList<String> failedFilesList = new ArrayList<>();
        for (File f : folder.listFiles()){
            String[] args = {f.getAbsolutePath()};
            try{
                log.info("Running " + args[0]);
                stream.run.main(args);
            } catch (Exception e){
                failedFilesList.add(f.getName());
                counter++;
            }
        }
        log.info("\n\n" + counter + " of " + size + " files in " + folder.getName() + " failed to execute");
        log.info(Arrays.toString(failedFilesList.toArray()));


    }

}
