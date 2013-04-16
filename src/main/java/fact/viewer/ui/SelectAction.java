/**
 * 
 */
package fact.viewer.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * @author chris
 * 
 */
public abstract class SelectAction extends AbstractAction {

	/** The unique class ID */
	private static final long serialVersionUID = 6391145834246582701L;

	String group = "Selection";

	public SelectAction(String name) {
		putValue(Action.NAME, name);
	}

	public String getGroup() {
		return group;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public abstract void actionPerformed(ActionEvent arg0);
}
