package stream.clustering;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

/**
 * Euklidische Distanz auf Data-Elementen. Wertet alle Attribute aus, deren
 * Schluessel nicht mit "@" beginnt und deren Wert vom Typ Double ist. Dabei
 * werden Attribute, deren Schluessel nur in einem Data-Element vorkommen, im
 * anderen implizit mit 0 besetzt.
 * 
 * @author Hendrik Fichtenberger & Lukas Pfahler
 */
public class EuclideanDistance implements Distance {

	static Logger log = LoggerFactory.getLogger(EuclideanDistance.class);
	Long total = 0L;
	Long count = 0L;

	@Override
	public double distance(Data first, Data second) {

		if (first.containsKey("@vector") && second.containsKey("@vector")) {
			return dist((double[]) first.get("@vector"),
					(double[]) second.get("@vector"));
		}

		Long start = System.nanoTime();
		double dist = 0;
		HashSet<String> union = new HashSet<String>(first.keySet().size()
				+ second.keySet().size());
		union.addAll(first.keySet());
		union.addAll(second.keySet());
		for (String key : union) {
			double x = 0.0;
			double y = 0.0;
			if (!key.startsWith("@") && first.get(key) instanceof Number) {
				x = ((Number) first.get(key)).doubleValue();
			}
			if (!key.startsWith("@") && second.get(key) instanceof Number) {
				y = ((Number) second.get(key)).doubleValue();
			}
			double a = x - y;
			dist += a * a;
		}
		dist = Math.sqrt(dist);
		Long end = System.nanoTime();
		total += (end - start);
		count++;

		if (count % 5000 == 0) {
			log.info("Average time for distance computations is {} ns", total
					/ count);
		}
		return dist;
	}

	public double dist(double[] row1, double[] row2) {
		double sum = 0.0d;
		for (int i = 0; i < row1.length; i++) {
			double diff = (row2[i] - row1[i]);
			sum += (diff * diff);
		}
		return Math.sqrt(sum);
	}
}
