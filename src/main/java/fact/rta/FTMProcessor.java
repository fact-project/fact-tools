package fact.rta;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.Closest;
import org.joda.time.DateTime;
import stream.Data;
import stream.Processor;
import stream.annotations.Service;

import java.io.IOException;

/**
 * Created by mackaiver on 14/09/16.
 */
public class FTMProcessor implements Processor{

    @Service
    AuxiliaryService auxService;

    @Service
    RTAWebService webService;

    @Override
    public Data process(Data item) {
        try {
            Closest c = new Closest();
            DateTime dT = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(RuntimeException::new);
            AuxPoint auxPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, dT, c);
            webService.addFTMPoint(auxPoint);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
