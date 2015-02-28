package fact;

import com.google.common.collect.Collections2;
import org.apache.log4j.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.fail;

/**
 * Created by kai on 28.02.15.
 */
public class FunctionalTest {
    static Logger log = LoggerFactory.getLogger(FunctionalTest.class);
    Level l =null;

    @Before
    public void setup(){
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        l = root.getLevel();
        root.setLevel(Level.ERROR);
    }

    @After
    public void tearDown(){
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setLevel(l);
    }
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
