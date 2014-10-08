package fact.auxservice.drivepoints;

import stream.Data;

/**
 * The sourcePoint contains all information provided by the DRIVE_SOURCE_POSITION aux files.
 * Created by kaibrugge on 06.10.14.
 */
public class SourcePoint extends DrivePoint {
    public String name = "";

    public double raSrc;
    public double decSrc;
    public double raCmd;
    public double decCmd;
    public double wobbleOffset;
    public double wobbleAngle;


    public SourcePoint(Data item) throws IllegalArgumentException{
        super(item);
        try{

            raSrc = Double.parseDouble(item.get("Ra_src").toString());
            raSrc = raSrc / 24 * 360.0;
            decSrc = Double.parseDouble(item.get("Dec_src").toString());

            raCmd = Double.parseDouble(item.get("Ra_cmd").toString());
            raCmd = raCmd / 24 * 360.0;
            decCmd = Double.parseDouble(item.get("Dec_cmd").toString());

            wobbleOffset = Double.parseDouble(item.get("Offset").toString());
            wobbleAngle = Double.parseDouble(item.get("Angle").toString());

            name = item.get("Name").toString();
            System.out.println(this);

        } catch (NumberFormatException e){
            log.error("Could not parse doubles in file");
            throw new IllegalArgumentException("Could not parse doubles in file");
        } catch (NullPointerException e){
            log.error("Tracking information not in data item");
            throw new IllegalArgumentException("Tracking information not in data item");
        }

    }

    @Override
    public String toString(){
        return "SourcePoint: " + name + "  time: " + time + " raSrc, decSrc: " + raSrc + ", " + decSrc + " wobble angle: " + wobbleAngle;
    }


    public SourcePoint(double time, String name, double raSrc, double decSrc, double raCmd, double decCmd, double wobbleOffset, double wobbleAngle) throws IllegalArgumentException {
        super(time);
        this.name = name;
        this.raSrc = raSrc;
        this.decSrc = decSrc;
        this.raCmd = raCmd;
        this.decCmd = decCmd;
        this.wobbleOffset = wobbleOffset;
        this.wobbleAngle = wobbleAngle;
    }

}
