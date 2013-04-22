package fact.image.monitors;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.data.Statistics;
import stream.monitor.DataRate;
import stream.plotter.DataVisualizer;
import stream.plotter.PlotPanel;

public class StatusWindow extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(PlotPanel.class);
//	private boolean showErrorBars = true;
	private JFrame frame;
	private boolean keepOpen;
	private DataRate monitor;
	JPanel panel;
	Statistics stats;
	private int every = 5;
	long eventCounter = 0;
	private JLabel rateInfoLabel;
	private JLabel rateLabel;
	private JLabel eventCountInfoLabel;
	private JLabel eventCounterLabel;
	private JLabel timerInfoLabel;
	private JLabel timerLabel;
	private long start;
	private long elapsed;
	private SimpleDateFormat df;
	private JTextPane textPane;
	private final StringBuilder b = new StringBuilder();
	private JScrollPane scrollPane;
	public int getEvery() {
		return every;
	}
	public void setEvery(int every) {
		this.every = every;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		width = 450;
		height = 250;
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(488, 315);
		frame.setVisible(true);
		monitor = new  stream.monitor.DataRate();
		monitor.setEvery(every);
		
		textPane = new JTextPane();
		scrollPane = new JScrollPane(textPane);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		textPane.setEditable(false);
		
		panel = new JPanel();
		panel.setLayout(new GridLayout(4, 2, 1, 0));
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		rateInfoLabel = new JLabel("DataRate currently is: ");
		panel.add(rateInfoLabel);
		
		rateLabel = new JLabel(" ");
		panel.add(rateLabel);
		
		eventCountInfoLabel = new JLabel("Number of processed Events");
		panel.add(eventCountInfoLabel);
		
		eventCounterLabel = new JLabel(" ");
		panel.add(eventCounterLabel);
		
		timerInfoLabel = new JLabel("Elapsed Time: ");
		panel.add(timerInfoLabel);
		
		timerLabel = new JLabel(" ");
		panel.add(timerLabel);
		
		panel.add(new JLabel("Keys in Event: "));

		start = System.currentTimeMillis();
		df = new SimpleDateFormat("HH 'hours', mm 'mins,' ss 'seconds'");
		df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		
	}

	@Override
	public Data processMatchingData(Data data) {
		monitor.process(data);
		stats = monitor.getStatistics();
		if(eventCounter % every == 0){
			rateLabel.setText( stats.get("dataRate").toString()  );
			eventCounterLabel.setText(Long.toString(eventCounter));
			elapsed = System.currentTimeMillis() - start;
			timerLabel.setText(df.format(new Date(elapsed)));
		}
		eventCounter++;
		
		b.setLength(0);
		for (String str : data.keySet()) {
			b.append(str);
			b.append("\n");
		}
		textPane.setText(b.toString());
		
		return data;
	}

	@Override
	public void finish() throws Exception {
		if (!keepOpen) {
			log.debug("Closing plot frame");
			frame.setVisible(false);
			frame.dispose();
			frame = null;
		} else {
			log.debug("Keeping plot frame visible...");
		}
	}
}

