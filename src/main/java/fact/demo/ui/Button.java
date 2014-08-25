/**
 * 
 */
package fact.demo.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author chris
 * 
 */
public class Button extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -4543251965812149852L;
	final static Color BACKGROUND_HIGHLIGHTED = new Color(79, 79, 68);
	final static Color BACKGROUND = new Color(39, 39, 34);
	String text;
	final JLabel label;

	final List<ClickListener> listener = new ArrayList<ClickListener>();

	public Button(String text) {
		this.text = text;
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		this.label = new JLabel(this.text);
		this.label.setOpaque(true);

		label.setBorder(null);
		this.add(label);
		this.setBorder(BorderFactory.createLineBorder(new Color(79, 79, 68)));

		final MouseAdapter clickHandler = new MouseAdapter() {
			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				for (ClickListener l : listener) {
					l.onClick();
				}
			}

			/**
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				highlight();
			}

			/**
			 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
				unhighlight();
			}
		};

		addMouseListener(clickHandler);

		label.setFont(new Font("SansSerif", Font.PLAIN, 10));
		label.setForeground(Color.WHITE);
		label.setBackground(BACKGROUND);

		setBackground(BACKGROUND);
	}

	protected void highlight() {
		setBackground(BACKGROUND_HIGHLIGHTED);
		label.setBackground(BACKGROUND_HIGHLIGHTED);
	}

	protected void unhighlight() {
		setBackground(BACKGROUND);
		label.setBackground(BACKGROUND);
	}

	public void addClickListener(ClickListener l) {
		listener.add(l);
	}

	public interface ClickListener {
		public void onClick();
	}
}
