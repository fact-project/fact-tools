/**
 * 
 */
package fact.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import stream.Data;
import streams.dashboard.Dashboard;
import streams.dashboard.Widget;

/**
 * @author chris
 * 
 */
public class FACTDashboard extends Dashboard {

	/** The unique class ID */
	private static final long serialVersionUID = -7248249565119404416L;

	static Logger log = LoggerFactory.getLogger(FACTDashboard.class);

	public FACTDashboard() {
		super();
		nameSpaces.add("fact.demo.widgets");
		// this.setBackground(new Color(21, 30, 3));
		// this.content.setBackground(new Color(21, 30, 3));
		// this.display.setBackground(new Color(21, 30, 3));

		String bgKey = this.getClass().getCanonicalName() + ".background";
		log.info("background color key is '{}'", bgKey);

		this.setBackground(colors.get(bgKey, Color.WHITE));
		display.setBackground(colors.get(bgKey, Color.WHITE));
		display.setBackground(colors.get(bgKey, Color.WHITE));

		this.setSize(1440, 900);
	}

	/**
	 * @see streams.dashboard.Dashboard#createContentPane()
	 */
	@Override
	public JPanel createContentPane() {
		return new LogoPanel();
	}

	public class LogoPanel extends JPanel {
		/** The unique class ID */
		private static final long serialVersionUID = -8996411322637271307L;
		BufferedImage tulogo;
		BufferedImage logo;

		public LogoPanel() {
			try {
				setLayout(null);
				logo = ImageIO.read(FACTDashboard.class
						.getResourceAsStream("/fact-tools-logo.png"));
				logo = ImageIO.read(FACTDashboard.class
						.getResourceAsStream("/fact-tools-logo-trans.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				tulogo = ImageIO.read(FACTDashboard.class
						.getResourceAsStream("/tu-logo.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.setBackground(new Color(255, 0, 0, 0));
			this.setOpaque(true);
		}

		/**
		 * @see streams.dashboard.Dashboard#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			if (tulogo != null) {
				int x = g.getClipBounds().width - tulogo.getWidth() - 10;
				g.drawImage(tulogo, x, 12, null);
			}
			if (logo != null) {
				g.drawImage(logo, 10, 4, null);
			}
		}
	}

	/**
	 * @see streams.dashboard.Dashboard#init(org.w3c.dom.Element)
	 */
	@Override
	public void init(Element root) throws Exception {
		super.init(root);

		NodeList streams = root.getElementsByTagName("stream");

		log.info("application has {} streams", streams.getLength());
	}

	/**
	 * @see streams.dashboard.Dashboard#dataArrived(stream.Data)
	 */
	@Override
	public void dataArrived(Data item) {
		super.dataArrived(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see streams.dashboard.Dashboard#process(stream.Data)
	 */
	@Override
	public Data process(Data item) {
		final String wid;

		if (item.containsKey("@widget")) {
			wid = item.get("@widget") + "";
		} else {
			wid = null;
		}

		// log.info("Processing item with {} processors", processors.size());
		for (Widget w : processors) {
			// log.info("Processing item with widget {}: {}", w, item);

			if (wid != null && !wid.equals(w.getId())) {
				log.debug("Widget id {} not matching item @widget value '{}'",
						w.getId(), wid);
				continue;
			}

			if (w.handles(item)) {
				log.debug("Handling item {} with widget {}", item, w);
				w.process(item);
			} else {
				log.debug("Skipping widget {} for item {}", w, item);
			}
		}

		return item;
	}

}