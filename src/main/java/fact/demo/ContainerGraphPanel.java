/**
 * 
 */
package fact.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.app.ComputeGraph;
import stream.io.Stream;
import stream.util.ApplicationBuilder.ProcessorNode;
import stream.util.ApplicationBuilder.StreamNode;

/**
 * @author chris
 * 
 */
public class ContainerGraphPanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -694587077393857558L;

	static Logger log = LoggerFactory.getLogger(ContainerGraphPanel.class);
	final static Color STREAM_FILL = new Color(179, 197, 114);
	final static Color STREAM_BORDER = new Color(83, 98, 41);

	final static Color PROCESSOR_FILL = new Color(209, 233, 255);
	final static Color PROCESSOR_BORDER = new Color(159, 178, 199);

	ComputeGraph graph;

	Map<Object, Point> objects = new LinkedHashMap<Object, Point>();

	int gridX = 100;
	int gridY = 100;

	public ContainerGraphPanel() {
		setBackground(Color.white);
		setLayout(null);
	}

	/**
	 * @return the graph
	 */
	public ComputeGraph getGraph() {
		return graph;
	}

	/**
	 * @param graph
	 *            the graph to set
	 */
	public void setGraph(ComputeGraph graph) {
		this.graph = graph;

		// Map<String, Source> src = this.graph.sources();
		Set<Object> roots = graph.getRootSources();
		log.info("Found {} root sources", roots.size());

		int x = 0;
		int y0 = 100;
		int y = y0;

		for (Object root : roots) {
			Point p = new Point(x, y);
			log.info("Adding {} to coordinate {}", root, p);
			objects.put(root, p);

			positioning(0, root, p);

			// Set<Object> s = graph.getTargets(root);
			// log.info("Root {} has {} dependend objects ", root, s.size());
			//
			// x += xStep;
			//
			// int ty = y0;
			// for (Object o : s) {
			// p = new Point(x, ty);
			// log.info("Adding {} to coordinate {}", o, p);
			// objects.put(o, p);
			// // ty += yStep;
			// }
			y += gridY;
		}
	}

	protected void positioning(int level, Object root, Point rootPos) {
		log.info("Determining position of {} at level {}", root, level);
		Set<Object> out = graph.getTargets(root);
		Integer num = out.size();
		log.info("Need to position {} targets from {}", num, rootPos);

		for (Object target : out) {

			Point p = new Point(rootPos.x + gridX, rootPos.y);
			objects.put(target, p);

			Set<Object> sib = graph.getTargets(target);
			positioning(level + 1, target, p);
		}
	}

	/**
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.translate(40, 0);

		Color old = g.getColor();

		g.setColor(Color.DARK_GRAY);
		g2.setStroke(new BasicStroke(2.0f));
		// log.info("Rendering edges...");
		for (Object o : graph.nodes()) {
			Point from = objects.get(o);
			if (from == null) {
				// log.info("No coordinates found for {}", o);
				continue;
			}

			Set<Object> targets = graph.getTargets(o);
			if (targets != null && !targets.isEmpty()) {
				for (Object t : targets) {
					Point to = objects.get(t);

					if (to != null) {
						g.drawLine(from.x, from.y, to.x, to.y);
					} else {
						log.info("No coordinates found for target {}", t);
					}
				}
			}
		}

		// log.info("Rendering objects...");

		for (Object o : objects.keySet()) {
			Point p = objects.get(o);
			old = g.getColor();

			if (o instanceof Stream) {
				// log.info("Drawing stream at {}", p);
				int radius = 20;

				g.setColor(STREAM_FILL);
				g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

				g.setColor(STREAM_BORDER);
				g.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

				String id = ((StreamNode) o).get("id");
				if (id != null) {
					drawStringCentered(g2, id, p.x, p.y + 40);
				}
			}

			if (o instanceof Processor) {

				ProcessorNode proc = (ProcessorNode) o;
				// log.info("Drawing processor {} at {}", proc, p);

				g.setColor(PROCESSOR_FILL);
				g.fillRoundRect(p.x - 30, p.y - 20, 60, 40, 4, 4);
				g.setColor(PROCESSOR_BORDER);
				g.drawRoundRect(p.x - 30, p.y - 20, 60, 40, 4, 4);

				g.setColor(PROCESSOR_BORDER);

				String className = "" + proc.get("class");
				int idx = className.lastIndexOf(".");
				if (idx > 0)
					className = className.substring(idx + 1);

				drawStringCentered(g2, className, p.x, p.y + 40);
			}

			if (o instanceof Process) {
				// log.info("Drawing process at {}", p);
			}

			g.setColor(old);
		}

	}

	public void drawStringCentered(Graphics2D g, String str, int x, int y) {
		Rectangle2D bounds = g.getFont().getStringBounds(str,
				g.getFontRenderContext());
		g.drawString(str, new Double(x - bounds.getWidth() / 2).intValue(), y);
	}
}