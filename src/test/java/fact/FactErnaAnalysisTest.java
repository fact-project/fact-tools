package fact;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.FITS;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static fact.RunFACTTools.runFACTTools;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class FactErnaAnalysisTest {


    @Test
    public void dataTest() {

        try {
            File xml = new File("examples/forErna/std_analysis_erna_observations.xml");
            File outFile = File.createTempFile("facttools_test", ".json");

            String[] args = {
                    xml.toURI().toString(),
                    "-Dinput=file:src/test/resources/testErnaDataInput.json",
                    "-DauxFolder=file:src/main/resources/aux/",
                    "-Doutput=" + outFile.toURI().toString(),
            };

            runFACTTools(args);

            JsonElement root = new JsonParser().parse(new InputStreamReader(outFile.toURI().toURL().openStream()));
            assertEquals(3, (long) root.getAsJsonArray().size());


        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run examples/forErna/std_analysis_erna_observations.xml for observations");
        }
    }


    @Test
    public void mcTest() {

        try {
            File xml = new File("examples/forErna/std_analysis_erna_simulations.xml");
            File outFile = File.createTempFile("facttools_test", ".json");

            String[] args = {
                    xml.toURI().toString(),
                    "-Doutput=" + outFile.toURI().toString(),
                    "-Dinput=file:src/test/resources/testErnaMCInput.json",
            };

            runFACTTools(args);

            JsonElement root = new JsonParser().parse(new InputStreamReader(outFile.toURI().toURL().openStream()));
            assertEquals(12, (long) root.getAsJsonArray().size());

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run forErna/std_analysis_erna_simulations.xml");
        }
    }
}

