package fact.parfactProcessors;

import java.util.ArrayList;
import stream.Processor;
import stream.Data;
import fact.Constants;
import fact.image.Pixel;
import fact.image.overlays.PixelSet;

public class SimpleClean implements Processor{
	/**
	 * parameters
	 */

	 /**
     * paremeter and getter setters
     */
    String[] keys = new String[] { "Data" };

	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	
	private int mNumberOfSigma = 2;

	public int getmNumberOfSigma() {
		return mNumberOfSigma;
	}

	public void setmNumberOfSigma(int mNumberOfSigma) {
		this.mNumberOfSigma = mNumberOfSigma;
	}

	@Override
	public Data process(Data input) {

		
		for(String key: keys)
		{	
		
			/**
			 * calculate photoncharges. they'll be needed a few lines down.
			 */
	
			float[] photonCharges = (new CalculatePhotonCharge().processEvent(input, key));
			
			ArrayList<Integer> showerPixel  = new ArrayList<Integer>();
			float[] mpBaseline = new float         [Constants.NUMBEROFPIXEL];
			float[] mpRms = new float         [Constants.NUMBEROFPIXEL];
			    
	
			    /// Initializing of mpBaseline and mpRms
		    for (int px=0 ; px < Constants.NUMBEROFPIXEL ; px++)
		    {
		        /** @todo remove hardcoded baseline and rms with values, calculated
		        * by using the interleaved pedestal events
		        */
		        mpBaseline[px]      = 0;
		        mpRms[px]           = 1;
		    }
	
			    /// Loop over all Pixels and identify the Shower Pixel
		    for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++)
		    {
	//		        if (mVerbosityLevel > 7 && 0 == px%100)
	//		        {
	//		            cout << "Cleaning Pixel: \t" << px << "/" << mpRun->mNumberOfPixels << endl;
	//		        }
		        if (photonCharges[pix] > ( mpBaseline[pix] +  mNumberOfSigma * mpRms[pix])  )
		        {
		        	//is this needed??
	//	            mpEvent->mpPicture[px].mpStatus->CurrentStatus(showerassociated);
		            showerPixel.add(pix);
		        }
		        else
		        {
		        	//again? does a pixel need to know that?
		            //mpEvent->mpPicture[px].mpStatus->CurrentStatus(cleaned);
		        }
		    }
	//		    if (mVerbosityLevel > 6)
	//		    {
	//		        cout << "Simple Clean ended: Event Nr. " << mpEvent->mEventId << endl << endl;
	//		    }
		    
	//		    delete[] mpBaseline;
	//		    delete[] mpRms;
	
			   // mpEvent->mNumberOfShowerPixel           = mpEvent->mpShowerPixel->size();
		    
	    	/**
	    	 * save an int[] array of softIds to the event
	    	 */
		    PixelSet corePixel = new PixelSet();
		    int[] showerPixelArray =  new int[showerPixel.size()];
		    for(int i = 0; i < showerPixel.size(); i++){
		    	showerPixelArray[i] = showerPixel.get(i);
		    	corePixel.add(new Pixel(showerPixel.get(i)));
		    }
		    
		    input.put(Constants.KEY_SIMPLE_CLEAN_COREPIXEL+key, showerPixelArray);
			input.put(Constants.KEY_SIMPLE_CLEAN_COREPIXEL+key + Constants.PIXELSET, corePixel);
		
		}
	    return input;
	    
	}
	
}
