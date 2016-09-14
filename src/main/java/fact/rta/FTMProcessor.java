package fact.rta;

import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import org.joda.time.DateTime;
import stream.Data;
import stream.Processor;
import stream.annotations.Service;

/**
 * Created by mackaiver on 14/09/16.
 */
public class FTMProcessor implements Processor{

    @Service
    AuxiliaryService auxiliaryService;

    @Service
    RTAWebService rtaWebService;

    @Override
    public Data process(Data item) {
        DateTime d = (int[]) item.get()
        auxiliaryService.getAuxiliaryData(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, );
        return null;
    }
}
