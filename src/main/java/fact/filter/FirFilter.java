/**
 * 
 */
package fact.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Description;
import stream.annotations.Parameter;
import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

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
public class FirFilter extends SimpleFactEventProcessor<double[], double[]>{
	static Logger log = LoggerFactory.getLogger(FirFilter.class);
	double[] coefficients = { 0.5f, 0.2f, 0.1f };
	
	// templates for filter types. will set the coefficents array accordingly.  this is just for user convenience
	private String template = "cfd";
	

	@Override
	public double[] processSeries(double[] data) {
		double[] result = new double[data.length];

		// foreach pixel
		int roi = data.length / Constants.NUMBEROFPIXEL;
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
		return result;
	}	


	/* Getter and Setter*/

	public double[] getCoefficents() {
		return coefficients;
	}

	@Parameter(required = true, description = "Filter coefficents array. {n, n-1, n-2, ..}.", defaultValue = "{0.5f,0.2f, 0.1f}")
	public void setCoefficents(double[] coefficents) {
		this.coefficients = coefficents;
	}
	
	public String getTemplate() {
		return template;
	}

	@Parameter(required = false, description = "Template strings for the coefficents array.", defaultValue = "cfd")
	public void setTemplate(String templateString) {
		this.template = templateString.toLowerCase();
		if (template.equals("cfd")) {
			setCoefficents(Constants.COEFFICENTS_CFD);
		} else if (template.equals("n3_average")) {
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