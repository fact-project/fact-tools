///**
// * 
// */
//package stream.clustering;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Random;
//
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import stream.clustering.KMeans.Cluster;
//import stream.data.Data;
//import stream.distribution.NominalDistribution;
//import stream.io.CsvStream;
//import stream.parser.ParseDouble;
//
///**
// * @author chris
// * 
// */
//public class KMeansTest {
//
//	static Logger log = LoggerFactory.getLogger(KMeansTest.class);
//
//	@Test
//	public void test() throws Exception {
//
//		List<Data> items = new ArrayList<Data>();
//
//		CsvStream stream = new CsvStream(
//				KMeansTest.class.getResource("/iris.csv"));
//
//		ParseDouble pd = new ParseDouble();
//		pd.setKeys(new String[] { "a1", "a2", "a3", "a4" });
//		stream.addPreprocessor(pd);
//
//		Data item = stream.readNext();
//		while (item != null) {
//			Serializable value = item.remove("label");
//			item.put("@label", value);
//			items.add(item);
//			item = stream.readNext();
//		}
//
//		ZNormalization norm = new ZNormalization();
//		items = norm.normalize(items);
//
//		KMeans kmeans = new KMeans();
//		Collection<Cluster> clustering = kmeans.cluster(items, 3);
//
//		for (Cluster cluster : clustering) {
//			log.info("Cluster[{}]: {}", cluster.getId(), cluster);
//
//			NominalDistribution<String> dist = new NominalDistribution<String>();
//			for (Data i : cluster) {
//				dist.update(i.get("@label") + "");
//			}
//
//			for (String key : dist.getElements()) {
//				log.info(" {} = {}", key, dist.getCount(key));
//			}
//		}
//	}
//
//	@Test
//	public void testDistance() {
//		double d[][] = new double[1440][300];
//
//		for (int i = 0; i < d.length; i++) {
//			for (int j = 0; j < d[i].length; j++) {
//				d[i][j] = Math.random();
//			}
//		}
//
//		KMeans kmeans = new KMeans();
//
//		Random rnd = new Random();
//		Long total = 0L;
//		Long count = 10000L;
//		for (int k = 0; k < count; k++) {
//			int x = rnd.nextInt(d.length);
//			int y = rnd.nextInt(d.length);
//
//			long start = System.nanoTime();
//			kmeans.dist(d[x], d[y]);
//			long end = System.nanoTime();
//
//			total += (end - start);
//		}
//
//		log.info("Average time for distance computations is {} ns", total
//				/ count);
//
//	}
//}