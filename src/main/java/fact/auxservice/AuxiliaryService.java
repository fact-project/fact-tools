package fact.auxservice;

import fact.auxservice.strategies.AuxPointStrategy;
import org.joda.time.DateTime;
import stream.service.Service;

import java.io.IOException;

/**
 * Created by kai on 31.03.15.
 */
public interface AuxiliaryService extends Service {
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException;
}
