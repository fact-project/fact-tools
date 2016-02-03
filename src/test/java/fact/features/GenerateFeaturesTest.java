package fact.features;

import org.junit.Assert;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import java.net.URL;

/**
 * @author alexey
 */
public class GenerateFeaturesTest {

    @Test
    public void process () throws Exception {
        Data data = DataFactory.create();
        data.put("arg1", 1);
        data.put("arg2", 2);

        GenerateFeatures generateFeatures = new GenerateFeatures();
        generateFeatures.setCalculation("arg1 / arg2");
        generateFeatures.setName("division");
        generateFeatures.setParameterList("arg1::Integer, arg2::Integer");
        Data process = generateFeatures.process(data);

        Assert.assertEquals(0.5, process.get("division"));

        generateFeatures.setCalculation("signum(division)");
        generateFeatures.setParameterList("division::Double");
        generateFeatures.setName("signDivision");
        process = generateFeatures.process(process);

        Assert.assertEquals(1.0, process.get("signDivision"));
    }

    @Test
    public void processXML () throws Exception {
        URL resource = this.getClass().getResource("/generate-features.xml");
        stream.run.main(resource);
    }
}