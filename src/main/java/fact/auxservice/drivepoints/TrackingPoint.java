package fact.auxservice.drivepoints;

import stream.Data;

/**
 * Created by kaibrugge on 06.10.14.
 */
public class TrackingPoint extends DrivePoint {

    //Degrees
    public double dec; //Command Declination
    public double ra; //Command right ascension
    public double Az; // Nominal Azimuth angle
    public double Zd; // Nominal zenith distance
    public double deviationAz; // Control deviation Az
    public double deviationZd; // Control deviation Zd


    @Override
    public void initialiseWithDataItem(Data item) throws IllegalArgumentException {
        try {
            this.setTime(Double.parseDouble(item.get("Time").toString()) + 2440587.5);
            ra = Double.parseDouble(item.get("Ra").toString());
            ra = ra / 24 * 360.0;
            dec = Double.parseDouble(item.get("Dec").toString());

            Az = Double.parseDouble(item.get("Az").toString());
            Zd = Double.parseDouble(item.get("Zd").toString());
            deviationAz = Double.parseDouble(item.get("dAz").toString());
            deviationZd = Double.parseDouble(item.get("dZd").toString());
        } catch (NumberFormatException e){
            log.error("Could not parse doubles in file");
            throw new IllegalArgumentException("Could not parse doubles in file");
        } catch (NullPointerException e){
            log.error("Tracking information not in data item");
            throw new IllegalArgumentException("Tracking information not in data item");
        }
    }
}
