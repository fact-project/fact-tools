package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.filter.DrsCalibration;
import fact.filter.ExponentialSmoothing;
import fact.filter.FirFilter;
import fact.filter.InterpolateBadPixel;
import fact.filter.MotionDiff;
import fact.filter.MultiplyValues;
import fact.io.FitsStream;
import fact.statistics.MovingAverage;
import fact.utils.ExFit;
import fact.utils.SimpleFactEventProcessor;


public class FilterTests {

	static Logger log = LoggerFactory.getLogger(FilterTests.class);


	@Test
	public void testFilterLikeProcessors() {

		try {
			
			URL drsUrl =  FilterTests.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test0");
			
			ArrayList<SimpleFactEventProcessor<float[], float[]>> pList = new ArrayList<SimpleFactEventProcessor<float[], float[]>>();
			pList.add(new FirFilter());
			pList.add(new MovingAverage());
			pList.add(new ExponentialSmoothing());
			pList.add(new InterpolateBadPixel());
			pList.add(new MultiplyValues());
			pList.add(new ExFit());
			pList.add(new MotionDiff());
			
			int i = 0;
			for(SimpleFactEventProcessor<float[], float[]> filter : pList){
				filter.setKey("test" + i);
				filter.setOutputKey("test"+(i+1));
			}
			
			URL dataUrl =  FilterTests.class.getResource("/sample.fits.gz");
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
						if(!item.containsKey(filter.getOutputKey())){
							fail("item does not conatin the right outputkey after applying " + filter.getClass().getSimpleName());
						}
						
						@SuppressWarnings("unused")
						float[] result = (float[]) item.get(filter.getOutputKey());
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

