package fact.auxservice.drivepoints;

import stream.Data;

/**
 * The sourcePoint contains all information provided by a row in the DRIVE_SOURCE_POSITION aux files.
 * Created by kaibrugge on 06.10.14.
 */
public class SourcePoint extends DrivePoint {

    //the name of the source. E.g Crab, Mrk501 etc.
    public String name = "";

    public double raSrc;
    public double decSrc;
    public double raCmd;
    public double decCmd;
    public double wobbleOffset;
    public double wobbleAngle;


    @Override
    public String toString(){
        return "SourcePoint: " + name + "  time: " + getTime() + " raSrc, decSrc: " + raSrc + ", " + decSrc + " wobble angle: " + wobbleAngle + " wobble offset: " + wobbleOffset;
    }

    @Override
    public void initialiseWithDataItem(Data item) throws IllegalArgumentException {
        try{
            this.setTime(Double.parseDouble(item.get("Time").toString()) + 2440587.5);
            raSrc = Double.parseDouble(item.get("Ra_src").toString());
            raSrc = raSrc / 24 * 360.0;
            decSrc = Double.parseDouble(item.get("Dec_src").toString());

            raCmd = Double.parseDouble(item.get("Ra_cmd").toString());
            raCmd = raCmd / 24 * 360.0;
            decCmd = Double.parseDouble(item.get("Dec_cmd").toString());

            wobbleOffset = Double.parseDouble(item.get("Offset").toString());
            wobbleAngle = Double.parseDouble(item.get("Angle").toString());

            name = item.get("Name").toString();

        } catch (NumberFormatException e){
            log.error("Could not parse doubles in file");
            throw new IllegalArgumentException("Could not parse doubles in file");
        } catch (NullPointerException e){
            log.error("Tracking information not in data item");
            throw new IllegalArgumentException("Tracking information not in data item");
        }
    }
}
