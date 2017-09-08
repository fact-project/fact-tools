package fact;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.runtime.ProcessContainer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test some of the provided example xmls in the examples temporaryFolder.
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
            e.printStackTrace();
            fail("Could not run the example_process.xml");
        }
    }

    @Test
    public void analysisXML() {
        try {
            File arg = new File("examples/stdAnalysis/data/analysis.xml");
            ProcessContainer container = new ProcessContainer(arg.toURI().toURL());
            container.run();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run the analysis.xml");
        }
    }

    @Test
    public void analysis_mcXML() {
        try {
            String[] args = {"examples/stdAnalysis/mc/analysis_mc.xml"};
            stream.run.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run the analysis_mc.xml");
        }
    }


    @Test
    public void studiesXMLs() throws IOException {
        int counter = 0;
        List<Path> pathList = Files.walk(Paths.get("examples/studies"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        int size = pathList.size();

        ArrayList<String> failedFilesList = new ArrayList<>();

        for (Path f : pathList){
            String[] args = {f.toAbsolutePath().toString()};
            try{
                stream.run.main(args);
            } catch (Exception e){
                log.error("Error executing xml: " + f, e);
                failedFilesList.add(f.toString());
                counter++;
            }
        }

        log.debug("\n\n" + counter + " of " + size + " files in  'examples/studies' failed to execute");
        log.debug(Arrays.toString(failedFilesList.toArray()));

        assertThat(failedFilesList.size(), is(0));
    }
}
