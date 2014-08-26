/**
 * 
 */
package fact.demo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.demo.NodeComponent;
import stream.app.ComputeGraph;
import stream.runtime.LifeCycle;
import stream.util.ApplicationBuilder;

/**
 * @author chris
 * 
 */
public class ContainerGraphPanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -694587077393857558L;

	final static Logger log = LoggerFactory
			.getLogger(ContainerGraphPanel.class);
	final static Color STREAM_FILL = new Color(179, 197, 114);
	final static Color STREAM_BORDER = new Color(83, 98, 41);

	final static Color PROCESSOR_FILL = new Color(209, 233, 255);
	final static Color PROCESSOR_BORDER = new Color(159, 178, 199);

	ComputeGraph graph;

	final Map<Object, Point> objects = new LinkedHashMap<Object, Point>();
	final List<NodeComponent> components = new ArrayList<NodeComponent>();

	int gridX = 135;
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

		int x = 40;
		int y0 = 100;
		int y = y0;

		for (Object root : roots) {
			Point p = new Point(x, y);
			log.info("Adding {} to coordinate {}", root, p);
			objects.put(root, p);

			components.add(new NodeComponent((stream.util.Node) root, p));

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

		for (NodeComponent c : components) {
			add(c);
		}
	}

	protected void positioning(int level, Object root, Point rootPos) {
		log.info("Determining position of {} at level {}", root, level);
		Set<Object> out = graph.getTargets(root);
		Integer num = out.size();
        log.info("Need to position {} targets from {}", num, rootPos);

        for (Object target : out) {
            //in case we traverse the node for the updatewidget we remove the node from the graph
            try{
                ApplicationBuilder.ProcessorNode n = (ApplicationBuilder.ProcessorNode) target;
                String name = n.attributes().get("class");
                if (name.equalsIgnoreCase("fact.demo.UpdateWidget")){
                    //do not increase the x position
                    Point p = new Point(rootPos.x, rootPos.y);
                    positioning(level + 1, target, p);

                    //Create new edges
                    Set<Object> children = graph.getTargets(target);
                    for(Object child: children){
                        graph.add(root, child);
                    }
                    //remove the node for the updatewidget
                    graph.remove(target);
                    continue;
                }
            } catch(ClassCastException e){
                //pass
            }
			Point p = new Point(rootPos.x + gridX, rootPos.y);
			objects.put(target, p);
			components.add(new NodeComponent((stream.util.Node) target, p));
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

		Color old = g.getColor();
		g.setColor(Color.DARK_GRAY);

		Stroke oldStroke = g2.getStroke();
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
		g2.setColor(old);
		g2.setStroke(oldStroke);

		// log.info("Rendering objects...");
		for (NodeComponent c : components) {
			c.paint(g2);
		}
	}

	public void drawStringCentered(Graphics2D g, String str, int x, int y) {
		Rectangle2D bounds = g.getFont().getStringBounds(str,
				g.getFontRenderContext());
		g.drawString(str, new Double(x - bounds.getWidth() / 2).intValue(), y);
	}
}