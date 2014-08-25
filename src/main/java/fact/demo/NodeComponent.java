/**
 * 
 */
package fact.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.demo.ui.ParameterDialog;
import stream.Processor;
import stream.io.Stream;
import stream.util.ApplicationBuilder.ProcessorNode;
import stream.util.ApplicationBuilder.StreamNode;
import stream.util.Node;

/**
 * @author chris
 * 
 */
public class NodeComponent extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -5540847639545692692L;

	final static Logger log = LoggerFactory.getLogger(NodeComponent.class);

	final static Color STREAM_FILL = new Color(179, 197, 114);
	final static Color STREAM_BORDER = new Color(83, 98, 41);

	final static Color PROCESSOR_FILL = new Color(209, 233, 255);
	final static Color PROCESSOR_BORDER = new Color(159, 178, 199);

	final Node node;
	Point position;

	int width = 40;
	int height = 40;
	Dimension dimension;

	public NodeComponent(stream.util.Node node) {
		this.node = node;

		final NodeComponent c = this;

		this.addMouseListener(new MouseAdapter() {

			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent
			 *      )
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				log.info("click! ({})", e);

				ParameterDialog d = new ParameterDialog(c);
				d.setLocation(new Point(e.getLocationOnScreen().x, e
						.getLocationOnScreen().y - d.getHeight() / 2));
				d.setVisible(true);
			}

		});

		if (node instanceof Stream) {
			width = 40;
			height = 40;
		}

		if (node instanceof Processor) {
			width = 60;
			height = 40;
		}

		dimension = new Dimension(width, height);
		this.setPreferredSize(dimension);
		this.setMinimumSize(dimension);
		this.setMaximumSize(dimension);
	}

	public NodeComponent(stream.util.Node node, Point p) {
		this(node);
		position = p;
		// this.setLocation(p);
		this.setLocation(position);
		this.setBounds(position.x - width / 2, position.y - height / 2, width,
				height);
	}

	/**
	 * @see javax.swing.JComponent#getLocation(java.awt.Point)
	 */
	@Override
	public Point getLocation(Point rv) {
		if (rv == null) {
			return new Point(position);
		}
		rv.x = position.x;
		rv.y = position.y;
		return rv;
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @return the position
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Point position) {
		this.position = position;
		this.setBounds(position.x, position.y, width, height);
	}

	/**
	 * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
	 */
	@Override
	protected void paintBorder(Graphics g) {
	}

	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return dimension;
	}

	/**
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// log.info("Painting component {}", this);
		// log.info("   position: {}", position);
		// log.info("   bounds:   {}", this.getBounds());
		// super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		String caption = null;

		if (node instanceof Stream) {
			// log.info("Drawing stream at {}", p);
			int radius = 20;

			g.setColor(STREAM_FILL);
			// g.fillOval(position.x, position.y, radius * 2, radius * 2);
			g.fillOval(position.x - radius, position.y - radius, radius * 2,
					radius * 2);

			g.setColor(STREAM_BORDER);
			// g.drawOval(position.x, position.y, radius * 2, radius * 2);
			g.drawOval(position.x - radius, position.y - radius, radius * 2,
					radius * 2);

			String id = ((StreamNode) node).get("id");
			if (id != null) {
				caption = id;
			}
		}

		if (node instanceof Processor) {

			ProcessorNode proc = (ProcessorNode) node;
			// log.info("Drawing processor {} at {}", proc, p);

			g.setColor(PROCESSOR_FILL);
			g.fillRoundRect(position.x - 30, position.y - 20, 60, 40, 6, 6);
			// g.fillRoundRect(position.x, position.y, 60, 40, 4, 4);
			g.setColor(PROCESSOR_BORDER);
			// g.drawRoundRect(position.x, position.y, 60, 40, 4, 4);
			g.drawRoundRect(position.x - 30, position.y - 20, 60, 40, 6, 6);

			g.setColor(PROCESSOR_BORDER);

			String className = "" + proc.get("class");
			int idx = className.lastIndexOf(".");
			if (idx > 0)
				className = className.substring(idx + 1);
			caption = className;
			//
			// drawStringCentered(g2, className, position.x - (width / 2),
			// position.y + height + 10);
		}

		if (node instanceof Process) {
			// log.info("Drawing process at {}", p);
		}

		if (caption != null) {
			drawStringCentered(g2, caption, position.x, position.y + 40);
		}

		if (log.isDebugEnabled()) {
			g.setColor(Color.red);
			g.fillOval(position.x - 1, position.y - 1, 3, 3);

			Rectangle r = this.getBounds();
			g.drawRect(r.x, r.y, r.width, r.height);
		}
	}

	public void drawStringCentered(Graphics2D g, String str, int x, int y) {
		Rectangle2D bounds = g.getFont().getStringBounds(str,
				g.getFontRenderContext());
		g.drawString(str, new Double(x - bounds.getWidth() / 2).intValue(), y);
	}
}
