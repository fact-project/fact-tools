/**
 * 
 */
package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Subtract two Pixel Arrays of the same size from each other
 * 
 * @author  Michael Bulinski &lt;michael.bulinski@udo.edu&gt;
 *
 */
public class CombineDataArrays implements Processor {
	static Logger log = LoggerFactory.getLogger(RemappingKeys.class);
	
    @Parameter(required = true, description = "The key to your first array.")
    private String firstArrayKey;
    @Parameter(required = true, description = "The key to your second array.")
    private String secondArrayKey;
    @Parameter(required = false, description = "The key for the resulting array.")
    private String outputKey;
    @Parameter(required = true, description = "The operation to perform, (add, sub, mul, div)")
	private String op;
    
	private int npix = 1440*300;
    

	/* (non-Javadoc)
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, firstArrayKey, double[].class);
		Utils.isKeyValid(input, secondArrayKey, double[].class);

		double[] resultArray =  new double[npix];

		double[] array1 	 = (double[]) input.get(firstArrayKey);
		double[] array2 	 = (double[]) input.get(secondArrayKey);

		if (op.equals("add")) {
			for (int pix = 0; pix < npix; pix++) {
				resultArray[pix] = (double)(array1[pix] + array2[pix]);
			}
		} else if (op.equals("sub")) {
			for (int pix = 0; pix < npix; pix++) {
				resultArray[pix] = (double)(array1[pix] - array2[pix]);
			}
		} else if (op.equals("mul")) {
			for (int pix = 0; pix < npix; pix++) {
				resultArray[pix] = (double)(array1[pix] * array2[pix]);
			}
		} else if (op.equals("div")) {
			for (int pix = 0; pix < npix; pix++) {
				resultArray[pix] = (double)(array1[pix] / array2[pix]);
			}
		} else {
			throw new RuntimeException("The given operation op: "+op+" is not supported");
		}

		input.put(outputKey, resultArray);
		
		return input;
	}



	public static Logger getLog() {
		return log;
	}



	public static void setLog(Logger log) {
		CombineDataArrays.log = log;
	}


	public void setFirstArrayKey(String key) {
		this.firstArrayKey = key;
	}

	public void setSecondArrayKey(String key) {
		this.secondArrayKey = key;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setOp(String op) { this.op = op;}
	
	

}
