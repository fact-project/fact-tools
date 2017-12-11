package fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;


public class RequireVersion implements StatefulProcessor {

    @Parameter(required = true)
    String version;

    private final Logger log = LoggerFactory.getLogger(RequireVersion.class);


    @Override
    public void init(ProcessContext processContext) throws Exception {
        String actual = VersionInformation.getInstance().gitDescribe;
        if (actual != version) {
            log.error("FACT-Tools version ({}) did not match required version ({})", actual, version);
            System.exit(1);
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

    @Override
    public Data process(Data data) {
        return data;
    }
}
