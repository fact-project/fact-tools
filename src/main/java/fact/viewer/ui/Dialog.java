package fact.viewer.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 
 * This class shall be the parent of all dialogs within an application based on
 * the org.jwall.app framework. The current implementation simply provides a
 * method for centering the dialog on the screen.
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 */
public class Dialog extends JDialog implements WindowListener {

	private static final long serialVersionUID = 8649175463454832676L;

	/**
	 * Creates an empty dialog.
	 * 
	 */
	public Dialog() {
	}

	/**
	 * Creates an empty dialog with the given frame as its parent.
	 * 
	 * @param parent
	 *            The frame that shall become this dialog's parent.
	 */
	public Dialog(JFrame parent) {
		super(parent);
	}

	/**
	 * 
	 * This method determines the screen size and relocates the dialog at the
	 * screen center, regarding the dialog's current width and height.
	 * 
	 */
	public void center() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		int x = (dim.width - this.getSize().width) / 2;
		int y = (dim.height - this.getSize().height) / 2;

		setLocation(x, y);
	}

	public void center(Component c) {
		Point dim = c.getLocation();
		int x = (dim.x + (c.getWidth() - this.getSize().width) / 2);
		int y = (dim.y + (c.getHeight() - this.getSize().height) / 2);
		setLocation(x, y);
	}

	public void displayError(Exception ue) {
		ue.printStackTrace();

		StringWriter err = new StringWriter();
		PrintWriter errOut = new PrintWriter(err);
		ue.printStackTrace(errOut);

		JOptionPane.showMessageDialog(this,
				"Error occured: " + ue.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
	}
}