package fact.coordinates;

/**
 * Created by maxnoe on 23.05.17.
 */
public interface CelestialCoordinate {

    /**
     * Great circle distance using the more complicated but numerically more
     * stable version of wikipedia (special case of the Vincenty formula)
     * https://en.wikipedia.org/wiki/Great-circle_distance
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Great circle distance between the two points
     */
    static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {

        double deltaLon = lon1 - lon2;

        double t1 = Math.cos(lat2) * Math.sin(deltaLon);
        double t2 = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double numerator = Math.sqrt(Math.pow(t1, 2.0) + Math.pow(t2, 2.0));

        double t3 = Math.sin(lat1) * Math.sin(lat2);
        double t4 = Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        double denominator = t3 + t4;

        return Math.atan2(numerator, denominator);
    }
}
