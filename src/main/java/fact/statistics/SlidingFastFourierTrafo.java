package fact.statistics;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class SlidingFastFourierTrafo implements Processor {
	static Logger log = LoggerFactory.getLogger(SlidingFastFourierTrafo.class);

	String key = null;
	String outputKey = null;
	
	int lengthForFFT = 128;
	
	int stepSize = 32;
	
	FastFourierTransformer fftObject = new FastFourierTransformer(DftNormalization.STANDARD);
	
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(this.getClass(), input,key);
		
		double[] data = (double[])input.get(key);
		int roi = data.length / Constants.NUMBEROFPIXEL;
		double[] result = new double[data.length]; 
		
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			for (int slBegin = 0 ; slBegin < roi ; slBegin+=stepSize)
			{
				double[] arrayForFFT = new double[lengthForFFT]; 
				int sl = 0;
				for (; sl < lengthForFFT && (sl+slBegin) < roi ; sl++)
				{
					arrayForFFT[sl] = data[px*roi+slBegin+sl];
				}
				for ( ; sl < lengthForFFT ; sl++)
				{
					arrayForFFT[sl] = 0;
				}
				Complex[] resultWindow = fftObject.transform(arrayForFFT, TransformType.INVERSE);
				for (sl = 1 ; sl < stepSize && (sl+slBegin) < roi ; sl++)
				{
					double real = resultWindow[sl].getReal();
					double imag = resultWindow[sl].getImaginary();
					result[px*roi+slBegin+sl] = Math.sqrt(real*real+imag*imag);
				}
			}
		}
		input.put(outputKey, result);
		
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

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}

}
