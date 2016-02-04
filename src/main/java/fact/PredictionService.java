package fact;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kai on 02.02.16.
 */
public class PredictionService implements Service {
    static Logger log = LoggerFactory.getLogger(PredictionService.class);

    PMML pmml;
    //arguments to pass to the decision function
    Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
    private ModelEvaluator<? extends Model> modelEvaluator;

    private FieldName targetName;

    //fields used while training the model
    private List<FieldName> activeFields;

    @Parameter(required = true, description = "URL point to the .pmml model")
    SourceURL url;

    @Parameter(required = false, description = "Names for the labels which should be put into the data item")
    String[] labelNames = {"background", "signal"};

    @Parameter(required = false, description = "Names for the classes in the model")
    String[] classNames = {"0", "1"};

    public void init() {
        log.info("Loading .pmml model");
        try (InputStream is = url.openStream()) {
            Source transformedSource = ImportFilter.apply(new InputSource(is));
            pmml = JAXBUtil.unmarshalPMML(transformedSource);
        } catch (SAXException ex) {
            log.error("Could not load model from file provided at" + url);
            ex.printStackTrace();
            throw  new RuntimeException(ex);
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        }

        //build a modelevaluator from the loaded pmml file
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        modelEvaluator = modelEvaluatorFactory.newModelManager(pmml);


        log.info("Loaded model requires the following fields: " + modelEvaluator.getActiveFields().toString());

        log.info("Loaded model has targets: " + modelEvaluator.getTargetFields().toString());
        if(modelEvaluator.getTargetFields().size() > 1){
            log.error("Only models with one target variable are supported for now");
            throw new IllegalArgumentException("Provided pmml model has more than 1 target variable. This is unsupported");
        }

        targetName = modelEvaluator.getTargetField();
        activeFields = modelEvaluator.getActiveFields();
    }

    public synchronized ProbabilityDistribution predict(Data data){

        if (modelEvaluator == null){
            init();
        }
        for(FieldName activeField : activeFields){

            Object rawValue = data.get(activeField.toString());

            FieldValue activeValue = modelEvaluator.prepare(activeField, rawValue);

            arguments.put(activeField, activeValue);
        }
        try {

            Map<FieldName, ?> results = modelEvaluator.evaluate(arguments);
            Object targetValue = results.get(targetName);
            ProbabilityDistribution pD = (ProbabilityDistribution) targetValue;
            return pD;

        } catch (MissingFieldException e){
            log.warn("Event had missing fields or missing field values. Skipping to next event.");
            return null;
        } catch (ClassCastException e){
            log.error("The modell did not return a ProbalilityDistribution");
            throw new RuntimeException(e);
        }

    }

    @Override
    public void reset() throws Exception {

    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setLabelNames(String[] labelNames) {
        this.labelNames = labelNames;
    }

    public void setClassNames(String[] classNames) {
        this.classNames = classNames;
    }
}
