package stream.clustering;

import stream.Data;

/**
 * Distanzberechnung zwischen zwei Data-Objekten.
 *
 * @author Hendrik Fichtenberger
 */
public interface Distance {
    public double distance(Data first, Data second);
}
