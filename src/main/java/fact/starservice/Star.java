package fact.starservice;

import fact.coordinates.EquatorialCoordinate;

public class Star implements  Comparable<Star> {
    public final EquatorialCoordinate equatorialCoordinate;
    public final double magnitude;
    public final int id;

    public Star(EquatorialCoordinate equatorialCoordinate, double magnitude, int id) {
        this.equatorialCoordinate = equatorialCoordinate;
        this.magnitude = magnitude;
        this.id = id;
    }

    @Override
    public int compareTo(Star star) {
        return Double.compare(magnitude, star.magnitude);
    }
}
