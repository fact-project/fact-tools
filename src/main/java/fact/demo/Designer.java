/**
 * 
 */
package fact.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import stream.util.URLUtilities;
import stream.util.XMLUtils;
import fact.demo.ui.ContainerGraphPanel;

/**
 * 
 * @author Christian Bockermann
 * 
 */
public class Designer extends JFrame {

	/** The unique class ID */
	private static final long serialVersionUID = 2226894508001986217L;

	static Logger log = LoggerFactory.getLogger(Designer.class);

	final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
	final JTextField address = new JTextField();

	final ContainerGraphPanel graphPanel = new ContainerGraphPanel();

	Document doc;

	public Designer() {
		top.add(new JLabel("Application: "));
		top.add(address);

		Dimension dim = address.getPreferredSize();
		address.setPreferredSize(new Dimension(600, dim.height));

		address.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URL url = null;

				try {
					url = new URL(address.getText());
					address.setBorder(BorderFactory
							.createLineBorder(Color.GRAY));
				} catch (Exception ex) {
					address.setBorder(BorderFactory.createLineBorder(Color.RED));
				}

				if (url != null) {
					try {
						load(url);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		JButton save = new JButton("store");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					store(new URL(address.getText()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		});
		top.add(save);

		setLayout(new BorderLayout());
		add(top, BorderLayout.NORTH);
		add(graphPanel, BorderLayout.CENTER);
	}

	public void load(URL url) throws Exception {
		log.info("Loading application graph from {}", url);

		doc = XMLUtils.parseDocument(url.openStream());
		// ComputeGraph g = ApplicationBuilder.parseGraph(doc);
		//
		// graphPanel.setGraph(g);
		// graphPanel.repaint();
	}

	public void store(URL url) throws Exception {

		if (doc != null) {

			log.info("Storing document at {}", url);
			URLConnection con = url.openConnection();
			log.info("Using connection {}", con);

			con.setDoInput(true);
			con.setDoOutput(true);
			PrintStream out = new PrintStream(con.getOutputStream());
			out.println(XMLUtils.toString(doc));

			out.flush();
			out.close();

			String res = URLUtilities.readResponse(con.getInputStream());
			log.info("response is: {}", res);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Designer designer = new Designer();

		designer.address.setText("http://localhost:8080/test.xml");
		designer.setSize(new Dimension(1280, 900));

		designer.setVisible(true);
	}
}
