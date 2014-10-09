/**
 * 
 */
package fact.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.data.DataFactory;

/**
 * @author chris
 * 
 */
public class UpdateWidget extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(UpdateWidget.class);

	protected FACTDashboard dashboard;

	protected String widget;

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (dashboard != null) {
			log.info("Sending input to dashboard...");
			Data up = DataFactory.create(input);
			up.put("@widget", widget + "");
			dashboard.dataArrived(up);
		}

		return input;
	}

	/**
	 * @return the dashboard
	 */
	public FACTDashboard getDashboard() {
		return dashboard;
	}

	/**
	 * @param dashboard
	 *            the dashboard to set
	 */
	public void setDashboard(FACTDashboard dashboard) {
		this.dashboard = dashboard;

	}

	/**
	 * @return the widget
	 */
	public String getWidget() {
		return widget;
	}

	/**
	 * @param widget
	 *            the widget to set
	 */
	public void setWidget(String widget) {
		this.widget = widget;
	}
}
