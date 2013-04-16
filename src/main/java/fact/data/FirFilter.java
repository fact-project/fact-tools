/**
 * 
 */
package fact.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import stream.annotations.Parameter;
import fact.Constants;

/**
 * This class implements a simple Fir-Filter. See
 * http://en.wikipedia.org/wiki/Fir_filter for Details. The coefficients of the
 * are stored in an array {n, n-1, n-2, ..}. Values outside of the data domain
 * are treated as zeroes.
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "")
public class FirFilter implements Processor {
	static Logger log = LoggerFactory.getLogger(FirFilter.class);
	private String color = "#CCCCCC";
	String[] keys = new String[] { Constants.DEFAULT_KEY };
	double[] coefficients = { 0.5f, 0.2f, 0.1f };
	private boolean overwrite = true;
	// templates for filter types. will set the coefficents array accordingly.  this is just for user convenience
	 
	private String template = "cfd";
	
	public FirFilter(String[] keys) {
		this.keys = keys;
	}





	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data event) {

		//
		//
		// String[] keys = new String[] { "Data", "DataCalibrated" };

		for (String key : keys) {

			if (event.get(key) == null) {
				System.out.println("ERROR! " + key
						+ " does not exist in FactEvent");
				return null;
			}

			/**
			 * get Data from map
			 */
			float[] data = (float[]) event.get(key);
			int roi = data.length / Constants.NUMBEROFPIXEL;

			/**
			 * array to keep results. will be initialized with zeros
			 */
			float[] result = new float[data.length];

			// foreach pixel
			for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
				// result[pix*roi] =
				// iterate over all slices
				for (int slice = 0; slice < roi; slice++) {
					int pos = pix * roi + slice;

					for (int i = Math.min(slice, coefficients.length - 1); i >= 0; i--) {
						// System.out.println("i: " + i);
						result[pos] += coefficients[i] * data[pos - i];
					}

				}

			}
			// add key to the output name
			if (overwrite) {
				event.put(Constants.KEY_FIR_RESULT, result);
				event.put("@" + Constants.KEY_COLOR + "_"
						+ Constants.KEY_FIR_RESULT, color);
			} else {
				event.put(Constants.KEY_FIR_RESULT + key, result);
				event.put("@" + Constants.KEY_COLOR + "_"
						+ Constants.KEY_FIR_RESULT, color);
			}

		}
		return event;
	}
	


	/* Getter and Setter*/
	
	public String getColor() {
		return color;
	}

	@Parameter(description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel", defaultValue = "#CCCCCC")
	public void setColor(String color) {
		this.color = color;
	}
	

	public double[] getCoefficents() {
		return coefficients;
	}

	@Parameter(required = true, description = "Filter coefficents array. {n, n-1, n-2, ..}.", defaultValue = "{0.5f,0.2f, 0.1f}")
	public void setCoefficents(double[] coefficents) {
		this.coefficients = coefficents;
	}
	
	

	public String[] getKeys() {
		return keys;
	}

	@Parameter(description = "The data elements to apply the Fir filter on, i.e. the name of the pixels array (floats).")
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	public boolean isOverwrite() {
		return overwrite;
	}
	
	@Parameter(required = false, description = "If false this operator will output the result as "
			+ Constants.KEY_FIR_RESULT
			+ "+{current Key}. Else the result will be named "
			+ Constants.KEY_FIR_RESULT)
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	
	public String getTemplate() {
		return template;
	}

	@Parameter(required = false, description = "Template strings for the coefficents array.", defaultValue = "cfd")
	public void setTemplate(String templateString) {
		this.template = templateString.toLowerCase();
		if (template.equals("cfd")) {
			setCoefficents(Constants.COEFFICENTS_CFD);
		}
		// if(template == "laplace"){
		// setCoefficents(Constants.COEFFICENTS_LAPLACE);
		// }
		if (template.equals("n3_average")) {
			setCoefficents(Constants.COEFFICENTS_N3);
		} else if (template.equals("n5_average")) {
			setCoefficents(Constants.COEFFICENTS_N5);
		} else if (template.equals("removesignal")) {
			setCoefficents(Constants.COEFFICENTS_REMOVE_SIGNAL);
		} else {
			log.info("The String "
					+ template
					+ " doesn't match any known template. Possible templates are: n3_average, n5_average, removeSignal, cfd ");
		}

	}

}