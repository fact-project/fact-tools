package fact.starservice;

import fact.coordinates.EquatorialCoordinate;

/**
 * A simple data class holding information about a star, coordinate, magnitude, catalogue id and name
 */
public class Star implements  Comparable<Star> {
    public final EquatorialCoordinate equatorialCoordinate;
    public final double magnitude;
    public final int id;
    public final String name;

    public Star(EquatorialCoordinate equatorialCoordinate, double magnitude, int id, String name) {
        this.equatorialCoordinate = equatorialCoordinate;
        this.magnitude = magnitude;
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Star star) {
        return Double.compare(magnitude, star.magnitude);
    }
}
