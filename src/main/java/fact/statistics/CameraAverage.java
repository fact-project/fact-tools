package fact.statistics;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

public class CameraAverage implements Processor {
	
	static Logger log = LoggerFactory.getLogger(CameraAverage.class);
	
	String key=null;
	String outputKey=null;
	
	
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.mapContainsKeys( input, key);
		
		double[] data=(double[]) input.get(key);
		int currentRoi = data.length / Constants.NUMBEROFPIXEL;
		
		double[] result = new double[currentRoi];
		for (int sl = 0 ; sl < currentRoi ; sl++)
		{
			for (int px=0 ; px < Constants.NUMBEROFPIXEL ; px++)
			{
				result[sl] += data[px*currentRoi+sl];
			}
			result[sl] /= Constants.NUMBEROFPIXEL;
		}
		
		input.put(outputKey, result);
		return input;
	}

}
