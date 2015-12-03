package fact;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import javax.xml.transform.Source;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Applies a .pmml model to data in the data item. The model can be written with SciKit learn for example.
 *
 * Created by kai on 03.12.15.
 */
public class ApplyModel implements StatefulProcessor{
    static Logger log = LoggerFactory.getLogger(ApplyModel.class);

    PMML pmml;
    //arguments to pass to the decission function
    Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
    //the evaluator containing the decissionf function of the pmml model
    private ModelEvaluator<? extends Model> modelEvaluator;

    //name of the target variable
    private FieldName targetName;
    //fields used while training the model
    private List<FieldName> activeFields;

    @Parameter(required = true, description = "URL point to the .pmml model")
    SourceURL url;

    @Override
    public void init(ProcessContext processContext) throws Exception {
        //load model and marshal it into pmml version  > 4.0
        InputStream is = url.openStream();
        try{
            Source transformedSource = ImportFilter.apply(new InputSource(is));
            pmml = JAXBUtil.unmarshalPMML(transformedSource);
        } catch (SAXException ex){
            log.error("Could not load model from file provided at" + url);
        } finally {
            log.info("Closing input stream");
            is.close();
        }

        //build a modelevaluator from the loaded pmml file
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        modelEvaluator = modelEvaluatorFactory.newModelManager(pmml);


        log.info("Loaded model requires the following fields: " + modelEvaluator.getActiveFields().toString());

        log.info("Loaded model has targets: " + modelEvaluator.getTargetFields().toString());
        if(modelEvaluator.getTargetFields().size() > 1){
            log.error("Only models with one target variable are supported for now");
        }

        targetName = modelEvaluator.getTargetField();
        activeFields = modelEvaluator.getActiveFields();
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    @Override
    public Data process(Data data) {
        
        for(FieldName activeField : activeFields){
//            log.info("Loading Active field from data item:  " + activeField.toString());
            Object rawValue = data.get(activeField.toString());

            // The raw value is passed through: 1) outlier treatment, 2) missing value treatment, 3) invalid value treatment and 4) type conversion
            FieldValue activeValue = modelEvaluator.prepare(activeField, rawValue);

            arguments.put(activeField, activeValue);
        }

        Map<FieldName, ?> results = modelEvaluator.evaluate(arguments);

        Object targetValue = results.get(targetName);

        log.info("Prediction: " + targetValue);

        return data;
    }



    public void setUrl(SourceURL url) {
        this.url = url;
    }

}
