/**
 * 
 */
package fact.demo;

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

/**
 * @author chris
 * 
 */
public class FACTDashboard extends Dashboard {

	/** The unique class ID */
	private static final long serialVersionUID = -7248249565119404416L;

	static Logger log = LoggerFactory.getLogger(FACTDashboard.class);

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
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				tulogo = ImageIO.read(FACTDashboard.class
						.getResourceAsStream("/tulogo.jpg"));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
				g.drawImage(logo, 10, 14, null);
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
}