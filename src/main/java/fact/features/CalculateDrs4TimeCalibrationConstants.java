package fact.features;

import java.util.ArrayList;
import java.util.List;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;


public class CalculateDrs4TimeCalibrationConstants implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	
	private int number_of_patches = 160;
	private int number_of_slices = 1024;
	
	@Parameter(required=true, description="")
	private String key;
	@Parameter(required=true, description="")
	private String outputKey;
    	
	private double[] wi   = new double[1440*number_of_slices];
	private double[] wli  = new double[1440*number_of_slices];
	private double[] s_n  = new double[1440*number_of_slices];
	private double[] time_offsets = new double[1440*number_of_slices];
    
	/**
	 * Just a way of keeping the process() method small:
	 * @param input
	 * @return
	 */
	private double[] retrieve_data(Data input){
        
        EventUtils.mapContainsKeys(getClass(), input, key, "StartCellData");
        double[] data = null;
		try{
			data = (double[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		if (data==null){
			log.error("Couldn't get key: " + key);
		}
        
        return data;
    }
    
 
    /** 
     * Find rising edge zero crossings in data[start:start+length]
     * 
     * @param data : 1D array of FACT Data. (actually a flattened 2D array)
     * @param start : position in data, to start searching
     * @param length : number of time slices to consider in search.
     * @return linear interpolated zero crossing positions relative to start.
     */
    private ArrayList<Double> find_zero_crossings(double[] data, int start, int length){

    	ArrayList<Double> lzc = new ArrayList<Double>();
    	// lzc: List of Zero Crossings
    	
        // Find all zero crossings, with a rising edge
        // We iterate here over time slices, so the loop variable is called: sl
        for(int sl=0 ; sl < length-1 ; sl++){
            if (data[start+sl] < 0 && data[start+sl+1] > 0){
                double weight = data[start+sl] / (data[start+sl] - data[start+sl+1]);
                lzc.add( sl + weight);
            }
        }
        return lzc;
    }
    
    /**
     * We need to dertermine the w_i and the wl_i:
     * $$ w_i = \sum_{k=0}^{N_k} w_ki $$
     * 
     * and
     * $$ wl_i = \sum_{k=0}^{N_k} w_ki \cdot l_k $$
     * 
     *  where l_k is nothing else than the apparent width of a calibration period.
     * 
     * @param lzc List of zero crossings
     * @param patch_id	just the patch id of the patch for which lzc is valid
     * @param sc start cell for the current patch
     */
    private void calculate_wi_wli(ArrayList<Double> lzc, int patch_id, int sc){
        // now we iterate over this list of zero-crossing:
        //  each pair of two crossings, forms a period of the calibration signal.
        // The measured length (in slices) of this period, will be associated with the 
        // physical cells of the DRS4 chip. Therefor we need the startcell
        // to convert slices into physical cell ids.
        
    	Double[] l = lzc.toArray(new Double[lzc.size()]);
    	
        for (int period_id=0; period_id<l.length-1; period_id++){

            double left = l[period_id];
            double right = l[period_id+1];

            double l_k = right - left;
            
            int left_i = (int)left;
            double left_f = left-left_i;
            
            int right_i = (int)right;
            double right_f = right-right_i;
            
            
            int left_index  = patch_id*number_of_slices + ( left_i+sc)%1024; 
            int right_index = patch_id*number_of_slices + (right_i+sc)%1024; 
            
            wi[left_index] += (1.-left_f);
            wli[left_index] += (1.-left_f)  * l_k;
            
            wi[right_index] += right_f;
            wli[right_index] += right_f  * l_k;
            
            for (int i=left_i+1; i<right_i; i++){
                
                int cid  = patch_id*number_of_slices + ( i+sc)%1024;
                
                wi[cid] += 1.;
                wli[cid] += l_k;
            }
        }
    }
    
    /**
     * s_n is defined as 
     * 
     * @param patch_id
     */
    private void calculate_s_n_and_time_offsets( int patch_id){			
		double cumsum_wi  = 0.;
		double cumsum_wli = 0.;
		for(int n=0; n < number_of_slices; n++)
		{
			int d = patch_id*number_of_slices+n;
			cumsum_wi  += wi[d];
			cumsum_wli += wli[d];
			s_n[d] = cumsum_wli/cumsum_wi; 	
		}

		int end = patch_id*number_of_slices+1023;
		for( int i=0; i<number_of_slices; i++){
			int d = patch_id*number_of_slices+i;
			time_offsets[d] = (i+1)*( 1. - (s_n[end]/s_n[d]) );
		}

    }
    
	@Override
	public Data process(Data input) {
        
		double[] data = retrieve_data(input);
		short[] startCell = (short[]) input.get("StartCellData");
		
		for(int patch_id=0 ; patch_id < number_of_patches ; patch_id++){
            // We only look at the 9th channel of each DRS4 chip
            // because only this channel sees a special periodic calibration signal.
            int chid = 9*patch_id + 8;
            short sc = startCell[chid];
            
            ArrayList<Double> lzc = find_zero_crossings(data, chid*number_of_slices, number_of_slices);
            calculate_wi_wli(lzc, chid, sc);
            calculate_s_n_and_time_offsets(chid);
        }
		
		
		input.put(outputKey, time_offsets);
		input.put("sum_of_wi", wi);
		input.put("sum_of_wli", wli);
		input.put("s_n", s_n);
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
}


