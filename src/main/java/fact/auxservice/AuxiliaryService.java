package fact.auxservice;

import fact.auxservice.strategies.AuxPointStrategy;
import org.joda.time.DateTime;
import stream.service.Service;

import java.io.IOException;

/**
 * The service should provide the ability to get the aux data from some data source.
 * Since sensor data can be written at different time intervals and frequencies we need to specify some strategy
 * for fetching the data. E.g. get the nearest point written before the given timestamp.
 *
 * Created by kai on 31.03.15.
 */
public interface AuxiliaryService extends Service {
    /**
     * An AuxiliaryService needs to implement just one method.
     * Providing the timestamp of the event, the name of the service
     * and some strategy, this method should return an AuxPoint.
     *
     * @param serviceName
     * @param eventTimeStamp
     * @param strategy
     * @return
     * @throws IOException
     */
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException;
}
