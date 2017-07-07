/**
 *
 */
package fact.datacorrection;

import fact.Utils;
import fact.io.FITSStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.net.MalformedURLException;

/**
 *
 * This processor handles the DRS calibration. It requires a DRS data source
 * either as File or URL and will read the DRS data from that. This data is then
 * applied to all FactEvents processed by this class.
 *
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 */
public class MCDrsCalibration implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(MCDrsCalibration.class);

	@Parameter(required = false, description = "Resulting data array", defaultValue = "DataCalibrated")
	private String outputKey = "DataCalibrated";

    @Parameter(required = false, description = "Data array to be calibrated", defaultValue = "Data")
	private String inputKey = "Data";

    @Parameter(required =  false, description = "Whether to reverse the process.", defaultValue = "false")
    private boolean reverse = false;

	/**
	 * @see stream.Processor#process(Data)
	 */
	@Override
	public Data process(Data data) {
		if (!reverse) {
			Utils.isKeyValid(data, inputKey, short[].class);
			short[] rawData = (short[]) data.get(inputKey);
			double[] calibData = new double[rawData.length];
			for (int i = 0; i < rawData.length; i++) {
				calibData[i] = ((double)rawData[i])*(2000/4096)+903.32031;
			}

			data.put(outputKey, calibData);
		} else {
			Utils.isKeyValid(data, inputKey, double[].class);
			double[] calibData = (double[]) data.get(inputKey);
			short[] rawData = new short[calibData.length];
			for (int i = 0; i < calibData.length; i++) {
				rawData[i] = (short)((calibData[i]-903.32031)*(4096/2000));
			}

			data.put(outputKey, rawData);
		}


		return data;
	}

    @Override
    public void init(ProcessContext processContext) throws Exception {

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }

	// -----------setter---------------------
	public void setInputKey(String key) {
		this.inputKey = key;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setReverse(boolean rev) {
        this.reverse = rev;
	}


}
