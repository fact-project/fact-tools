package fact;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fact.RunFACTTools.runFACTTools;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test some of the provided example xmls in the examples folder.
 * Created by kai on 28.02.15.
 */
public class FunctionalTest {
    static Logger log = LoggerFactory.getLogger(FunctionalTest.class);
    Level l = null;

    @Before
    public void setup() {
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        l = root.getLevel();
        root.setLevel(Level.ERROR);
    }

    @After
    public void tearDown() {
        org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
        root.setLevel(l);
    }

    @Test
    public void studiesXMLs() throws IOException {
        int counter = 0;
        List<Path> pathList = Files.walk(Paths.get("examples/studies"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        int size = pathList.size();

        ArrayList<String> failedFilesList = new ArrayList<>();

        for (Path f : pathList) {
            log.info("Executing xml {}", f.toString());
            String[] args = {f.toAbsolutePath().toString()};
            try {
                runFACTTools(args);
            } catch (RunFACTTools.SystemExitException e) {
                if (e.status != 0) {
                    log.error("Error executing xml: " + f, e);
                    failedFilesList.add(f.toString());
                    counter++;
                }
            } catch (Exception e) {
                log.error("Error executing xml: " + f, e);
                failedFilesList.add(f.toString());
                counter++;
            }
        }

        String msg =  counter + " of " + size + " files in  'examples/studies' failed to execute: ";
        msg += Arrays.toString(failedFilesList.toArray());

        assertEquals(msg, 0, failedFilesList.size());
    }
}
