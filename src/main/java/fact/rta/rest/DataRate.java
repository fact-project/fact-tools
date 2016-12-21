package fact.rta.rest;

import java.time.OffsetDateTime;

/**
 * Created by mackaiver on 20/12/16.
 */
public class DataRate {

    final double rate;
    final OffsetDateTime date;

    public DataRate(OffsetDateTime date, double rate) {
        this.rate = rate;
        this.date = date;
    }
}
