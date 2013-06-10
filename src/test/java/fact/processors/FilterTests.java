package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;


public class FilterTests {

	static Logger log = LoggerFactory.getLogger(FilterTests.class);


	@Test
	public void drsCalibProcessor() {

		try {
			
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test0");
			
			ArrayList<SimpleFactEventProcessor<float[], float[]>> pList = new ArrayList<>();
			pList.add(new FirFilter());
			pList.add(new MovingAverage());
			pList.add(new ExponentialSmoothing());
			pList.add(new InterpolateBadPixel());
			pList.add(new MultiplyValues());
			pList.add(new ExFit());
			
			int i = 0;
			for(SimpleFactEventProcessor<float[], float[]> filter : pList){
				filter.setKey("test" + i);
				filter.setOutputKey("test"+(i+1));
			}
			
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				pr.process(item);
				if (!item.containsKey("test0"))
					fail("Item does not contain the right key after drs calibration");
				try{
					
					for(SimpleFactEventProcessor<float[], float[]> filter : pList){
						filter.process(item);
						if(!item.containsKey(filter.outputKey)){
							fail("item does not conatin the right outputkey after applying " + filter.getClass().getSimpleName());
						}
						float[] result = (float[]) item.get(filter.outputKey);
					}
					
				} catch(ClassCastException e){
					fail("Failed to cast items to float[]");
				}
				item = stream.read();
			}
			
		} catch(ClassCastException e){
			fail("Wrong datatypes.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not execute drsCalibration");
		}
	}
}

