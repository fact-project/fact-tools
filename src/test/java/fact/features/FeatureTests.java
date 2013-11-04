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
import fact.cleaning.CoreNeighborClean;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.statistics.PixelDistribution2D;
import fact.utils.CutSlices;

public class FeatureTests {
	
	private LinkedHashMap<Processor, Pair<String, Class<?>>> typeMap = new LinkedHashMap<Processor, Pair<String, Class<?>>>();
	
	private final String cleaningOutput = "showerPixel";

	private void setupProcessors() throws Exception{
		typeMap.clear();
		
		
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
		c.setPositions(rE.getOutputKey());
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
	
	private void setupHillasProcessors() throws Exception{
		typeMap.clear();
		
		URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
		DrsCalibration pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey("DataCalibrated");
		Pair<String, Class<?>> p = new Pair<String, Class<?>>(pr.getOutputKey(), double[].class );
		typeMap.put(pr, p);
		
		CutSlices cut = new CutSlices();
		String[] keys = {pr.getOutputKey()};
		cut.setKeys(keys);
		cut.setEnd(220);
		cut.setStart(22);
		cut.setOutputKey(pr.getOutputKey());
		p = new Pair<String, Class<?>>(cut.getOutputKey(), double[].class );
		typeMap.put(cut, p);
		
		RisingEdge rE = new RisingEdge();
		rE.setKey(cut.getOutputKey());
		rE.setOutputKey("risingEdge");
		p = new Pair<String, Class<?>>(rE.getOutputKey(), int[].class );
		typeMap.put(rE, p);
		
		PhotonCharge charge = new PhotonCharge();
		charge.setKey(cut.getOutputKey());
		charge.setOutputKey("photonCharge");
		charge.setPositions(rE.getOutputKey());
		p = new Pair<String, Class<?>>(charge.getOutputKey(), double[].class );
		typeMap.put(charge, p);
		
		URL driveURL = FitsStreamTest.class.getResource("/drive_file.fits");
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		poser.setPhysicalSource("crab");
		poser.setOutputKey("pos");
		poser.init(null);
		p = new Pair<String, Class<?>>(charge.getOutputKey(), double[].class );
		typeMap.put(poser, p);
		
		
		//to test the hillas stuff we need some cleaned pixel. set the output to the final String we set globaly
		CoreNeighborClean core = new CoreNeighborClean();
		core.setKey(charge.getOutputKey());
		core.setKeyPositions(rE.getOutputKey());
		core.setOutputKey(cleaningOutput);
		core.init(null);
		p = new Pair<String, Class<?>>(core.getOutputKey(), int[].class );
		typeMap.put(core, p);
		
		//and build a 2D distribution
		DistributionFromShower dist = new DistributionFromShower();
		dist.setPixel(core.getOutputKey());
		dist.setWeights(charge.getOutputKey());
		dist.setOutputKey("distribution");
		dist.init(null);
		p = new Pair<String, Class<?>>(dist.getOutputKey(), PixelDistribution2D.class );
		typeMap.put(dist, p);
		
		
		//test the hillas stuff
		HillasAlpha alpha = new HillasAlpha();
		alpha.setDistribution(dist.getOutputKey());
		alpha.setSourcePosition(poser.getOutputKey());
		alpha.setOutputKey("alpha");
		p = new Pair<String, Class<?>>(alpha.getOutputKey(), Double.class );
		typeMap.put(alpha, p);
		
		HillasDistance distance = new HillasDistance();
		distance.setDistribution(dist.getOutputKey());
		distance.setSourcePosition(poser.getOutputKey());
		distance.setOutputKey("distance");
		p = new Pair<String, Class<?>>(distance.getOutputKey(), Double.class );
		typeMap.put(distance, p);
		
		HillasLength length = new HillasLength();
		length.setDistribution(dist.getOutputKey());
		length.setOutputKey("length");
		p = new Pair<String, Class<?>>(length.getOutputKey(), Double.class );
		typeMap.put(length, p);
		
		HillasWidth width = new HillasWidth();
		width.setDistribution(dist.getOutputKey());
		width.setOutputKey("length");
		p = new Pair<String, Class<?>>(width.getOutputKey(), Double.class );
		typeMap.put(width, p);
		
		
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
					assertTrue("Processor " + entry.getKey().getClass().getSimpleName() + " did not write data to the item", item.containsKey(outputKey));
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
	
	@Test
	public void testHillasFeatures(){
		try {
			setupHillasProcessors();
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);

			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();

			//not all events contain shower pixel. We still want to guarantee that hillas parameter will be written iff showerpixel exist 
			//also check that at least one item contained a shower. This should be the case even for our small test file.
			int showerCount = 0;
			while (item != null) {
				Data checkedItem = item.createCopy();
				//check if we have showerPixel. Just loop over the complete process and check for the output of the cleaning method.
				for (Entry<Processor, Pair<String, Class<?>>> entry : typeMap.entrySet()) {
					entry.getKey().process(item);
				}
				//did cleaning produce any output? if so just do the loop over all the processors defined in hte setupHillasProcessor method
				//but use the copieditem.
				if(item.containsKey(cleaningOutput)){
					showerCount++;
					//set  this to null. just for clarification. we  dont need it anymore.
					item  = null;
					for (Entry<Processor, Pair<String, Class<?>>> entry : typeMap.entrySet()) {
						Processor processor = entry.getKey();
						String outputKey = entry.getValue().getFirst();
						Class<?> outputClass = entry.getValue().getSecond();
						processor.process(checkedItem);
						assertTrue("Processor " + entry.getKey().getClass().getSimpleName() + " did not write data to the item", checkedItem.containsKey(outputKey));
						
						try{
//							System.out.println("value: " + checkedItem.get(outputKey).toString());
							outputClass.cast(checkedItem.get(outputKey));
						} catch (ClassCastException e) {
							System.out.println("Trying to cast " + checkedItem.get(outputKey) + " to "  + outputClass.getSimpleName() + "   FAILED.");
							int[] shower = (int[]) checkedItem.get(cleaningOutput);
							System.out.println("item contains a shower with  : " + shower.length  + "  pixel");
							//fail("Could not cast the objects to right type. The Processor " + entry.getKey().getClass().getSimpleName() + " should write a " + outputClass.getSimpleName());
						}
					}
				}
//				ShowImage show = new ShowImage();
//				show.process(checkedItem);
				//get the next item from the fits file and test again.
				item = stream.read();
			}
			if(showerCount < 1){
				fail("No whoer at all found in the test data. this cannot happen.");
			}
			System.out.println("Number of showers fund in testdata: " + showerCount );
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read stream");
		}
	}

}
