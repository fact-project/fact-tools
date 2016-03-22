package fact.services;


import fact.PredictionService;
import org.jpmml.evaluator.ProbabilityDistribution;
import org.junit.Test;
import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Compare predictions from sklearn to those of the predictions service.
 * Created by kai on 19.03.16.
 */
public class PredictionServiceTest {

    @Test
    public void comparePredictions() throws Exception {
        URL url = PredictionServiceTest.class.getResource("/prediction_test_files/model.pmml.gz");


        URL gammas = PredictionServiceTest.class.getResource("/prediction_test_files/proton_prediction.csv.gz");

        CsvStream csvStream = new CsvStream(new SourceURL(gammas));
        csvStream.init();
        csvStream.setHeader(true);

        PredictionService predictionService = new PredictionService();
        predictionService.setUrl(new SourceURL(url));
        predictionService.init();

        for (int i = 0; i < 2000; i++) {
            Data data = csvStream.readNext();
            ProbabilityDistribution predict = predictionService.predict(data);
            assertThat(predict.getProbability("1"), is(data.get("label_prediction")));
        }
    }
}
