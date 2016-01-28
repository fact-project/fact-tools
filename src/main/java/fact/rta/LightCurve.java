package fact.rta;

import stream.Data;
import stream.Processor;

/**
 * Created by kai on 24.01.16.
 */
public class LightCurve implements Processor {
    //TODO: use service annotation to inject rtawebservice

    RTAWebService rtaWebService = new RTAWebService();

    @Override
    public Data process(Data data) {

        if (data.containsKey("@datarate")){
            rtaWebService.updateDatarate((double) data.get("@datarate"));
        }
        return data;
    }
}
