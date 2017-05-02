package fact.auxservice;

import fact.auxservice.strategies.AuxPointStrategy;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import stream.Data;
import stream.service.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

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
     * @param serviceName the name of the aux data to access. This is written in the filename 20130112.<serviceName>.fits
     * @param eventTimeStamp the DateTime of the event you need the aux data for.
     * @param strategy one of the strategies implemented for fetcvhing aux points
     * @return the auxpoint according to the strategy
     * @throws IOException in case something goes wrong while trying to access the aux data. Be it a file or a database.
     */
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException;

    /**
     * Takes the int[2] array found in the FITs files under the name UnixTimeUTC and converts it to a DateTime
     * instance with time zone UTC. If the passed array cannot be converted the optional will be empty.
     *
     * @param eventTime the UnixTimeUTC array as found in the FITS file.
     * @return an Optional containing the Datetime instance
     */
    public  static Optional<DateTime> unixTimeUTCToDateTime(int [] eventTime){
        if(eventTime != null && eventTime.length == 2) {
            DateTime timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
            return Optional.of(timeStamp);
        }
        return Optional.empty();
    }

    /**
     * Takes the int[2] array found in the FITs files under the name UnixTimeUTC from the Data Item and converts it to a DateTime
     * instance with time zone UTC. If the passed array cannot be converted the optional will be empty.
     *
     * @param item A data item from the stream of raw FACT data
     * @return an Optional containing the Datetime instance
     */
    public  static Optional<DateTime> unixTimeUTCToDateTime(Data item){
        int[] eventTime = (int[]) item.get("UnixTimeUTC");
        return unixTimeUTCToDateTime(eventTime);
    }


    /**
     * Takes the int[2] array found in the FITs files under the name UnixTimeUTC and converts it to a DateTime
     * instance with time zone UTC. If the passed array cannot be converted the optional will be empty.
     *
     * @param eventTime the UnixTimeUTC array as found in the FITS file.
     * @return an Optional containing the Datetime instance
     */
    public  static Optional<OffsetDateTime> unixTimeUTCToOffsetDateTime(int [] eventTime){
        if(eventTime != null && eventTime.length == 2) {
            OffsetDateTime offsetDateTime = Instant.ofEpochMilli((long) ((eventTime[0] + eventTime[1] / 1000000.) * 1000)).atOffset(ZoneOffset.UTC);
            return Optional.of(offsetDateTime);
        }
        return Optional.empty();
    }


    /**
     * Takes the int[2] array found in the FITs files under the name UnixTimeUTC from the Data Item and converts it to a DateTime
     * instance with time zone UTC. If the passed array cannot be converted the optional will be empty.
     *
     * @param item A data item from the stream of raw FACT data
     * @return an Optional containing the Datetime instance
     */
    public  static Optional<OffsetDateTime> unixTimeUTCToOffsetDateTime(Data item){
        int[] eventTime = (int[]) item.get("UnixTimeUTC");
        return unixTimeUTCToOffsetDateTime(eventTime);
    }

}
