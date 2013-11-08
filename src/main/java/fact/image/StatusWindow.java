package fact.image;

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
import stream.plotter.DataVisualizer;
import stream.plotter.PlotPanel;
/**
 * <PRE format="md">
 * This opens up a small window containing some information about the currently running stream like number of items per second or the names of the keys in the item.
 * ![Alt text](/status_window.jpg)
 * ![Alt text](/images/status_window.jpg)
 * ![Alt text](/images/status_window)
 * ![Alt text](status_window)
 * ![Alt text](src/main/resources/images/status_window.jpg)
 * ![Alt text](/home/bruegge/Documents/workspace/fact/src/main/resources/images/status_window.jpg)
 * 
 * </PRE>
 * <img src="images/status_window.jpg">
 * <img src="status_window.jpg">
 * <img src="./status_window.jpg">
 * <img src="/home/bruegge/Documents/workspace/fact/src/main/resources/images/status_window.jpg">
 * 
 * @author bruegge
 *
 */
public class StatusWindow extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(PlotPanel.class);
//	private boolean showErrorBars = true;
	private JFrame frame;
	private boolean keepOpen;
	JPanel panel;
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
		
		textPane = new JTextPane();
		scrollPane = new JScrollPane(textPane);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		textPane.setEditable(false);
		
		panel = new JPanel();
		panel.setLayout(new GridLayout(4, 2, 1, 0));
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		rateInfoLabel = new JLabel("Average DataRate currently is: ");
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
		if(eventCounter % every == 0){
			eventCounterLabel.setText(Long.toString(eventCounter));
			elapsed = System.currentTimeMillis() - start;
			timerLabel.setText(df.format(new Date(elapsed)));
			rateLabel.setText( Double.toString((((double)eventCounter)/elapsed)*1000)  );
			
			b.setLength(0);
			for (String str : data.keySet()) {
				b.append(str);
				b.append("\n");
			}
			textPane.setText(b.toString());
		}
		eventCounter++;
		

		
		return data;
	}

	@Override
	public void finish() throws Exception {
		if (!keepOpen && frame != null) {
			log.debug("Closing plot frame");
			frame.setVisible(false);
			frame.dispose();
			frame = null;
		} else {
			log.debug("Keeping plot frame visible...");
		}
	}
}

