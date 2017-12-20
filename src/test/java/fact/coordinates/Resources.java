package fact.coordinates;

/**
 * Created by maxnoe on 29.06.17.
 */

public class Resources {
    public class Source {
        public String name;
        public String obstime;
        public double ra;
        public double dec;
        public double zd;
        public double az;
    }

    public class GMSTData {
        public String obstime;
        public double gmst_rad;
    }
}