/**
 * 
 */
package stream.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;

/**
 * @author chris
 * 
 */
public class KMeans {

	static Logger log = LoggerFactory.getLogger(KMeans.class);
	static Integer clusterId = 0;

	int rounds = 10;
	int optimizations = 10;

	Random rnd = new Random(System.currentTimeMillis());
	Distance distance = new EuclideanDistance();

	public KMeans() {
		this(new EuclideanDistance());
	}

	public KMeans(Distance distance) {
		this.distance = distance;
	}

	/**
	 * @return the rounds
	 */
	public int getRounds() {
		return rounds;
	}

	/**
	 * @param rounds
	 *            the rounds to set
	 */
	public void setRounds(int rounds) {
		this.rounds = rounds;
	}

	/**
	 * @return the optimizations
	 */
	public int getOptimizations() {
		return optimizations;
	}

	/**
	 * @param optimizations
	 *            the optimizations to set
	 */
	public void setOptimizations(int optimizations) {
		this.optimizations = optimizations;
	}

	/**
	 * @return the distance
	 */
	public Distance getDistance() {
		return distance;
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setDistance(Distance distance) {
		this.distance = distance;
	}

	public Collection<Cluster> cluster(List<Data> input, int k) {

		Collection<Cluster> bestClustering = null;
		Double withinDistance = Double.MAX_VALUE;

		List<String> keys = new ArrayList<String>();
		for (Data dat : input) {
			for (String key : dat.keySet()) {
				if (!keys.contains(key))
					keys.add(key);
			}
		}

		double d[][] = new double[input.size()][keys.size()];
		int row = 0;
		for (Data dat : input) {
			for (int i = 0; i < keys.size(); i++) {
				Serializable val = dat.get(keys.get(i));
				if (val != null && val instanceof Number)
					d[row][i] = ((Number) val).doubleValue();
				else
					d[row][i] = 0.0d;
			}

			dat.put("@vector", d[row]);

			row++;
		}

		for (int round = 0; round < rounds; round++) {

			clusterId = 0;
			Double within = 0.0;
			List<Data> items = new ArrayList<Data>(input);
			List<Data> centroids = selectInitialCentroids(items, k);
			log.info("------------------------------------");
			log.info("Optimization Round {}", round);
			log.info("Initial centroids: {}", centroids);
			List<Cluster> cluster = new ArrayList<Cluster>();
			for (Data centroid : centroids) {
				cluster.add(new Cluster(centroid));
				// items.remove(centroid);
			}

			assign(items, cluster);

			// Assign points to clusters
			//

			for (int i = 0; i < optimizations; i++) {

				for (Cluster c : cluster) {
					Data centroid = getAverage(c);
					log.trace("Average of {}: {}", c.getId(), centroid);
					c.setCentroid(centroid);
				}
				assign(items, cluster);
			}
			for (Cluster c : cluster) {
				within += this.computeWithinDistance(c, distance);
			}

			log.debug("within distance: {}", within);

			if (bestClustering == null || withinDistance > within) {
				log.info("Found improved clustering: {} (old was {})", within,
						withinDistance);
				withinDistance = within;
				bestClustering = cluster;
			}
		}

		return bestClustering;
	}

	protected void assign(Collection<Data> items, Collection<Cluster> clustering) {
		log.info("Assigning {} items to {} clusters", items.size(),
				clustering.size());

		long start = System.currentTimeMillis();
		Iterator<Cluster> c = clustering.iterator();
		while (c.hasNext()) {
			c.next().clear();
		}

		for (Data item : items) {
			Cluster cl = getNearest(item, clustering);
			if (cl != null)
				cl.add(item);
		}
		long end = System.currentTimeMillis();
		log.info("Cluster assignment required {} ms", (end - start));
		start = System.currentTimeMillis();

		for (Cluster cl : clustering) {
			log.info("Within cluster distance of {}: {}", cl.getId(),
					computeWithinDistance(cl, distance));
		}
		log.info("within cluster distance computation required {} ms",
				(System.currentTimeMillis() - start));
	}

	public Cluster getNearest(Data item, Collection<Cluster> points) {

		if (points.isEmpty())
			return null;

		Iterator<Cluster> it = points.iterator();
		Cluster nearest = null;
		Double dist = Double.MAX_VALUE;

		while (it.hasNext()) {
			Cluster cluster = it.next();
			Double d = distance.distance(item, cluster.getCentroid());
			if (nearest == null || d < dist) {
				nearest = cluster;
				dist = d;
			}
		}

		return nearest;
	}

	public List<Data> selectInitialCentroids(List<Data> items, int k) {
		List<Data> centroids = new ArrayList<Data>();
		if (items.size() < k) {
			return centroids;
		}

		while (centroids.size() < k) {

			int idx = rnd.nextInt(items.size());
			Data item = items.get(idx);

			if (!centroids.contains(item)) {
				log.trace("Selecting {}", idx);
				centroids.add(item);
			}
		}

		return centroids;
	}

	protected Data getAverage(Collection<Data> items) {

		Data sum = DataFactory.create();
		Double count = new Double(items.size());
		for (Data item : items) {
			for (String key : item.keySet()) {
				Serializable value = item.get(key);
				if (Number.class.isAssignableFrom(value.getClass())) {
					Double d = ((Number) value).doubleValue();
					Double s = (Double) sum.get(key);
					if (s == null)
						s = d;
					else
						s = s + d;
					sum.put(key, s);
				}
			}
		}

		for (String key : sum.keySet()) {
			sum.put(key, ((Double) sum.get(key)) / count);
		}

		return sum;
	}

	protected Double computeWithinDistance(Cluster c, Distance dist) {
		Double d = 0.0d;

		for (Data item : c) {
			for (Data other : c) {
				d += dist.distance(item, other);
			}
		}

		return d;
	}

	public class Clustering implements Comparable<Clustering> {
		Set<Cluster> clusters;
		Double withinDistance;

		public Clustering(Set<Cluster> clusters, Double withinDist) {
			this.clusters = clusters;
			this.withinDistance = withinDist;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Clustering arg0) {
			return withinDistance.compareTo(arg0.withinDistance);
		}
	}

	public class Cluster extends ArrayList<Data> {
		/** The unique class ID */
		private static final long serialVersionUID = 1481842764312524304L;
		Integer id;
		Data centroid;

		public Cluster(Data centroid) {
			this.centroid = centroid;
			this.id = clusterId++;
		}

		/**
		 * @return the centroid
		 */
		public Data getCentroid() {
			return centroid;
		}

		/**
		 * @param centroid
		 *            the centroid to set
		 */
		public void setCentroid(Data centroid) {
			this.centroid = centroid;
		}

		public Integer getId() {
			return id;
		}
	}

	public Collection<Cluster> kmeans(double[][] examples, int k) {

		for (int round = 0; round < rounds; round++) {

		}

		return null;
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