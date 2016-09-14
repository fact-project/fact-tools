package fact.rta;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.Closest;
import fact.rta.db.Run;
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
    AuxiliaryService auxiliaryService;

    @Service
    RTAWebService rtaWebService;

    @Override
    public Data process(Data item) {
        try {
            Closest c = new Closest();
            DateTime dT = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(RuntimeException::new);
            AuxPoint auxPoint = auxiliaryService.getAuxiliaryData(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, dT, c);


            float onTime =auxPoint.getFloat("OnTime");
            float elapsedTime = auxPoint.getFloat("ElapsedTime");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
