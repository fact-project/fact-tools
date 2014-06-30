package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import fact.Constants;
import stream.Data;
import stream.Processor;

public class FastFourierTrafo implements Processor {
	static Logger log = LoggerFactory.getLogger(FastFourierTrafo.class);

	String key = null;
	String outputKey = null;
	
	int lengthForFFT = 512;
	
	int excludeFreqBinsMin = (int) (0.15*lengthForFFT*0.5);
	int excludeFreqBinsMax = (int) (0.25*lengthForFFT*0.5);
	
	
//	excludeFreqBins[0] = (int) (0.17*lengthForFFT*0.5);
	
	FastFourierTransformer fftObject = new FastFourierTransformer(DftNormalization.STANDARD);
	
	double[] freqAverage = new double[Constants.NUMBEROFPIXEL * 300];
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.mapContainsKeys(this.getClass(), input, key);
		
		log.info("exclution range: [" + excludeFreqBinsMin + "," + excludeFreqBinsMax + "]");
		
		double[] data = (double[])input.get(key);
		int roi = data.length / Constants.NUMBEROFPIXEL;
		double[] frResult = new double[data.length];
		double[] resultBackTrafo = new double[data.length];
		
		Complex[] frResultPixel = null;
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double[] currPixel = new double[lengthForFFT];
			int sl = 0 ;
			for ( ; sl < roi && sl < lengthForFFT ; sl++)
			{
				currPixel[sl] = data[px*roi+sl];
			}
			for ( ; sl < lengthForFFT ; sl++)
			{
				currPixel[sl] = 0;
			}
			frResultPixel = fftObject.transform(currPixel, TransformType.INVERSE);
			sl = 0;
			for ( ; sl < 10 ; sl++)
			{
				frResult[px*roi+sl] = 0;
			}
			for ( ; sl < (lengthForFFT/2 + 1) ; sl++)
			{
				double real = frResultPixel[sl].getReal();
				double ima = frResultPixel[sl].getImaginary();
				frResult[px*roi+sl] = Math.sqrt(real*real + ima*ima);
			}
			for ( ; sl < roi ; sl++)
			{
				frResult[px*roi+sl] = 0;
			}
			for (int fr=excludeFreqBinsMin ; fr < excludeFreqBinsMax && fr < lengthForFFT ; fr++)
			{
				frResultPixel[fr].multiply(0.0);
			}
			Complex[] backTrafo = fftObject.transform(frResultPixel, TransformType.FORWARD);
			sl = 0 ;
			for ( ; sl < roi && sl < lengthForFFT ; sl++)
			{
				double real = backTrafo[sl].getReal();
				double ima = backTrafo[sl].getImaginary();
				resultBackTrafo[px*roi+sl] = Math.sqrt(real*real + ima*ima);
			}
			for ( ; sl < lengthForFFT && sl < roi ; sl++)
			{
				resultBackTrafo[px*roi+sl] = 0;
			}
		}
		
		for (int i = 0 ; i < freqAverage.length ; i++)
		{
			freqAverage[i] += frResult[i];
		}
		
		input.put(outputKey, frResult);
		input.put(outputKey+"BackTrafo", resultBackTrafo);
		input.put(outputKey+"Average", freqAverage);
		
		return input;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public int getLengthForFFT() {
		return lengthForFFT;
	}

	public void setLengthForFFT(int lengthForFFT) {
		this.lengthForFFT = lengthForFFT;
	}

}
