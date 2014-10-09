/**
 * 
 */
package fact.demo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author chris
 * 
 */
public class ParameterTableHeaderRenderer extends JPanel implements
		TableCellRenderer {

	/** The unique class ID */
	private static final long serialVersionUID = -7534128202908992786L;

	final JLabel label = new JLabel();

	public ParameterTableHeaderRenderer() {
		setBackground(Button.FONT_COLOR);
		setForeground(Button.BACKGROUND);

		setLayout(new BorderLayout());

		label.setFont(new Font("SansSerif", Font.BOLD, 12));
		label.setForeground(Button.BACKGROUND);
		label.setBackground(Button.FONT_COLOR);
		add(label, BorderLayout.CENTER);
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 *      .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		label.setText(value + "");
		return this;
	}
}
