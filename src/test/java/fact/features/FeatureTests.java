package fact.features;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import stream.Data;
import stream.Processor;
import stream.io.SourceURL;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

public class FeatureTests {
	
	private LinkedHashMap<Processor, Pair<String, Class<?>>> typeMap = new LinkedHashMap<Processor, Pair<String, Class<?>>>();

	private void setupProcessors() throws Exception{
		URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
		DrsCalibration pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey("test");
		Pair<String, Class<?>> p = new Pair<String, Class<?>>(pr.getOutputKey(), double[].class );
		typeMap.put(pr, p);

		MaxAmplitudePosition pP = new MaxAmplitudePosition();
		pP.setKey("test");
		pP.setOutputKey("positions");
		p = new Pair<String, Class<?>>(pP.getOutputKey(), int[].class );
		typeMap.put(pP, p);
		
		MaxAmplitude maxAmp = new MaxAmplitude();
		maxAmp.setKey("test");
		maxAmp.setOutputKey("maxAmps");
		p = new Pair<String, Class<?>>(maxAmp.getOutputKey(), double[].class );
		typeMap.put(maxAmp, p);
		
		RisingEdge rE = new RisingEdge();
		rE.setKey("test");
		rE.setOutputKey("risingEdge");
		p = new Pair<String, Class<?>>(rE.getOutputKey(), int[].class );
		typeMap.put(rE, p);
		
		PhotonCharge c = new PhotonCharge();
		c.setKey("test");
		c.setOutputKey("photonCharge");
		p = new Pair<String, Class<?>>(c.getOutputKey(), double[].class );
		typeMap.put(c, p);
		
		URL driveURL = FitsStreamTest.class.getResource("/drive_file.fits");
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		poser.setPhysicalSource("crab");
		poser.setOutputKey("pos");
		poser.init(null);
		p = new Pair<String, Class<?>>(c.getOutputKey(), double[].class );
		typeMap.put(poser, p);
		
		
	}
	@Test
	public void dataTypes() {

		try {
			setupProcessors();
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);

			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				for (Entry<Processor, Pair<String, Class<?>>> entry : typeMap.entrySet()) { 
//					System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//					entry.getKey().process(item);
					String outputKey = entry.getValue().getFirst();
					Class<?> outputClass = entry.getValue().getSecond();
					entry.getKey().process(item);
					assertTrue("Processor" + entry.getKey().getClass().getSimpleName() + " did not write data to the item", item.containsKey(outputKey));
					try{
						outputClass.cast(item.get(outputKey));
					} catch (ClassCastException e) {
						fail("Could not cast the objects to right type. The Processor " + entry.getKey().getClass().getSimpleName() + " should write a " + outputClass.getSimpleName());
					}
				}
				item = stream.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read stream");
		}

	}

}
