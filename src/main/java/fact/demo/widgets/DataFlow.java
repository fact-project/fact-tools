/**
 * 
 */
package fact.demo.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.app.ComputeGraph;
import stream.io.SourceURL;
import stream.util.ApplicationBuilder;
import streams.dashboard.Widget;
import fact.demo.ContainerGraphPanel;

/**
 * @author chris
 * 
 */
public class DataFlow extends Widget {

	/** The unique class ID */
	private static final long serialVersionUID = -3652245233030173313L;

	static Logger log = LoggerFactory.getLogger(DataFlow.class);

	SourceURL config;

	final ContainerGraphPanel panel = new ContainerGraphPanel();

	/**
	 * @see streams.dashboard.Widget#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		setContent(panel);

		log.info("initializing DataFlow widget, config is at {}", config);
		ComputeGraph g = ApplicationBuilder.parseGraph(config);
		panel.setGraph(g);
	}

	/**
	 * @see streams.dashboard.Widget#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		return input;
	}

	/**
	 * @return the config
	 */
	public SourceURL getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(SourceURL config) {
		this.config = config;
	}
}
