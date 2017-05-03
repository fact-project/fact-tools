package fact.rta;

import fact.auxservice.AuxPoint;
import stream.Data;
import stream.service.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Created by mackaiver on 02/05/17.
 */
public abstract class RTAWebService implements Service{


    public abstract void updateDataRate(OffsetDateTime timeStamp, Double dataRate);
    public abstract void updateEvent(OffsetDateTime eventTimeStamp, Data item) throws IOException;
}
